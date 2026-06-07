# Adapters — Business Reference

## JPA Entity with full domain mapping

```java
@Entity
@Table(name = "orders")
class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_currency", nullable = false, length = 3)
    private String totalCurrency;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static OrderJpaEntity from(Order order) {
        var entity = new OrderJpaEntity();
        entity.id = order.getId().value();
        entity.customerId = order.getCustomerId().value();
        entity.status = order.getStatus();
        entity.totalAmount = order.getTotal().getAmount();
        entity.totalCurrency = order.getTotal().getCurrency();
        entity.items = order.getItems().stream()
            .map(OrderItemJpaEntity::from)
            .collect(Collectors.toList());
        return entity;
    }

    public Order toDomain() {
        var domainItems = items.stream()
            .map(OrderItemJpaEntity::toDomain)
            .collect(Collectors.toList());
        return Order.reconstitute(
            new OrderId(id),
            new CustomerId(customerId),
            domainItems,
            status,
            new Money(totalAmount, totalCurrency)
        );
    }
}
```

## Messaging Adapter

```java
@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publishSingle);
    }

    private void publishSingle(DomainEvent event) {
        var topic = resolveTopicFor(event);
        var key = extractKey(event);
        kafkaTemplate.send(topic, key, event);
    }

    private String resolveTopicFor(DomainEvent event) {
        return switch (event) {
            case OrderConfirmedEvent e  -> "order.confirmed";
            case OrderCancelledEvent e  -> "order.cancelled";
            case OrderShippedEvent e    -> "order.shipped";
        };
    }

    private String extractKey(DomainEvent event) {
        return switch (event) {
            case OrderConfirmedEvent e -> e.orderId().value().toString();
            case OrderCancelledEvent e -> e.orderId().value().toString();
            case OrderShippedEvent e   -> e.orderId().value().toString();
        };
    }
}
```

## External HTTP Adapter

```java
public interface PaymentGatewayPort {
    PaymentResult charge(PaymentRequest request);
}

@Component
public class StripePaymentAdapter implements PaymentGatewayPort {

    private final WebClient webClient;

    public StripePaymentAdapter(@Value("${stripe.base-url}") String baseUrl,
                                 @Value("${stripe.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        try {
            var stripeRequest = StripeChargeRequest.from(request);
            var stripeResponse = webClient.post()
                .uri("/charges")
                .bodyValue(stripeRequest)
                .retrieve()
                .bodyToMono(StripeChargeResponse.class)
                .block(Duration.ofSeconds(10));

            return PaymentResult.success(stripeResponse.chargeId());
        } catch (WebClientResponseException e) {
            return PaymentResult.failure(e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }
}
```

## ArchUnit — enforce layer rules in tests

```java
@AnalyzeClasses(packages = "com.company.service")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainHasNoDependencyOnOtherLayers =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..adapter..", "..config..")
            .as("Domain must not depend on outer layers");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..")
            .as("Application must not depend on adapters");

    @ArchTest
    static final ArchRule domainHasNoFrameworkDependencies =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
            .as("Domain must be framework-free");

    @ArchTest
    static final ArchRule jpaEntitiesOnlyInPersistencePackage =
        classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..adapter.out.persistence..")
            .as("JPA @Entity annotations must only appear in the persistence adapter");
}
```

# Advanced Factory Patterns — Business Reference

## Factory + Builder Combination

```java
public final class NotificationFactory {

    private NotificationFactory() {}

    /**
     * Creates a fully configured {@link Notification} for the given event type.
     * Combines factory dispatch with builder-style construction.
     *
     * @param event the triggering domain event; must not be null
     * @return a ready-to-send notification
     */
    public static Notification forEvent(DomainEvent event) {
        return switch (event) {
            case OrderPlacedEvent e -> Notification.builder()
                .recipient(e.customerId().toString())
                .subject("Order Confirmed")
                .template("order-placed")
                .variable("orderId", e.orderId().value())
                .variable("total", e.total().getAmount())
                .channel(NotificationChannel.EMAIL)
                .build();

            case PaymentFailedEvent e -> Notification.builder()
                .recipient(e.customerId().toString())
                .subject("Payment Issue")
                .template("payment-failed")
                .variable("reason", e.reason())
                .channel(NotificationChannel.SMS)
                .priority(Priority.HIGH)
                .build();

            case OrderShippedEvent e -> Notification.builder()
                .recipient(e.customerId().toString())
                .subject("Your order is on the way!")
                .template("order-shipped")
                .variable("trackingCode", e.trackingCode())
                .channel(NotificationChannel.PUSH)
                .build();
        };
    }
}
```

## Prototype-Based Factory

```java
public interface DocumentTemplate extends Cloneable {
    Document createDocument();
    DocumentTemplate withTitle(String title);
    DocumentTemplate withAuthor(String author);
}

public final class DocumentTemplateRegistry {

    private final Map<String, DocumentTemplate> prototypes = new HashMap<>();

    public void register(String name, DocumentTemplate template) {
        prototypes.put(name, template);
    }

    /**
     * Creates a document from a registered template prototype.
     * Each call returns an independent copy — prototypes are never mutated.
     *
     * @param templateName the registered template name
     * @return a new document instance pre-configured from the prototype
     */
    public Document createFrom(String templateName) {
        var prototype = prototypes.get(templateName);
        if (prototype == null) {
            throw new UnknownTemplateException(templateName);
        }
        return prototype.createDocument();
    }
}
```

## Lazy-Initialized Factory with Caching

```java
public final class HeavyResourceFactory {

    private final Map<ResourceKey, HeavyResource> cache = new ConcurrentHashMap<>();
    private final ResourceConfig config;

    public HeavyResourceFactory(ResourceConfig config) {
        this.config = config;
    }

    /**
     * Returns a cached {@link HeavyResource} if one exists for the given key,
     * otherwise initializes one. Thread-safe via {@link ConcurrentHashMap#computeIfAbsent}.
     */
    public HeavyResource getOrCreate(ResourceKey key) {
        return cache.computeIfAbsent(key, k -> initializeResource(k, config));
    }

    private HeavyResource initializeResource(ResourceKey key, ResourceConfig config) {
        return HeavyResource.builder()
            .key(key)
            .connectionPool(config.poolSize())
            .timeout(config.timeout())
            .build();
    }

    public void evict(ResourceKey key) {
        var resource = cache.remove(key);
        if (resource != null) {
            resource.close();
        }
    }
}
```

## Abstract Factory for Testing — Swapping Infrastructure

```java
public interface RepositoryFactory {
    OrderRepository createOrderRepository();
    ProductRepository createProductRepository();
    CustomerRepository createCustomerRepository();
}

@Configuration
@Profile("!test")
public class JpaRepositoryFactory implements RepositoryFactory {
    @Override public OrderRepository createOrderRepository() { return new OrderJpaRepository(); }
    @Override public ProductRepository createProductRepository() { return new ProductJpaRepository(); }
    @Override public CustomerRepository createCustomerRepository() { return new CustomerJpaRepository(); }
}

@Configuration
@Profile("test")
public class InMemoryRepositoryFactory implements RepositoryFactory {
    @Override public OrderRepository createOrderRepository() { return new InMemoryOrderRepository(); }
    @Override public ProductRepository createProductRepository() { return new InMemoryProductRepository(); }
    @Override public CustomerRepository createCustomerRepository() { return new InMemoryCustomerRepository(); }
}
```

## Factory with Decorator Chain

```java
public final class PaymentProcessorFactory {

    private final MetricsRegistry metrics;
    private final AuditLogger auditLogger;
    private final CircuitBreakerRegistry circuitBreakers;

    public PaymentProcessorFactory(MetricsRegistry metrics,
                                    AuditLogger auditLogger,
                                    CircuitBreakerRegistry circuitBreakers) {
        this.metrics = metrics;
        this.auditLogger = auditLogger;
        this.circuitBreakers = circuitBreakers;
    }

    /**
     * Creates a processor wrapped with observability and resilience decorators.
     * The wrapping order is: circuit breaker → audit → metrics → core processor.
     */
    public PaymentProcessor create(PaymentMethod method, PaymentProcessor coreProcessor) {
        return CircuitBreakerDecorator.decorate(
            coreProcessor,
            circuitBreakers.circuitBreaker(method.name()),
            AuditDecorator.decorate(
                coreProcessor,
                auditLogger,
                MetricsDecorator.decorate(coreProcessor, metrics)
            )
        );
    }
}
```

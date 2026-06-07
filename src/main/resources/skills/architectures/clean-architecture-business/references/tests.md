# Testing Strategy — Business Reference

## Unit Test — Domain (no Spring)

```java
class OrderTest {

    @Test
    void confirm_shouldTransitionStatusAndRegisterEvent() {
        var order = Order.create(new CustomerId(UUID.randomUUID()));
        order.addItem(ProductId.generate(), Money.of("50.00", "USD"), 2);

        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.pullDomainEvents())
            .hasSize(1)
            .first().isInstanceOf(OrderConfirmedEvent.class);
    }

    @Test
    void confirm_shouldThrowWhenNoItems() {
        var order = Order.create(new CustomerId(UUID.randomUUID()));
        assertThatThrownBy(order::confirm).isInstanceOf(EmptyOrderException.class);
    }

    @Test
    void addItem_shouldThrowWhenOrderAlreadyConfirmed() {
        var order = confirmedOrder();
        assertThatThrownBy(() ->
            order.addItem(ProductId.generate(), Money.of("10.00", "USD"), 1))
            .isInstanceOf(OrderAlreadyConfirmedException.class);
    }

    private Order confirmedOrder() {
        var order = Order.create(new CustomerId(UUID.randomUUID()));
        order.addItem(ProductId.generate(), Money.of("20.00", "USD"), 1);
        order.confirm();
        order.pullDomainEvents(); // clear events
        return order;
    }
}
```

## Unit Test — Use Case (mocked ports)

```java
@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private InventoryPort inventory;
    @Mock private DomainEventPublisher eventPublisher;
    @InjectMocks private PlaceOrderUseCaseImpl useCase;

    @Test
    void execute_shouldCreateAndConfirmOrder() {
        var customerId = new CustomerId(UUID.randomUUID());
        var productId = ProductId.generate();
        var command = new PlaceOrderCommand(customerId,
            List.of(new OrderItemCommand(productId, 2)));

        var customer = Customer.reconstitute(customerId, "Alice");
        var product = Product.reconstitute(productId, Money.of("25.00", "USD"), "Widget");
        var savedOrder = buildConfirmedOrder(customerId, productId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(savedOrder);

        var response = useCase.execute(command);

        assertThat(response).isNotNull();
        verify(inventory).reserve(eq(productId), eq(2));
        verify(eventPublisher).publish(anyList());
    }

    @Test
    void execute_shouldThrowWhenCustomerNotFound() {
        var command = new PlaceOrderCommand(new CustomerId(UUID.randomUUID()), List.of());
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(CustomerNotFoundException.class);
    }
}
```

## Integration Test — Persistence Adapter

```java
@DataJpaTest
class OrderPersistenceAdapterTest {

    @Autowired private OrderJpaRepository jpaRepository;
    private OrderPersistenceAdapter adapter;

    @BeforeEach
    void setUp() { adapter = new OrderPersistenceAdapter(jpaRepository); }

    @Test
    void saveAndRetrieve_shouldPreserveDomainState() {
        var order = Order.create(new CustomerId(UUID.randomUUID()));
        order.addItem(ProductId.generate(), Money.of("30.00", "USD"), 3);
        order.confirm();
        order.pullDomainEvents();

        var saved = adapter.save(order);
        var found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(found.get().getTotal()).isEqualTo(Money.of("90.00", "USD"));
        assertThat(found.get().getItems()).hasSize(1);
    }
}
```

## Architecture Test — ArchUnit

See `references/adapters.md` for full ArchUnit rule definitions.

Add to pom.xml:
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

# Application Layer — Business Reference

## CQRS — Full Split

```java
// Write side
public interface PlaceOrderUseCase {
    OrderResponse execute(PlaceOrderCommand command);
}

public interface CancelOrderUseCase {
    void execute(CancelOrderCommand command);
}

// Read side — can use a denormalized read model or projections
public interface GetOrderQuery {
    OrderResponse findById(OrderId id);
    Page<OrderSummary> findByCustomerId(CustomerId customerId, Pageable pageable);
    List<OrderSummary> findByStatus(OrderStatus status);
}

// Read model — optimized for display, may differ from domain model
public record OrderSummary(
    UUID id,
    String customerName,
    String status,
    BigDecimal total,
    int itemCount,
    Instant createdAt
) { }
```

## Transactional Outbox Pattern

Guarantees domain events are published even if the message broker is temporarily unavailable:

```java
@Service
@Transactional
public class PlaceOrderUseCaseImpl implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository; // persists events to DB table

    @Override
    public OrderResponse execute(PlaceOrderCommand command) {
        var order = buildOrder(command);
        order.confirm();
        var saved = orderRepository.save(order);

        // Persist events to outbox table in the SAME transaction
        var events = saved.pullDomainEvents();
        events.forEach(e -> outboxRepository.save(OutboxMessage.from(e)));

        // A separate scheduler/CDC picks up the outbox and publishes to broker
        return OrderResponse.from(saved);
    }
}

public record OutboxMessage(UUID id, String eventType, String payload, Instant createdAt) {
    public static OutboxMessage from(DomainEvent event) {
        return new OutboxMessage(UUID.randomUUID(), event.getClass().getSimpleName(),
            serialize(event), event.occurredOn());
    }
}
```

## Saga (Choreography-based)

```java
// Each service listens to events and reacts, without a central orchestrator
@Component
public class InventoryReservationSaga {

    private final ReserveInventoryUseCase reserveInventory;
    private final DomainEventPublisher eventPublisher;

    @EventListener
    public void on(OrderConfirmedEvent event) {
        try {
            reserveInventory.execute(new ReserveInventoryCommand(event.orderId(), event.items()));
            eventPublisher.publish(List.of(new InventoryReservedEvent(event.orderId())));
        } catch (InsufficientStockException e) {
            eventPublisher.publish(List.of(new InventoryReservationFailedEvent(
                event.orderId(), e.getMessage())));
        }
    }

    @EventListener
    public void on(InventoryReservationFailedEvent event) {
        // compensating transaction: cancel the order
        cancelOrder.execute(new CancelOrderCommand(event.orderId(), "Insufficient stock"));
    }
}
```

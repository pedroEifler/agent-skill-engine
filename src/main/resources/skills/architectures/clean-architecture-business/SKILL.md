---
name: clean-arch-business
description: >
  Use this skill whenever a professional developer requests Clean Architecture, Hexagonal Architecture
  (Ports & Adapters), Domain-Driven Design (DDD), or any production-grade layered Java project structure.
  Triggers include: "clean architecture", "hexagonal architecture", "ports and adapters", "DDD", "domain
  driven design", "onion architecture", "use case pattern", "decouple domain from framework", "aggregate",
  "value object", "domain event", "CQRS", "repository pattern", "dependency inversion in Java", "framework
  agnostic domain", "clean code architecture Java". Produces production-ready English code with strict layer
  separation, pure Java domain (no framework annotations), port/adapter pattern, proper aggregate design,
  domain events, CQRS, and full test coverage strategy per layer. ALWAYS use this skill for any professional
  Clean Architecture or DDD project generation task, even when the request seems partial or simple.
---

# Skill: Clean Architecture Business

Generates production-grade Java projects with strict Clean Architecture layering. Pure Java domain,
port/adapter pattern, full DDD tactical patterns, CQRS, and testable by design.

---

## 1. Architecture Overview

```
com.company.service/
├── domain/              ← Pure Java. Zero framework dependencies.
│   ├── model/           ← Entities, Aggregates, Value Objects
│   ├── event/           ← Domain Events
│   ├── exception/       ← Domain Exceptions
│   └── port/
│       ├── in/          ← Input Ports (Use Case interfaces)
│       └── out/         ← Output Ports (Repository/Service interfaces)
│
├── application/         ← Orchestration only. Uses domain + ports.
│   ├── usecase/         ← Use Case implementations
│   └── dto/             ← Input/Output command/query objects
│
├── adapter/             ← Framework-specific implementations.
│   ├── in/
│   │   ├── web/         ← REST Controllers, request/response DTOs
│   │   └── messaging/   ← Message consumers
│   └── out/
│       ├── persistence/ ← JPA entities, Spring Data repos, adapters
│       ├── messaging/   ← Message producers
│       └── external/    ← HTTP clients, external API adapters
│
└── config/              ← Spring @Configuration, Bean wiring
```

**Dependency Rule:** arrows point inward only.
`adapter → application → domain` (never the reverse).

---

## 2. Domain Layer

### Entities & Aggregates

```java
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money total;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Order(OrderId id, CustomerId customerId) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.items = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
        this.total = Money.zero("USD");
    }

    public static Order create(CustomerId customerId) {
        return new Order(OrderId.generate(), customerId);
    }

    public static Order reconstitute(OrderId id, CustomerId customerId,
                                     List<OrderItem> items, OrderStatus status, Money total) {
        var order = new Order(id, customerId);
        order.items.addAll(items);
        order.status = status;
        order.total = total;
        return order;
    }

    public void addItem(ProductId productId, Money unitPrice, int quantity) {
        if (status != OrderStatus.DRAFT) {
            throw new OrderAlreadyConfirmedException(id);
        }
        items.add(new OrderItem(productId, unitPrice, quantity));
        recalculateTotal();
    }

    public void confirm() {
        if (items.isEmpty()) throw new EmptyOrderException(id);
        this.status = OrderStatus.CONFIRMED;
        registerEvent(new OrderConfirmedEvent(id, customerId, total, Instant.now()));
    }

    public void cancel(String reason) {
        if (status == OrderStatus.SHIPPED) throw new CannotCancelShippedOrderException(id);
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(id, reason, Instant.now()));
    }

    private void recalculateTotal() {
        this.total = items.stream()
            .map(OrderItem::subtotal)
            .reduce(Money.zero("USD"), Money::add);
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public OrderId getId() { return id; }
    public CustomerId getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public Money getTotal() { return total; }
}
```

### Value Objects

```java
public final class Money {

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money multiply(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
    }

    @Override public int hashCode() { return Objects.hash(amount, currency); }
    @Override public String toString() { return amount + " " + currency; }

    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}

public record OrderId(UUID value) {
    public OrderId { Objects.requireNonNull(value); }
    public static OrderId generate() { return new OrderId(UUID.randomUUID()); }
    public static OrderId of(String value) { return new OrderId(UUID.fromString(value)); }
}
```

---

## 3. Ports

```java
// Input Port
public interface PlaceOrderUseCase {
    /**
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws ProductNotFoundException  if any product in the request does not exist
     * @throws InsufficientStockException if stock is unavailable for any item
     */
    OrderResponse execute(PlaceOrderCommand command);
}

// Output Ports
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
}

public interface DomainEventPublisher {
    void publish(List<DomainEvent> events);
}

public interface InventoryPort {
    void reserve(ProductId productId, int quantity);
    void release(ProductId productId, int quantity);
}
```

---

## 4. Application Layer — Use Cases

```java
@Service
@Transactional
public class PlaceOrderUseCaseImpl implements PlaceOrderUseCase {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InventoryPort inventory;
    private final DomainEventPublisher eventPublisher;

    public PlaceOrderUseCaseImpl(CustomerRepository customerRepository,
                                  ProductRepository productRepository,
                                  OrderRepository orderRepository,
                                  InventoryPort inventory,
                                  DomainEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.inventory = inventory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse execute(PlaceOrderCommand command) {
        customerRepository.findById(command.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        var order = Order.create(command.customerId());

        for (var item : command.items()) {
            var product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
            inventory.reserve(item.productId(), item.quantity());
            order.addItem(product.getId(), product.getPrice(), item.quantity());
        }

        order.confirm();
        var saved = orderRepository.save(order);
        eventPublisher.publish(saved.pullDomainEvents());

        return OrderResponse.from(saved);
    }
}
```

---

## 5. Adapter Layer

### Web Adapter (in)
```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderQuery getOrderQuery;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request) {
        var command = request.toCommand();
        var response = placeOrderUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(getOrderQuery.findById(OrderId.of(id.toString())));
    }
}
```

### Persistence Adapter (out)
```java
@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        var entity = OrderJpaEntity.from(order);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.value()).stream()
            .map(OrderJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
}
```

---

## 6. Naming Conventions

```java
// Domain
public class Order { }                          // Entity/Aggregate: noun
public record Money(BigDecimal amount, ...) {}  // Value Object: noun
public record OrderId(UUID value) {}            // Typed ID: EntityId
public interface OrderRepository {}             // Output Port: noun + Repository/Port/Gateway
public interface PlaceOrderUseCase {}           // Input Port: verb + UseCase
public record OrderConfirmedEvent(...) {}       // Domain Event: past tense + Event

// Application
public class PlaceOrderUseCaseImpl {}           // Use Case impl: Interface + Impl
public record PlaceOrderCommand(...) {}         // Input DTO: verb + Command/Query
public record OrderResponse(...) {}             // Output DTO: noun + Response

// Adapter
public class OrderController {}                 // Web: noun + Controller
public class OrderPersistenceAdapter {}         // Persistence: noun + PersistenceAdapter
public class OrderJpaEntity {}                  // JPA entity: noun + JpaEntity
public interface OrderJpaRepository {}          // Spring Data: noun + JpaRepository
```

---

## 7. Code Generation Checklist

- [ ] Domain layer has zero imports from `org.springframework.*`, `jakarta.persistence.*`
- [ ] All domain IDs are typed (e.g. `OrderId`, `CustomerId`), not raw `Long`/`UUID`
- [ ] Value objects are immutable with structural equality (`equals`/`hashCode`)
- [ ] Aggregate roots expose `pullDomainEvents()` for event collection
- [ ] Input/Output ports defined as interfaces in `domain/port`
- [ ] Use cases depend only on port interfaces, never on adapters
- [ ] JPA `@Entity` classes exist only in `adapter/out/persistence`
- [ ] Mapping logic (domain ↔ JPA) lives in the JPA entity or a dedicated mapper
- [ ] CQRS split applied: write use cases separate from query use cases
- [ ] Unit tests for domain (no Spring context), integration tests for adapters

---

## 8. Delivery Format

1. **Package structure** with layer annotations
2. **Domain layer** — entities, value objects, ports
3. **Application layer** — use case implementations, DTOs
4. **Adapter layer** — web controllers, persistence adapters, JPA entities
5. **Config** — Spring bean wiring
6. **Test skeletons** per layer

Load reference files for additional patterns:
- `references/architectures/domain.md` — Aggregates, Value Objects, Domain Events, Specifications
- `references/architectures/application.md` — CQRS, Saga, complex orchestration
- `references/architectures/adapters.md` — Messaging, REST client, cache adapters
- `references/architectures/tests.md` — Unit, integration, and architecture enforcement tests

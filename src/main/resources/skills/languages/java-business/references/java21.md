    # Java 21 — Professional Reference

## Includes all Java 8, 11, and 17 features, plus:

### Virtual Threads
```java
// Executor for high-throughput I/O workloads
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<OrderSummary>> futures = orderIds.stream()
        .map(id -> executor.submit(() -> fetchOrderSummary(id)))
        .collect(Collectors.toList());

    return futures.stream()
        .map(f -> {
            try { return f.get(); }
            catch (Exception e) { throw new RuntimeException(e); }
        })
        .collect(Collectors.toList());
}

// Named virtual thread for debugging
Thread.ofVirtual()
    .name("order-processor-", 0)
    .start(() -> processOrderBatch(batch));
```

### Pattern Matching — Switch
```java
public BigDecimal calculateShipping(PaymentMethod method) {
    return switch (method) {
        case Pix p                              -> BigDecimal.ZERO;
        case CreditCard cc when cc.brand().equals("AMEX") -> new BigDecimal("5.00");
        case CreditCard cc                      -> new BigDecimal("2.50");
        case BankTransfer bt                    -> new BigDecimal("1.00");
    };
}

public String describeShape(Shape shape) {
    return switch (shape) {
        case Circle c    -> "Circle r=%.2f".formatted(c.radius());
        case Rectangle r -> "Rect %dx%d".formatted(r.width(), r.height());
        case null        -> throw new IllegalArgumentException("Shape must not be null");
    };
}
```

### Record Patterns
```java
public BigDecimal extractAmount(Object value) {
    return switch (value) {
        case Money(var amount, var currency) when currency.equals(Currency.getInstance("USD"))
            -> amount;
        case Money(var amount, var currency)
            -> convertToUsd(amount, currency);
        default
            -> throw new IllegalArgumentException("Expected Money, got: " + value.getClass());
    };
}
```

### Sequenced Collections
```java
SequencedCollection<Order> recentOrders = fetchRecentOrders();

Order newest = recentOrders.getFirst();
Order oldest = recentOrders.getLast();

SequencedCollection<Order> reversed = recentOrders.reversed();
```

## pom.xml — Java 21
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

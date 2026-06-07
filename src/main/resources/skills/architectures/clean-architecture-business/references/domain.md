# Domain Layer — Business Reference

## Specification Pattern

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}

public class EligibleForDiscountSpecification implements Specification<Order> {
    private static final int MIN_ITEMS = 3;
    private static final Money MIN_TOTAL = Money.of("100.00", "USD");

    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getItems().size() >= MIN_ITEMS
            && order.getTotal().getAmount().compareTo(MIN_TOTAL.getAmount()) >= 0;
    }
}

// Usage in domain service or use case:
var discountSpec = new EligibleForDiscountSpecification();
if (discountSpec.isSatisfiedBy(order)) {
    order.applyDiscount(DiscountRate.of("10"));
}
```

## Domain Service

Use when logic spans multiple aggregates and doesn't belong to any single one:

```java
public class PricingDomainService {

    public Money calculateFinalPrice(Product product, Customer customer, PromoCode promoCode) {
        var basePrice = product.getPrice();
        var customerDiscount = customer.getLoyaltyTier().discountRate();
        var promoDiscount = promoCode.isValidFor(product) ? promoCode.discountRate() : DiscountRate.ZERO;
        var effectiveDiscount = customerDiscount.max(promoDiscount);
        return basePrice.multiply(effectiveDiscount.complement());
    }
}
```

## Domain Events

```java
public sealed interface DomainEvent
    permits OrderConfirmedEvent, OrderCancelledEvent, OrderShippedEvent {
    Instant occurredOn();
}

public record OrderConfirmedEvent(
    OrderId orderId,
    CustomerId customerId,
    Money total,
    Instant occurredOn
) implements DomainEvent { }

public record OrderCancelledEvent(
    OrderId orderId,
    String reason,
    Instant occurredOn
) implements DomainEvent { }
```

## Policy / Business Rule Objects

```java
public class FreeShippingPolicy {
    private static final Money THRESHOLD = Money.of("50.00", "USD");

    public boolean appliesTo(Order order) {
        return order.getTotal().getAmount().compareTo(THRESHOLD.getAmount()) >= 0;
    }

    public Money shippingCost(Order order) {
        return appliesTo(order) ? Money.zero("USD") : Money.of("5.99", "USD");
    }
}
```

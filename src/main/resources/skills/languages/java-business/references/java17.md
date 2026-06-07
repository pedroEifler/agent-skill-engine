# Java 17 — Professional Reference

## Includes all Java 8 and 11 features, plus:

### Records
```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}
```

### Sealed Classes
```java
public sealed interface PaymentMethod
    permits CreditCard, BankTransfer, Pix { }

public record CreditCard(String last4, String brand) implements PaymentMethod { }
public record BankTransfer(String bankCode, String accountNumber) implements PaymentMethod { }
public record Pix(String key) implements PaymentMethod { }
```

### Pattern Matching — instanceof
```java
public String describeEvent(Object event) {
    if (event instanceof OrderPlaced placed) {
        return "Order placed: " + placed.orderId();
    } else if (event instanceof OrderCancelled cancelled) {
        return "Order cancelled: " + cancelled.reason();
    }
    return "Unknown event";
}
```

### Text Blocks
```java
private static final String SQL_FIND_ACTIVE = """
        SELECT o.id, o.total, c.name
        FROM orders o
        JOIN customers c ON c.id = o.customer_id
        WHERE o.status = 'CONFIRMED'
          AND o.created_at >= :since
        ORDER BY o.created_at DESC
        """;

private static final String ERROR_RESPONSE_TEMPLATE = """
        {
            "error": "%s",
            "message": "%s",
            "timestamp": "%s"
        }
        """;
```

## pom.xml — Java 17
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

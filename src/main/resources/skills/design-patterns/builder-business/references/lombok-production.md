# Lombok Builder — Production Reference

## Jackson Compatibility

```java
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = PaymentRequest.PaymentRequestBuilder.class)
public class PaymentRequest {

    @NonNull UUID orderId;
    @NonNull BigDecimal amount;
    @NonNull String currency;

    @Builder.Default
    PaymentMethod method = PaymentMethod.CREDIT_CARD;

    @Builder.Default
    boolean capture = true;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class PaymentRequestBuilder {}
}
```

## Lombok + Custom Validation in build()

```java
@Getter
@Builder
public final class JwtConfig {

    @NonNull private final String secret;
    @NonNull private final String issuer;

    @Builder.Default private final Duration accessTokenTtl  = Duration.ofMinutes(15);
    @Builder.Default private final Duration refreshTokenTtl = Duration.ofDays(7);
    @Builder.Default private final String algorithm         = "HS256";

    public static final class JwtConfigBuilder {
        public JwtConfig build() {
            if (secret != null && secret.length() < 32) {
                throw new IllegalArgumentException(
                    "JWT secret must be at least 32 characters for HS256");
            }
            if (accessTokenTtl != null && refreshTokenTtl != null
                    && accessTokenTtl.compareTo(refreshTokenTtl) >= 0) {
                throw new IllegalArgumentException(
                    "accessTokenTtl must be shorter than refreshTokenTtl");
            }
            return new JwtConfig(secret, issuer, accessTokenTtl, refreshTokenTtl, algorithm);
        }
    }
}
```

## @SuperBuilder with Abstract Base

```java
@Getter
@SuperBuilder(toBuilder = true)
public abstract class BaseCommand {
    @NonNull private final UUID commandId;
    @NonNull private final UUID correlationId;
    @NonNull private final Instant issuedAt;
    @NonNull private final String issuedBy;
}

@Getter
@SuperBuilder(toBuilder = true)
public final class CreateOrderCommand extends BaseCommand {
    @NonNull private final UUID customerId;
    @NonNull private final List<OrderLineItem> items;

    @Builder.Default
    private final String currency = "USD";
}

// Usage:
var command = CreateOrderCommand.builder()
    .commandId(UUID.randomUUID())
    .correlationId(correlationId)
    .issuedAt(Instant.now())
    .issuedBy(currentUser)
    .customerId(customerId)
    .items(lineItems)
    .build();

// Modify using toBuilder():
var retry = command.toBuilder()
    .commandId(UUID.randomUUID())
    .issuedAt(Instant.now())
    .build();
```

## Lombok with Spring Validation

```java
@Value
@Builder
public class CreateProductRequest {

    @NonNull
    @NotBlank
    @Size(max = 200)
    String name;

    @NonNull
    @NotNull
    @DecimalMin("0.01")
    BigDecimal price;

    @NonNull
    @NotBlank
    @Size(max = 50)
    String sku;

    @Builder.Default
    @Min(0)
    int stockQuantity = 0;

    @Builder.Default
    boolean active = true;
}
```

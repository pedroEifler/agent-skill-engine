# Step Builder Advanced — Business Reference

## Branching Steps — Optional Step Groups

```java
/**
 * Step builder with conditional branches:
 * - Delivery orders require address and method
 * - Pickup orders require store location
 */
public final class ShippingConfig {

    private final String orderId;
    private final ShippingType type;
    private final String address;
    private final ShippingMethod method;
    private final String storeLocation;

    private ShippingConfig(FinalStep b) {
        this.orderId       = b.orderId;
        this.type          = b.type;
        this.address       = b.address;
        this.method        = b.method;
        this.storeLocation = b.storeLocation;
    }

    public static OrderIdStep builder() { return new Steps(); }

    public interface OrderIdStep { TypeStep orderId(String orderId); }
    public interface TypeStep {
        DeliveryStep delivery();
        PickupStep pickup();
    }
    public interface DeliveryStep {
        DeliveryMethodStep to(String address);
    }
    public interface DeliveryMethodStep {
        FinalStep via(ShippingMethod method);
    }
    public interface PickupStep {
        FinalStep atStore(String storeLocation);
    }
    public interface FinalStep {
        ShippingConfig build();
        String orderId    = null;
        ShippingType type = null;
        String address    = null;
        ShippingMethod method = null;
        String storeLocation  = null;
    }

    private static final class Steps
            implements OrderIdStep, TypeStep, DeliveryStep, DeliveryMethodStep, PickupStep, FinalStep {

        private String orderId;
        private ShippingType type;
        private String address;
        private ShippingMethod method;
        private String storeLocation;

        @Override public TypeStep orderId(String orderId) {
            this.orderId = Objects.requireNonNull(orderId);
            return this;
        }

        @Override public DeliveryStep delivery() {
            this.type = ShippingType.DELIVERY;
            return this;
        }

        @Override public PickupStep pickup() {
            this.type = ShippingType.PICKUP;
            return this;
        }

        @Override public DeliveryMethodStep to(String address) {
            this.address = Objects.requireNonNull(address);
            return this;
        }

        @Override public FinalStep via(ShippingMethod method) {
            this.method = Objects.requireNonNull(method);
            return this;
        }

        @Override public FinalStep atStore(String storeLocation) {
            this.storeLocation = Objects.requireNonNull(storeLocation);
            return this;
        }

        @Override public ShippingConfig build() {
            return new ShippingConfig(this);
        }
    }
}

// Usage — compiler guides the caller through the right branch:
var delivery = ShippingConfig.builder()
    .orderId("ORD-001")
    .delivery()
    .to("123 Main St")
    .via(ShippingMethod.EXPRESS)
    .build();

var pickup = ShippingConfig.builder()
    .orderId("ORD-002")
    .pickup()
    .atStore("Downtown Branch")
    .build();
```

## Step Builder with Repeated Optional Steps

```java
public interface QueryBuilder {
    WhereStep from(String table);
}

public interface WhereStep extends BuildStep {
    WhereStep where(String column, Object value);
    WhereStep whereIn(String column, Collection<?> values);
    OrderStep orderBy(String column);
}

public interface OrderStep extends BuildStep {
    OrderStep thenBy(String column, SortDirection direction);
    LimitStep limit(int count);
}

public interface LimitStep extends BuildStep {
    LimitStep offset(int count);
}

public interface BuildStep {
    String build();
}
```

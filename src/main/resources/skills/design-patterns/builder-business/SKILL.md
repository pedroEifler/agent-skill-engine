---
name: builder-pattern-business
description: >
  Use this skill whenever a professional developer requests production-grade Builder pattern
  implementations, immutable value objects, fluent APIs, step builders, domain-specific language
  builders, or any object construction strategy that enforces validity and readability in Java.
  Triggers include: "builder pattern", "fluent builder", "step builder", "immutable object builder",
  "telescoping constructor", "effective java builder", "builder with validation", "domain builder",
  "builder with inheritance", "Lombok @Builder", "@SuperBuilder", "toBuilder", "builder interface
  segregation", "staged builder", "mandatory fields builder", "DSL builder java", "query builder
  pattern", "test data builder". Produces clean, production-ready English code with no inline comments,
  full JavaDoc, immutable results, compile-time mandatory field enforcement (step builder), validation
  at build time, and Lombok integration where appropriate. ALWAYS use this skill for any professional
  Builder pattern request — prefer step builders when mandatory fields must be enforced at compile time.
---

# Skill: Builder Pattern Business

Generates production-grade Java Builder implementations. Clean English code, no inline comments,
immutable results, compile-time or runtime validation, Lombok integration, and test data builders.

---

## 1. Variant Selection Guide

| Variant              | Enforces mandatory fields | Use when                                                    |
|----------------------|---------------------------|--------------------------------------------------------------|
| Classic fluent       | Runtime (in `build()`)    | Most cases; simple and readable                              |
| Step / Staged        | Compile-time              | All mandatory fields must be set; API guides the caller      |
| Lombok `@Builder`    | Runtime                   | Low-boilerplate; add custom validation in Builder subclass   |
| Lombok `@SuperBuilder` | Runtime                 | Class hierarchies with shared builder fields                 |
| GoF Director         | N/A                       | Multiple representations from same construction steps        |
| Test data builder    | N/A                       | Readable, defaulted test fixtures                            |

---

## 2. Classic Fluent Builder

```java
/**
 * Immutable HTTP request configuration built via a fluent builder.
 *
 * <p>Mandatory fields ({@code url}, {@code method}) are enforced at build time.
 * All other fields have sensible defaults.</p>
 */
public final class HttpRequestConfig {

    private final String url;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxRetries;
    private final boolean followRedirects;
    private final String body;

    private HttpRequestConfig(Builder builder) {
        this.url             = builder.url;
        this.method          = builder.method;
        this.headers         = Map.copyOf(builder.headers);
        this.connectTimeout  = builder.connectTimeout;
        this.readTimeout     = builder.readTimeout;
        this.maxRetries      = builder.maxRetries;
        this.followRedirects = builder.followRedirects;
        this.body            = builder.body;
    }

    /**
     * Returns a new builder for {@link HttpRequestConfig}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder pre-populated with this configuration's values,
     * allowing easy creation of modified copies.
     *
     * @return a builder seeded from this instance
     */
    public Builder toBuilder() {
        return new Builder()
            .url(url)
            .method(method)
            .headers(headers)
            .connectTimeout(connectTimeout)
            .readTimeout(readTimeout)
            .maxRetries(maxRetries)
            .followRedirects(followRedirects)
            .body(body);
    }

    public String getUrl()                { return url; }
    public HttpMethod getMethod()          { return method; }
    public Map<String, String> getHeaders(){ return headers; }
    public Duration getConnectTimeout()    { return connectTimeout; }
    public Duration getReadTimeout()       { return readTimeout; }
    public int getMaxRetries()             { return maxRetries; }
    public boolean isFollowRedirects()     { return followRedirects; }
    public Optional<String> getBody()      { return Optional.ofNullable(body); }

    public static final class Builder {

        private String url;
        private HttpMethod method = HttpMethod.GET;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout    = Duration.ofSeconds(30);
        private int maxRetries          = 0;
        private boolean followRedirects = true;
        private String body;

        private Builder() {}

        public Builder url(String url) {
            this.url = Objects.requireNonNull(url, "url must not be null");
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = Objects.requireNonNull(method, "method must not be null");
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(
                Objects.requireNonNull(name, "header name must not be null"),
                Objects.requireNonNull(value, "header value must not be null"));
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder bearerToken(String token) {
            return header("Authorization", "Bearer " + token);
        }

        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = Objects.requireNonNull(timeout);
            return this;
        }

        public Builder readTimeout(Duration timeout) {
            this.readTimeout = Objects.requireNonNull(timeout);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) throw new IllegalArgumentException("maxRetries cannot be negative");
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * Builds and validates the {@link HttpRequestConfig}.
         *
         * @return the constructed configuration
         * @throws IllegalStateException if mandatory fields are missing or inconsistent
         */
        public HttpRequestConfig build() {
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("url is required");
            }
            if (body != null && method == HttpMethod.GET) {
                throw new IllegalStateException("GET requests cannot have a body");
            }
            return new HttpRequestConfig(this);
        }
    }
}
```

---

## 3. Step Builder — Compile-Time Mandatory Field Enforcement

```java
/**
 * Step builder for {@link EmailMessage} that enforces mandatory fields
 * at compile time via interface segregation.
 *
 * <p>Usage pattern:</p>
 * <pre>{@code
 * EmailMessage email = EmailMessage.builder()
 *     .to("alice@example.com")
 *     .subject("Hello")
 *     .body("Hi there!")
 *     .build();
 * }</pre>
 *
 * The compiler prevents calling {@code build()} before all mandatory
 * fields have been set.
 */
public final class EmailMessage {

    private final String to;
    private final String subject;
    private final String body;
    private final List<String> cc;
    private final List<String> bcc;
    private final boolean html;
    private final Priority priority;

    private EmailMessage(FinalStep builder) {
        this.to       = builder.to;
        this.subject  = builder.subject;
        this.body     = builder.body;
        this.cc       = List.copyOf(builder.cc);
        this.bcc      = List.copyOf(builder.bcc);
        this.html     = builder.html;
        this.priority = builder.priority;
    }

    public static ToStep builder() { return new Steps(); }

    public interface ToStep      { SubjectStep to(String to); }
    public interface SubjectStep { BodyStep subject(String subject); }
    public interface BodyStep    { FinalStep body(String body); }

    public interface FinalStep {
        FinalStep cc(String... recipients);
        FinalStep bcc(String... recipients);
        FinalStep html(boolean html);
        FinalStep priority(Priority priority);
        EmailMessage build();
    }

    private static final class Steps implements ToStep, SubjectStep, BodyStep, FinalStep {

        private String to;
        private String subject;
        private String body;
        private final List<String> cc  = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private boolean html           = false;
        private Priority priority      = Priority.NORMAL;

        @Override public SubjectStep to(String to) {
            this.to = Objects.requireNonNull(to, "to must not be null");
            return this;
        }

        @Override public BodyStep subject(String subject) {
            this.subject = Objects.requireNonNull(subject, "subject must not be null");
            return this;
        }

        @Override public FinalStep body(String body) {
            this.body = Objects.requireNonNull(body, "body must not be null");
            return this;
        }

        @Override public FinalStep cc(String... recipients) {
            cc.addAll(Arrays.asList(recipients));
            return this;
        }

        @Override public FinalStep bcc(String... recipients) {
            bcc.addAll(Arrays.asList(recipients));
            return this;
        }

        @Override public FinalStep html(boolean html) {
            this.html = html;
            return this;
        }

        @Override public FinalStep priority(Priority priority) {
            this.priority = Objects.requireNonNull(priority);
            return this;
        }

        @Override public EmailMessage build() {
            return new EmailMessage(this);
        }
    }

    public String getTo()           { return to; }
    public String getSubject()      { return subject; }
    public String getBody()         { return body; }
    public List<String> getCc()     { return cc; }
    public List<String> getBcc()    { return bcc; }
    public boolean isHtml()         { return html; }
    public Priority getPriority()   { return priority; }

    public enum Priority { LOW, NORMAL, HIGH, CRITICAL }
}
```

---

## 4. Lombok @Builder — Production Setup

```java
@Value
@Builder(toBuilder = true)
public class OrderLineItem {

    @NonNull UUID productId;
    @NonNull String productName;

    @Builder.Default
    int quantity = 1;

    @NonNull BigDecimal unitPrice;

    @Builder.Default
    BigDecimal discount = BigDecimal.ZERO;

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
            .subtract(discount)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public static final class OrderLineItemBuilder {
        public OrderLineItem build() {
            if (quantity != null && quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("unitPrice must be positive");
            }
            return new OrderLineItem(productId, productName, quantity, unitPrice, discount);
        }
    }
}
```

---

## 5. @SuperBuilder — Hierarchy Support

```java
@Getter
@SuperBuilder(toBuilder = true)
public abstract class BaseEvent {
    @NonNull private final UUID eventId;
    @NonNull private final Instant occurredOn;
    @NonNull private final String aggregateType;
    @NonNull private final UUID aggregateId;

    @Builder.Default
    private final int schemaVersion = 1;
}

@Getter
@SuperBuilder(toBuilder = true)
public final class OrderPlacedEvent extends BaseEvent {
    @NonNull private final UUID customerId;
    @NonNull private final Money total;
    @NonNull private final List<OrderLineItem> items;
}

// Usage:
var event = OrderPlacedEvent.builder()
    .eventId(UUID.randomUUID())
    .occurredOn(Instant.now())
    .aggregateType("Order")
    .aggregateId(orderId)
    .customerId(customerId)
    .total(Money.of("150.00", "USD"))
    .items(lineItems)
    .build();
```

---

## 6. GoF Director — Multiple Representations

```java
public interface ReportBuilder {
    ReportBuilder header(String title, String period);
    ReportBuilder summarySection(Map<String, BigDecimal> totals);
    ReportBuilder detailSection(List<ReportRow> rows);
    ReportBuilder footer(String generatedBy);
    byte[] build();
}

@Component
public final class ReportDirector {

    public byte[] buildMonthlySalesReport(ReportBuilder builder,
                                           SalesData data,
                                           String period) {
        return builder
            .header("Monthly Sales Report", period)
            .summarySection(data.totals())
            .detailSection(data.rows())
            .footer("Generated by ReportService at " + Instant.now())
            .build();
    }
}

@Component
public final class PdfReportBuilder implements ReportBuilder {
    private final PdfDocument document = new PdfDocument();

    @Override public ReportBuilder header(String title, String period) {
        document.addTitle(title).addSubtitle(period);
        return this;
    }
    @Override public byte[] build() { return document.toBytes(); }
}

@Component
public final class CsvReportBuilder implements ReportBuilder {
    private final StringBuilder csv = new StringBuilder();

    @Override public ReportBuilder header(String title, String period) {
        csv.append("Report: ").append(title).append(",Period: ").append(period).append("\n");
        return this;
    }
    @Override public byte[] build() { return csv.toString().getBytes(StandardCharsets.UTF_8); }
}
```

---

## 7. Test Data Builder

```java
public final class OrderTestDataBuilder {

    private UUID id              = UUID.randomUUID();
    private UUID customerId      = UUID.randomUUID();
    private OrderStatus status   = OrderStatus.PENDING;
    private List<OrderLineItem> items = new ArrayList<>();
    private Instant createdAt    = Instant.now();

    public static OrderTestDataBuilder anOrder() {
        return new OrderTestDataBuilder();
    }

    public OrderTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public OrderTestDataBuilder withCustomer(UUID customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderTestDataBuilder withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestDataBuilder withItem(OrderLineItem item) {
        this.items.add(item);
        return this;
    }

    public OrderTestDataBuilder withItems(List<OrderLineItem> items) {
        this.items = new ArrayList<>(items);
        return this;
    }

    public OrderTestDataBuilder confirmed() {
        return withStatus(OrderStatus.CONFIRMED);
    }

    public OrderTestDataBuilder cancelled() {
        return withStatus(OrderStatus.CANCELLED);
    }

    public Order build() {
        if (items.isEmpty()) {
            items.add(defaultLineItem());
        }
        return Order.reconstitute(id, customerId, List.copyOf(items), status, createdAt);
    }

    private OrderLineItem defaultLineItem() {
        return OrderLineItem.builder()
            .productId(UUID.randomUUID())
            .productName("Default Product")
            .unitPrice(new BigDecimal("10.00"))
            .build();
    }
}
```

---

## 8. Naming Conventions

```java
// Fluent builders
public static Builder builder() {}           // entry point always builder()
public HttpRequestConfig build() {}          // terminal always build()
public Builder toBuilder() {}                // copy factory always toBuilder()

// Step builder interfaces
public interface ToStep {}                   // Step suffix, verb name (To, Subject, Body)
public interface FinalStep {}                // FinalStep for the optional fields stage

// Test data builders
public static OrderTestDataBuilder anOrder() {}    // an + EntityName
public OrderTestDataBuilder confirmed() {}         // state shortcut: adjective
public OrderTestDataBuilder withItem(...) {}       // optional field: with + FieldName
```

---

## 9. Checklist

- [ ] Correct variant chosen per selection guide (Section 1)
- [ ] Target class is `final` and all fields are `final` (immutable)
- [ ] Constructor is `private` — only the builder creates instances
- [ ] All setter methods return `this` (or the step interface type) for chaining
- [ ] Mandatory fields validated in the setter call, not only at `build()`
- [ ] Cross-field consistency validated in `build()`
- [ ] `toBuilder()` provided when the object will be modified (copy pattern)
- [ ] JavaDoc on the class, `builder()`, `build()`, and `toBuilder()`
- [ ] Test data builder provided alongside the domain builder

---

## 10. Delivery Format

1. **Variant rationale** — which variant and why
2. **Production builder** with full JavaDoc
3. **Usage examples** showing mandatory-only, partial, and full construction
4. **Test data builder** for the same domain object
5. **Test skeletons** covering happy path, defaults, and validation failures

Load reference files:
- `references/design-patterns/step-builder-advanced.md` — Branching steps, optional step groups
- `references/design-patterns/lombok-production.md` — @Builder customization, @SuperBuilder, Jackson compat
- `references/design-patterns/tests.md` — Test data builders, parameterized construction tests

---
name: factory-pattern-business
description: >
  Use this skill whenever a professional developer requests production-grade implementations of the
  Factory design pattern (Simple Factory, Factory Method, Abstract Factory), object creation
  strategies, plugin architectures, or registry-based instantiation in Java. Triggers include:
  "factory pattern", "factory method pattern", "abstract factory pattern", "creational design pattern",
  "decouple object creation", "plugin architecture Java", "registry pattern", "static factory methods",
  "Spring factory bean", "polymorphic object creation", "builder vs factory", "dependency injection
  factory", "strategy resolver", "type-safe factory enterprise". Produces clean, production-ready English
  code with no inline comments, full JavaDoc, type-safe enum-based dispatch, Spring-integrated registries,
  and extensible plugin-style factories that follow the Open/Closed Principle. ALWAYS use this skill for
  any professional Factory pattern task, even when the request only mentions one product type — design
  for extensibility from the start.
---

# Skill: Factory Pattern Business

Generates production-grade Java implementations of Factory pattern variants. Clean English code,
no inline comments, full JavaDoc, type-safe and extensible by design (Open/Closed Principle).

---

## 1. Pattern Selection Guide

| Variant          | Use when                                                                 |
|------------------|---------------------------------------------------------------------------|
| Simple Factory   | Small, stable set of types; centralized creation logic acceptable        |
| Factory Method   | Subclasses must customize one step of an algorithm (Template Method)     |
| Abstract Factory | Multiple families of related objects must be created consistently        |
| Registry Factory | Open set of types resolved by key, ideally Spring-managed, no switch     |

---

## 2. Simple Factory — Enum-Based Dispatch

```java
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

public final class PaymentProcessorFactory {

    private PaymentProcessorFactory() {}

    /**
     * Creates the appropriate {@link PaymentProcessor} for the given payment method.
     *
     * @param method the payment method; must not be null
     * @return the corresponding processor implementation
     * @throws UnsupportedPaymentMethodException if no processor exists for the method
     */
    public static PaymentProcessor create(PaymentMethod method) {
        return switch (method) {
            case CREDIT_CARD -> new CreditCardPaymentProcessor();
            case PIX -> new PixPaymentProcessor();
            case BOLETO -> new BoletoPaymentProcessor();
            case BANK_TRANSFER -> throw new UnsupportedPaymentMethodException(method);
        };
    }
}

public enum PaymentMethod {
    CREDIT_CARD, PIX, BOLETO, BANK_TRANSFER
}
```

> Limitation: every new `PaymentMethod` requires editing this factory, violating
> the Open/Closed Principle. Use the Registry Factory (Section 5) for open extensibility.

---

## 3. Factory Method — Template Method Integration

```java
public abstract class ReportGenerator {

    /**
     * Generates the report using the format-specific renderer created
     * by {@link #createRenderer()}.
     *
     * @param data the report data; must not be null
     * @return the generated report as a byte array
     */
    public final byte[] generate(ReportData data) {
        var renderer = createRenderer();
        var formatted = renderer.render(data);
        return renderer.toBytes(formatted);
    }

    /**
     * Factory method implemented by subclasses to provide the format-specific renderer.
     *
     * @return a renderer for this report's output format
     */
    protected abstract ReportRenderer createRenderer();
}

public interface ReportRenderer {
    FormattedReport render(ReportData data);
    byte[] toBytes(FormattedReport report);
}

public final class PdfReportGenerator extends ReportGenerator {
    @Override
    protected ReportRenderer createRenderer() {
        return new PdfReportRenderer();
    }
}

public final class ExcelReportGenerator extends ReportGenerator {
    @Override
    protected ReportRenderer createRenderer() {
        return new ExcelReportRenderer();
    }
}
```

---

## 4. Abstract Factory — Related Object Families

```java
public interface DatabaseConnectionFactory {
    Connection createConnection();
    QueryBuilder createQueryBuilder();
    SchemaValidator createSchemaValidator();
}

public final class PostgresConnectionFactory implements DatabaseConnectionFactory {

    private final DataSource dataSource;

    public PostgresConnectionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection createConnection() {
        return new PostgresConnection(dataSource);
    }

    @Override
    public QueryBuilder createQueryBuilder() {
        return new PostgresQueryBuilder();
    }

    @Override
    public SchemaValidator createSchemaValidator() {
        return new PostgresSchemaValidator();
    }
}

public final class MySqlConnectionFactory implements DatabaseConnectionFactory {

    private final DataSource dataSource;

    public MySqlConnectionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection createConnection() {
        return new MySqlConnection(dataSource);
    }

    @Override
    public QueryBuilder createQueryBuilder() {
        return new MySqlQueryBuilder();
    }

    @Override
    public SchemaValidator createSchemaValidator() {
        return new MySqlSchemaValidator();
    }
}
```

```java
/**
 * Consumes an entire family of related objects via a single abstraction.
 * The client never references concrete database-specific classes.
 */
public final class MigrationRunner {

    private final Connection connection;
    private final QueryBuilder queryBuilder;
    private final SchemaValidator validator;

    public MigrationRunner(DatabaseConnectionFactory factory) {
        this.connection = factory.createConnection();
        this.queryBuilder = factory.createQueryBuilder();
        this.validator = factory.createSchemaValidator();
    }

    public void run(List<Migration> migrations) {
        validator.validateCurrentSchema();
        migrations.forEach(m -> connection.execute(queryBuilder.build(m)));
    }
}
```

---

## 5. Registry Factory — Open for Extension (Recommended for Spring)

```java
public interface PaymentProcessor {
    PaymentMethod supportedMethod();
    PaymentResult process(PaymentRequest request);
}

@Component
public final class CreditCardPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod supportedMethod() { return PaymentMethod.CREDIT_CARD; }

    @Override
    public PaymentResult process(PaymentRequest request) {
        return PaymentResult.success("Processed via credit card");
    }
}

@Component
public final class PixPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod supportedMethod() { return PaymentMethod.PIX; }

    @Override
    public PaymentResult process(PaymentRequest request) {
        return PaymentResult.success("Processed via PIX");
    }
}
```

```java
/**
 * Resolves the {@link PaymentProcessor} for a given {@link PaymentMethod}.
 *
 * <p>New processors are registered automatically via Spring component scanning.
 * Adding a new payment method requires only a new {@code @Component}
 * implementing {@link PaymentProcessor} — this class never changes.</p>
 */
@Component
public final class PaymentProcessorRegistry {

    private final Map<PaymentMethod, PaymentProcessor> processors;

    public PaymentProcessorRegistry(List<PaymentProcessor> availableProcessors) {
        this.processors = availableProcessors.stream()
            .collect(Collectors.toUnmodifiableMap(
                PaymentProcessor::supportedMethod,
                Function.identity()
            ));
    }

    /**
     * Resolves the processor for the given payment method.
     *
     * @param method the payment method; must not be null
     * @return the registered processor
     * @throws UnsupportedPaymentMethodException if no processor is registered for the method
     */
    public PaymentProcessor resolve(PaymentMethod method) {
        var processor = processors.get(method);
        if (processor == null) {
            throw new UnsupportedPaymentMethodException(method);
        }
        return processor;
    }
}
```

---

## 6. Static Factory Methods (Effective Java Item 1)

```java
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        validate(amount);
        return new Money(amount.setScale(2, RoundingMode.HALF_UP), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money fromCents(long cents, Currency currency) {
        return new Money(BigDecimal.valueOf(cents, 2), currency);
    }

    private static void validate(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
}
```

---

## 7. Plugin-Style Factory with SPI (ServiceLoader)

```java
public interface ExportFormatPlugin {
    String formatName();
    DocumentExporter createExporter();
}

public final class ExportFormatRegistry {

    private final Map<String, ExportFormatPlugin> plugins;

    public ExportFormatRegistry() {
        this.plugins = ServiceLoader.load(ExportFormatPlugin.class).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toUnmodifiableMap(
                p -> p.formatName().toLowerCase(),
                Function.identity()
            ));
    }

    public DocumentExporter exporterFor(String format) {
        var plugin = plugins.get(format.toLowerCase());
        if (plugin == null) {
            throw new UnsupportedExportFormatException(format);
        }
        return plugin.createExporter();
    }
}
```

```
src/main/resources/META-INF/services/com.company.export.ExportFormatPlugin
    com.company.export.pdf.PdfExportPlugin
    com.company.export.csv.CsvExportPlugin
```

---

## 8. Naming Conventions

```java
// Simple/static factories
public final class PaymentProcessorFactory {}   // noun + Factory
public static Money of(...) {}                  // static factory: of/from/valueOf

// Factory Method
public abstract class ReportGenerator {}         // abstract base
protected abstract ReportRenderer createRenderer(); // create + Noun

// Abstract Factory
public interface DatabaseConnectionFactory {}    // noun + Factory
public final class PostgresConnectionFactory implements DatabaseConnectionFactory {} // Variant + Factory

// Registry
public final class PaymentProcessorRegistry {}   // noun + Registry
public PaymentProcessor resolve(PaymentMethod method) {} // resolve, not create, for registry lookups

// Plugin SPI
public interface ExportFormatPlugin {}           // noun + Plugin
```

---

## 9. Checklist

- [ ] Chosen variant matches the selection guide (Section 1)
- [ ] Product interface defines the contract; concrete classes implement it
- [ ] Enum (not String) used for type discrimination where applicable
- [ ] Registry/SPI variant used when the set of types is expected to grow (Open/Closed)
- [ ] Static factory methods named descriptively (`of`, `from`, `zero`, `empty`)
- [ ] Constructors private where static factories enforce invariants
- [ ] JavaDoc on all public factory methods including `@throws` for unsupported types
- [ ] Unit tests cover every registered/created variant plus the unsupported-type case

---

## 10. Delivery Format

1. **Pattern selection rationale** — which variant and why
2. **Product interface** and concrete implementations
3. **Factory implementation** (simple, method, abstract, or registry)
4. **Client usage example**
5. **Test skeletons** covering each branch and the failure case

Load reference files:
- `references/spring-integration.md` — Registry factories with Spring DI, conditional beans
- `references/advanced-patterns.md` — Builder+Factory combination, prototype-based factories
- `references/tests.md` — Parameterized tests, mutation-resistant factory tests

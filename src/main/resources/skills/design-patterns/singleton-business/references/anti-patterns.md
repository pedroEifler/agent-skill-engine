# Singleton Anti-Patterns & Migration to DI — Business Reference

## When NOT to Use Singleton

```
❌ Avoid Singleton when:
  - The class holds per-request or per-user state
  - You need more than one instance in tests
  - The class has heavyweight initialization that may not always be needed
  - The class depends on other singletons (creates hidden coupling)
  - You are in a Spring application (prefer @Component + DI)

✅ Singleton is appropriate when:
  - The resource is genuinely shared across all threads/requests (connection pool, config)
  - Object creation is expensive and must happen exactly once
  - You are outside a DI container (pure Java SE, library code)
  - The Enum variant applies and you need Reflection/serialization safety
```

## Anti-Pattern: Singleton as Global State Container

```java
// ❌ Anti-pattern: abusing Singleton as a service locator
public enum ServiceLocator {
    INSTANCE;
    private final Map<Class<?>, Object> services = new HashMap<>();
    public <T> void register(Class<T> type, T impl) { services.put(type, impl); }
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) { return (T) services.get(type); }
}

// Usage (bad): hides dependencies, makes testing painful
public class OrderService {
    public void place(Order order) {
        ServiceLocator.INSTANCE.get(PaymentProcessor.class).process(order);
        ServiceLocator.INSTANCE.get(InventoryPort.class).reserve(order.items());
    }
}
```

```java
// ✅ Prefer explicit dependency injection
@Service
@RequiredArgsConstructor
public class OrderService {
    private final PaymentProcessor paymentProcessor;
    private final InventoryPort inventory;

    public void place(Order order) {
        paymentProcessor.process(order);
        inventory.reserve(order.items());
    }
}
```

## Migrating a Legacy Singleton to Spring DI

```java
// Step 1: extract an interface
public interface ConfigurationPort {
    String get(String key);
    String getOrDefault(String key, String defaultValue);
}

// Step 2: make the legacy singleton implement it (backwards-compatible)
public final class ApplicationConfig implements ConfigurationPort {
    private static final ApplicationConfig INSTANCE = new ApplicationConfig();
    public static ApplicationConfig getInstance() { return INSTANCE; }

    @Override public String get(String key) { return properties.getProperty(key); }
    @Override public String getOrDefault(String key, String def) {
        return properties.getProperty(key, def);
    }
}

// Step 3: Spring wrapper bean — new code injects the interface, not the singleton
@Configuration
public class LegacyMigrationConfig {

    @Bean
    public ConfigurationPort configurationPort() {
        return ApplicationConfig.getInstance();
    }
}

// Step 4 (future): replace the bean with a full Spring implementation
@Component
public final class SpringApplicationConfig implements ConfigurationPort {

    @Value("${db.url:jdbc:h2:mem:default}")
    private String dbUrl;

    @Override
    public String get(String key) { return environment.getProperty(key); }
    @Override
    public String getOrDefault(String key, String def) {
        return environment.getProperty(key, def);
    }

    @Autowired
    private Environment environment;
}
```

## Singleton Scope Pitfall in Spring — Injecting Prototype into Singleton

```java
// ❌ Pitfall: injecting a prototype-scoped bean into a singleton
// The prototype is created ONCE and cached inside the singleton — not per call!
@Service
public class ReportService {
    @Autowired
    private ReportContext context; // @Scope("prototype") but injected once!
}
```

```java
// ✅ Solution 1: inject ObjectFactory to get a new instance per call
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ObjectFactory<ReportContext> contextFactory;

    public Report generate(ReportRequest request) {
        var context = contextFactory.getObject(); // new instance per call
        return context.build(request);
    }
}

// ✅ Solution 2: use @Lookup (proxy-based)
@Service
public abstract class ReportService {
    public Report generate(ReportRequest request) {
        var context = createReportContext();
        return context.build(request);
    }

    @Lookup
    protected abstract ReportContext createReportContext();
}
```

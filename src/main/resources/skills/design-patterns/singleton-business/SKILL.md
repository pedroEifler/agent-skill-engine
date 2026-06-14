---
name: singleton-pattern-business
description: >
  Use this skill whenever a professional developer requests production-grade Singleton pattern
  implementations, thread-safe single-instance resource management, registry initialization,
  configuration holders, connection pool managers, or any scenario requiring controlled single-instance
  lifecycle in Java. Triggers include: "singleton pattern", "thread-safe singleton", "enum singleton",
  "lazy initialization singleton", "Bill Pugh singleton", "holder pattern", "double-checked locking",
  "single instance resource", "application-wide shared state", "singleton vs dependency injection",
  "Spring singleton scope", "singleton connection pool", "singleton configuration", "singleton registry",
  "volatile singleton java", "singleton serialization safe". Produces clean, production-ready English code
  with no inline comments, full JavaDoc, thread-safe implementations, proper protection against Reflection
  and serialization attacks, testability guidance, and clear rationale for the chosen variant. ALWAYS use
  this skill for any professional Singleton pattern request — and always assess whether DI is preferable.
---

# Skill: Singleton Pattern Business

Generates production-grade Java Singleton implementations. Clean English code, no inline comments,
thread-safe by default, protected against Reflection and serialization, testable by design.

---

## 1. Variant Selection Guide

| Variant                    | Thread-safe | Lazy | Use when                                              |
|----------------------------|-------------|------|-------------------------------------------------------|
| Eager (static final)       | ✅          | ❌   | Lightweight, always used, no circular deps            |
| Enum                       | ✅          | ❌   | Best default — serialization & Reflection proof       |
| Holder (Bill Pugh)         | ✅          | ✅   | Lazy + no synchronization overhead                    |
| Double-Checked Locking     | ✅          | ✅   | Must inherit; `volatile` mandatory                    |
| Spring `@Component`        | ✅          | ✅   | Any Spring application — prefer over manual           |

> **Default recommendation**: Enum for SE/standalone; Spring `@Component` for Spring apps.

---

## 2. Eager Singleton

```java
/**
 * Eager singleton for lightweight, always-used shared resources.
 * JVM class-loading guarantees atomic initialization.
 *
 * <p>Prefer {@link #getInstance()} over direct field access for potential
 * future subclassing or interception.</p>
 */
public final class ApplicationConfig {

    private static final ApplicationConfig INSTANCE = new ApplicationConfig();

    private final Properties properties;

    private ApplicationConfig() {
        this.properties = loadProperties();
    }

    /**
     * Returns the single application-wide configuration instance.
     *
     * @return the singleton instance; never null
     */
    public static ApplicationConfig getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves a configuration value by key.
     *
     * @param key the property key; must not be null
     * @return the configured value, or {@code null} if not present
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private Properties loadProperties() {
        var props = new Properties();
        try (var stream = getClass().getResourceAsStream("/application.properties")) {
            if (stream != null) props.load(stream);
        } catch (IOException e) {
            throw new ConfigurationLoadException("Failed to load application.properties", e);
        }
        return props;
    }
}
```

---

## 3. Enum Singleton (Recommended Default)

```java
/**
 * Enum-based singleton for application-wide metrics collection.
 *
 * <p>Enum singletons are immune to Reflection-based instantiation and
 * serialization attacks. Prefer this variant unless inheritance is required.</p>
 */
public enum MetricsCollector {

    INSTANCE;

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gauges   = new ConcurrentHashMap<>();

    /**
     * Increments a named counter by one.
     *
     * @param name the counter name; must not be null
     */
    public void increment(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
    }

    /**
     * Increments a named counter by the given delta.
     *
     * @param name  the counter name; must not be null
     * @param delta the amount to add; may be negative
     */
    public void increment(String name, long delta) {
        counters.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(delta);
    }

    /**
     * Sets a gauge value.
     *
     * @param name  the gauge name; must not be null
     * @param value the current value
     */
    public void gauge(String name, long value) {
        gauges.computeIfAbsent(name, k -> new AtomicLong()).set(value);
    }

    /**
     * Returns a snapshot of all collected metrics.
     *
     * @return an unmodifiable view of the current metrics
     */
    public Map<String, Long> snapshot() {
        var snapshot = new LinkedHashMap<String, Long>();
        counters.forEach((k, v) -> snapshot.put("counter." + k, v.get()));
        gauges.forEach((k, v) -> snapshot.put("gauge." + k, v.get()));
        return Collections.unmodifiableMap(snapshot);
    }

    public void reset() {
        counters.clear();
        gauges.clear();
    }
}
```

---

## 4. Holder (Bill Pugh) Singleton

```java
/**
 * Lazy-initialized singleton using the initialization-on-demand holder idiom.
 *
 * <p>The {@code Holder} inner class is not loaded until {@link #getInstance()}
 * is called, providing lazy initialization without synchronization overhead.
 * JVM class-loading guarantees thread-safe initialization.</p>
 */
public final class ConnectionPool {

    private final int maxSize;
    private final Duration acquireTimeout;
    private final BlockingDeque<Connection> pool;

    private ConnectionPool() {
        this.maxSize = Integer.parseInt(ApplicationConfig.getInstance().getOrDefault("pool.size", "10"));
        this.acquireTimeout = Duration.ofSeconds(
            Long.parseLong(ApplicationConfig.getInstance().getOrDefault("pool.timeout.seconds", "30")));
        this.pool = new LinkedBlockingDeque<>(maxSize);
        initializePool();
    }

    private static final class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }

    /**
     * Returns the application-wide connection pool instance.
     * Initialized lazily on first call; subsequent calls return the cached instance.
     *
     * @return the singleton connection pool; never null
     */
    public static ConnectionPool getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Acquires a connection from the pool, waiting up to the configured timeout.
     *
     * @return a pooled connection
     * @throws ConnectionAcquisitionException if no connection becomes available within the timeout
     */
    public Connection acquire() {
        try {
            var connection = pool.pollFirst(acquireTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (connection == null) {
                throw new ConnectionAcquisitionException("No connection available after " + acquireTimeout);
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionAcquisitionException("Interrupted while waiting for connection", e);
        }
    }

    /**
     * Returns a connection to the pool.
     *
     * @param connection the connection to release; must not be null
     */
    public void release(Connection connection) {
        Objects.requireNonNull(connection, "connection must not be null");
        pool.offerLast(connection);
    }

    public int availableConnections() {
        return pool.size();
    }

    private void initializePool() {
        for (int i = 0; i < maxSize; i++) {
            pool.offer(createConnection(i));
        }
    }

    private Connection createConnection(int index) {
        return new PooledConnection("conn-" + index);
    }
}
```

---

## 5. Double-Checked Locking

```java
/**
 * Thread-safe lazy singleton using double-checked locking.
 *
 * <p>Use this variant only when the class must extend another class
 * (ruling out enum) and lazy initialization is required.
 * The {@code volatile} keyword is mandatory to prevent instruction
 * reordering by the JVM and hardware memory models.</p>
 */
public final class CacheManager {

    private static volatile CacheManager instance;

    private final Map<String, CacheEntry<?>> store = new ConcurrentHashMap<>();
    private final Duration defaultTtl;

    private CacheManager() {
        this.defaultTtl = Duration.ofMinutes(10);
    }

    /**
     * Returns the singleton cache manager instance.
     * Initialized on first call; subsequent calls incur no synchronization cost.
     *
     * @return the singleton instance; never null
     */
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    /**
     * Stores a value under the given key with the default TTL.
     *
     * @param key   the cache key; must not be null
     * @param value the value to cache; must not be null
     * @param <T>   the type of the cached value
     */
    public <T> void put(String key, T value) {
        put(key, value, defaultTtl);
    }

    /**
     * Stores a value under the given key with a custom TTL.
     *
     * @param key   the cache key; must not be null
     * @param value the value to cache; must not be null
     * @param ttl   time-to-live for this entry; must be positive
     * @param <T>   the type of the cached value
     */
    public <T> void put(String key, T value, Duration ttl) {
        store.put(key, new CacheEntry<>(value, Instant.now().plus(ttl)));
    }

    /**
     * Retrieves a cached value if present and not expired.
     *
     * @param key  the cache key
     * @param type the expected type
     * @param <T>  the expected type
     * @return an {@link Optional} containing the value, or empty if absent or expired
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        var entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of((T) entry.value());
    }

    public void evict(String key) {
        store.remove(key);
    }

    public void clear() {
        store.clear();
    }

    private record CacheEntry<T>(T value, Instant expiresAt) {
        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }
}
```

---

## 6. Reflection & Serialization Defense

```java
public final class SecureSingleton implements Serializable {

    private static final long serialVersionUID = 1L;
    private static volatile boolean instantiated = false;

    private static final SecureSingleton INSTANCE = new SecureSingleton();

    private SecureSingleton() {
        if (instantiated) {
            throw new IllegalStateException(
                "Singleton already instantiated. Use getInstance().");
        }
        instantiated = true;
    }

    public static SecureSingleton getInstance() {
        return INSTANCE;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
```

---

## 7. Spring Singleton Scope

```java
@Configuration
public class InfrastructureConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MetricsRegistry metricsRegistry(MeterRegistry meterRegistry) {
        return new MicrometerMetricsRegistry(meterRegistry);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        var redisCacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)))
            .build();
        return redisCacheManager;
    }
}

@Component
public final class FeatureFlagRegistry {

    private final Map<String, Boolean> flags = new ConcurrentHashMap<>();
    private final FeatureFlagRepository repository;

    public FeatureFlagRegistry(FeatureFlagRepository repository) {
        this.repository = repository;
        refresh();
    }

    public boolean isEnabled(String flagName) {
        return flags.getOrDefault(flagName, false);
    }

    @Scheduled(fixedDelay = 60_000)
    public void refresh() {
        repository.findAll().forEach(f -> flags.put(f.getName(), f.isEnabled()));
    }
}
```

---

## 8. Naming Conventions

```java
// Enum singletons
public enum MetricsCollector { INSTANCE; }      // SCREAMING_SNAKE_CASE instance name

// Class-based singletons
public final class ConnectionPool {}            // final, no inheritance
private static final ConnectionPool INSTANCE;   // private static final field
public static ConnectionPool getInstance() {}   // get + ClassName

// Spring
@Component public final class FeatureFlagRegistry {} // Spring manages lifecycle
```

---

## 9. Checklist

- [ ] Correct variant chosen per selection guide (Section 1)
- [ ] Constructor is `private` in all non-enum variants
- [ ] Class is `final` (prevents subclassing that could bypass Singleton)
- [ ] `volatile` present on DCL field
- [ ] All mutable shared state uses `AtomicLong`, `AtomicReference`, or `ConcurrentHashMap`
- [ ] `readResolve()` implemented if class implements `Serializable`
- [ ] Reflection guard in constructor if security is a concern
- [ ] JavaDoc on `getInstance()` documents thread-safety and lazy behavior
- [ ] Dependency injection considered as an alternative for better testability

---

## 10. Delivery Format

1. **Variant rationale** — which variant and why for this use case
2. **Production implementation** with full JavaDoc
3. **Thread-safety proof** (which JVM guarantee makes it safe)
4. **Test skeletons** — identity test, concurrency test, state isolation
5. **DI alternative** — when Spring or constructor injection is preferable

Load reference files:
- `references/design-patterns/thread-safety.md` — AtomicReference, StampedLock, concurrent state patterns
- `references/design-patterns/testing.md` — Concurrency tests, state reset, testability patterns
- `references/design-patterns/anti-patterns.md` — When not to use Singleton, migration to DI

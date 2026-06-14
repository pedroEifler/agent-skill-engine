# Thread Safety in Singletons — Business Reference

## AtomicReference for Lazy Initialization Without Synchronization

```java
public final class ConfigurationHolder {

    private static final AtomicReference<ConfigurationHolder> INSTANCE_REF =
        new AtomicReference<>();

    private final Map<String, String> config;

    private ConfigurationHolder(Map<String, String> config) {
        this.config = Map.copyOf(config);
    }

    public static ConfigurationHolder getInstance() {
        var existing = INSTANCE_REF.get();
        if (existing != null) return existing;

        var created = new ConfigurationHolder(loadConfig());
        return INSTANCE_REF.compareAndSet(null, created)
            ? created
            : INSTANCE_REF.get();
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(config.get(key));
    }

    private static Map<String, String> loadConfig() {
        return Map.of(
            "db.url", System.getenv().getOrDefault("DB_URL", "jdbc:h2:mem:default"),
            "pool.size", System.getenv().getOrDefault("POOL_SIZE", "10")
        );
    }
}
```

## StampedLock for Read-Heavy Singleton State

```java
public enum RateLimiterRegistry {

    INSTANCE;

    private final Map<String, RateLimiter> limiters = new HashMap<>();
    private final StampedLock lock = new StampedLock();

    /**
     * Retrieves or creates a rate limiter for the given key.
     * Uses optimistic read for the common case; falls back to write lock only on miss.
     */
    public RateLimiter getOrCreate(String key, int maxRequestsPerSecond) {
        long stamp = lock.tryOptimisticRead();
        RateLimiter limiter = limiters.get(key);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                limiter = limiters.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        if (limiter != null) return limiter;

        stamp = lock.writeLock();
        try {
            return limiters.computeIfAbsent(key, k ->
                RateLimiter.create(maxRequestsPerSecond));
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void remove(String key) {
        long stamp = lock.writeLock();
        try {
            limiters.remove(key);
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
```

## Safe Lazy Initialization with CompletableFuture

```java
public final class HeavyResourceSingleton {

    private static final class Holder {
        private static final CompletableFuture<HeavyResourceSingleton> FUTURE =
            CompletableFuture.supplyAsync(HeavyResourceSingleton::initialize);
    }

    private final Object resource;

    private HeavyResourceSingleton(Object resource) {
        this.resource = resource;
    }

    private static HeavyResourceSingleton initialize() {
        try {
            Thread.sleep(100);
            return new HeavyResourceSingleton(new Object());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InitializationException("Interrupted during initialization", e);
        }
    }

    /**
     * Returns the singleton instance, blocking until initialization completes.
     *
     * @return the initialized instance
     * @throws InitializationException if initialization failed
     */
    public static HeavyResourceSingleton getInstance() {
        try {
            return Holder.FUTURE.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InitializationException("Interrupted waiting for singleton", e);
        } catch (ExecutionException e) {
            throw new InitializationException("Singleton initialization failed", e.getCause());
        }
    }
}
```

## Concurrent Singleton State Patterns

```java
public enum EventBus {

    INSTANCE;

    private final Map<Class<?>, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add((Consumer<Object>) listener);
    }

    public <T> void publish(T event) {
        var handlers = listeners.getOrDefault(event.getClass(), List.of());
        handlers.forEach(handler -> executor.submit(() -> handler.accept(event)));
    }

    public void unsubscribeAll(Class<?> eventType) {
        listeners.remove(eventType);
    }
}
```

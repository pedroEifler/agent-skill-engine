# Testing Singleton Patterns — Business Reference

## Identity Test

```java
class ConnectionPoolTest {

    @Test
    void getInstance_shouldAlwaysReturnSameReference() {
        var first  = ConnectionPool.getInstance();
        var second = ConnectionPool.getInstance();
        assertThat(first).isSameAs(second);
    }
}
```

## Concurrency Test — Ensuring Single Instantiation

```java
class SingletonConcurrencyTest {

    @Test
    void getInstance_shouldReturnSingleInstanceUnderHighConcurrency()
            throws InterruptedException {
        int threadCount = 200;
        var latch    = new CountDownLatch(1);
        var barrier  = new CyclicBarrier(threadCount);
        var captured = Collections.newSetFromMap(new ConcurrentHashMap<>());
        var errors   = new CopyOnWriteArrayList<Throwable>();

        var threads = IntStream.range(0, threadCount)
            .mapToObj(i -> new Thread(() -> {
                try {
                    barrier.await();
                    captured.add(ConnectionPool.getInstance());
                } catch (Exception e) {
                    errors.add(e);
                }
            }))
            .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.countDown();
        for (var t : threads) t.join(5000);

        assertThat(errors).isEmpty();
        assertThat(captured).hasSize(1);
    }
}
```

## State Isolation Between Tests

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MetricsCollectorTest {

    @BeforeEach
    void resetState() {
        MetricsCollector.INSTANCE.reset();
    }

    @Test
    @Order(1)
    void increment_shouldAccumulateCount() {
        MetricsCollector.INSTANCE.increment("requests");
        MetricsCollector.INSTANCE.increment("requests");
        assertThat(MetricsCollector.INSTANCE.snapshot())
            .containsEntry("counter.requests", 2L);
    }

    @Test
    @Order(2)
    void gauge_shouldRecordLatestValue() {
        MetricsCollector.INSTANCE.gauge("active.connections", 5L);
        MetricsCollector.INSTANCE.gauge("active.connections", 8L);
        assertThat(MetricsCollector.INSTANCE.snapshot())
            .containsEntry("gauge.active.connections", 8L);
    }
}
```

## Testability via Dependency Injection Wrapper

```java
public interface MetricsPort {
    void increment(String name);
    void increment(String name, long delta);
    Map<String, Long> snapshot();
}

@Component
public final class EnumMetricsAdapter implements MetricsPort {

    @Override
    public void increment(String name) {
        MetricsCollector.INSTANCE.increment(name);
    }

    @Override
    public void increment(String name, long delta) {
        MetricsCollector.INSTANCE.increment(name, delta);
    }

    @Override
    public Map<String, Long> snapshot() {
        return MetricsCollector.INSTANCE.snapshot();
    }
}

// In service code — inject the interface, not the enum:
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MetricsPort metrics;

    public Order placeOrder(PlaceOrderCommand cmd) {
        var order = buildOrder(cmd);
        metrics.increment("orders.placed");
        return orderRepository.save(order);
    }
}

// In tests — inject a mock of the interface:
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock MetricsPort metrics;
    @InjectMocks OrderService orderService;

    @Test
    void placeOrder_shouldIncrementOrdersPlacedMetric() {
        orderService.placeOrder(buildCommand());
        verify(metrics).increment("orders.placed");
    }
}
```

## Reflection Attack Test

```java
class SecureSingletonTest {

    @Test
    void reflectionInstantiation_shouldBeRejected() throws NoSuchMethodException {
        var constructor = SecureSingleton.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
            .hasCauseInstanceOf(IllegalStateException.class)
            .cause()
            .hasMessageContaining("already instantiated");
    }

    @Test
    void serialization_shouldReturnSameInstance() throws Exception {
        var original = SecureSingleton.getInstance();

        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) { oos.writeObject(original); }
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            var deserialized = (SecureSingleton) ois.readObject();
            assertThat(deserialized).isSameAs(original);
        }
    }
}
```

# Resilience Patterns — Business Reference

## Full Resilience Stack

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductClient productClient;

    @CircuitBreaker(name = "product-service", fallbackMethod = "productFallback")
    @Retry(name = "product-service")
    @Bulkhead(name = "product-service", type = Bulkhead.Type.THREADPOOL)
    @TimeLimiter(name = "product-service")
    public CompletableFuture<ProductResponse> fetchProduct(UUID productId) {
        return CompletableFuture.supplyAsync(() -> productClient.findById(productId));
    }

    private CompletableFuture<ProductResponse> productFallback(UUID productId, Throwable t) {
        log.warn("Product service unavailable for {}: {}", productId, t.getMessage());
        return CompletableFuture.failedFuture(
            new ProductServiceUnavailableException(productId));
    }
}
```

## Full Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      product-service:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        slow-call-rate-threshold: 80
        slow-call-duration-threshold: 2s
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        record-exceptions:
          - feign.FeignException
          - java.net.ConnectException
        ignore-exceptions:
          - com.company.exception.ResourceNotFoundException

  retry:
    instances:
      product-service:
        max-attempts: 3
        wait-duration: 500ms
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - feign.FeignException.ServiceUnavailable
          - java.net.ConnectException

  bulkhead:
    instances:
      product-service:
        max-concurrent-calls: 20
        max-wait-duration: 500ms

  thread-pool-bulkhead:
    instances:
      product-service:
        max-thread-pool-size: 10
        core-thread-pool-size: 5
        queue-capacity: 20

  timelimiter:
    instances:
      product-service:
        timeout-duration: 3s
        cancel-running-future: true

  ratelimiter:
    instances:
      product-service:
        limit-for-period: 100
        limit-refresh-period: 1s
        timeout-duration: 500ms
```

## Exposing Circuit Breaker Metrics

```yaml
management:
  health:
    circuitbreakers:
      enabled: true
  endpoints:
    web:
      exposure:
        include: health,circuitbreakers,circuitbreakerevents
  endpoint:
    health:
      show-details: always
```

# Feign Client — Business Reference

## Header Propagation (Trace ID, Auth)

```java
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor tracingInterceptor() {
        return template -> {
            var span = Tracer.currentSpan();
            if (span != null) {
                template.header("X-B3-TraceId", span.context().traceId());
                template.header("X-B3-SpanId", span.context().spanId());
            }
        };
    }

    @Bean
    public RequestInterceptor authPropagationInterceptor() {
        return template -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                template.header("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
            }
        };
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
```

## Error Decoder

```java
@Configuration
public class FeignErrorDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 404 -> new ResourceNotFoundException("Resource not found via " + methodKey);
            case 400 -> new BadRequestException("Invalid request to " + methodKey);
            case 503 -> new ServiceUnavailableException(methodKey);
            default  -> new FeignException.InternalServerError(
                "Unexpected error from " + methodKey, response.request(), null, null);
        };
    }
}
```

## Reactive Feign (WebFlux)

```java
@ReactiveFeignClient(name = "product-service")
public interface ReactiveProductClient {

    @GetMapping("/api/products/{id}")
    Mono<ProductResponse> findById(@PathVariable UUID id);

    @GetMapping("/api/products")
    Flux<ProductResponse> findAll();
}
```

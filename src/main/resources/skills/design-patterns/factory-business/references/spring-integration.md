# Factory + Spring Integration — Business Reference

## Registry with Validation at Startup

```java
@Component
public final class PaymentProcessorRegistry {

    private final Map<PaymentMethod, PaymentProcessor> processors;

    public PaymentProcessorRegistry(List<PaymentProcessor> availableProcessors) {
        this.processors = availableProcessors.stream()
            .collect(Collectors.toUnmodifiableMap(
                PaymentProcessor::supportedMethod,
                Function.identity(),
                (existing, replacement) -> {
                    throw new DuplicateProcessorRegistrationException(
                        existing.supportedMethod());
                }
            ));
    }

    @PostConstruct
    void validateAllMethodsCovered() {
        var missing = Arrays.stream(PaymentMethod.values())
            .filter(m -> !processors.containsKey(m))
            .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            throw new IncompleteRegistryException(
                "Missing processors for: " + missing);
        }
    }

    public PaymentProcessor resolve(PaymentMethod method) {
        return Optional.ofNullable(processors.get(method))
            .orElseThrow(() -> new UnsupportedPaymentMethodException(method));
    }
}
```

## Conditional Bean Registration

```java
@Configuration
public class PaymentProcessorConfig {

    @Bean
    @ConditionalOnProperty(name = "payments.providers.stripe.enabled", havingValue = "true")
    public PaymentProcessor stripeProcessor(StripeClient client) {
        return new StripePaymentProcessor(client);
    }

    @Bean
    @ConditionalOnProperty(name = "payments.providers.adyen.enabled", havingValue = "true")
    public PaymentProcessor adyenProcessor(AdyenClient client) {
        return new AdyenPaymentProcessor(client);
    }

    @Bean
    @ConditionalOnMissingBean(PaymentProcessor.class)
    public PaymentProcessor defaultProcessor() {
        return new NoOpPaymentProcessor();
    }
}
```

## Generic Factory Resolver

```java
/**
 * Generic registry for any strategy interface where each implementation
 * declares the discriminator it handles via {@link Discriminated}.
 */
public interface Discriminated<K> {
    K discriminator();
}

public final class GenericRegistry<K, V extends Discriminated<K>> {

    private final Map<K, V> entries;

    public GenericRegistry(List<V> implementations) {
        this.entries = implementations.stream()
            .collect(Collectors.toUnmodifiableMap(Discriminated::discriminator, Function.identity()));
    }

    public V resolve(K key) {
        var value = entries.get(key);
        if (value == null) {
            throw new NoSuchElementException("No implementation registered for key: " + key);
        }
        return value;
    }

    public Set<K> supportedKeys() {
        return entries.keySet();
    }
}

// Usage:
@Configuration
public class RegistryConfig {

    @Bean
    public GenericRegistry<PaymentMethod, PaymentProcessor> paymentProcessorRegistry(
            List<PaymentProcessor> processors) {
        return new GenericRegistry<>(processors);
    }

    @Bean
    public GenericRegistry<ExportFormat, ReportExporter> exporterRegistry(
            List<ReportExporter> exporters) {
        return new GenericRegistry<>(exporters);
    }
}
```

## Factory Returning Prototype-Scoped Beans

```java
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportJob {
    private String sourceFile;
    // mutable per-execution state
}

@Component
public final class ImportJobFactory {

    private final ObjectFactory<ImportJob> jobFactory;

    public ImportJobFactory(ObjectFactory<ImportJob> jobFactory) {
        this.jobFactory = jobFactory;
    }

    /**
     * Creates a new {@link ImportJob} instance for each call,
     * ensuring no shared mutable state between concurrent imports.
     */
    public ImportJob createFor(String sourceFile) {
        var job = jobFactory.getObject();
        job.setSourceFile(sourceFile);
        return job;
    }
}
```

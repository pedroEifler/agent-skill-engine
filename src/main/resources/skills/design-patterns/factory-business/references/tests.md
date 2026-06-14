# Testing Factory Patterns — Business Reference

## Parameterized Tests — Covering Every Variant

```java
class PaymentProcessorFactoryTest {

    @ParameterizedTest(name = "should create processor for {0}")
    @EnumSource(PaymentMethod.class)
    void create_shouldReturnNonNullProcessorForEveryMethod(PaymentMethod method) {
        var processor = PaymentProcessorFactory.create(method);
        assertThat(processor).isNotNull();
    }

    @ParameterizedTest(name = "processor for {0} should handle valid request")
    @EnumSource(value = PaymentMethod.class, names = {"CREDIT_CARD", "PIX"})
    void create_processorShouldSucceedForValidRequest(PaymentMethod method) {
        var processor = PaymentProcessorFactory.create(method);
        var request = PaymentRequest.of(Money.of("50.00", "BRL"), method);

        var result = processor.process(request);

        assertThat(result.isSuccess()).isTrue();
    }
}
```

## Registry Contract Tests

```java
class PaymentProcessorRegistryTest {

    private PaymentProcessorRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PaymentProcessorRegistry(List.of(
            new CreditCardPaymentProcessor(),
            new PixPaymentProcessor(),
            new BoletoPaymentProcessor()
        ));
    }

    @Test
    void resolve_shouldReturnCorrectProcessorType() {
        assertThat(registry.resolve(PaymentMethod.CREDIT_CARD))
            .isInstanceOf(CreditCardPaymentProcessor.class);
        assertThat(registry.resolve(PaymentMethod.PIX))
            .isInstanceOf(PixPaymentProcessor.class);
    }

    @Test
    void resolve_shouldThrowForUnregisteredMethod() {
        var emptyRegistry = new PaymentProcessorRegistry(List.of());
        assertThatThrownBy(() -> emptyRegistry.resolve(PaymentMethod.CREDIT_CARD))
            .isInstanceOf(UnsupportedPaymentMethodException.class)
            .hasMessageContaining("CREDIT_CARD");
    }

    @Test
    void constructor_shouldThrowOnDuplicateRegistration() {
        assertThatThrownBy(() -> new PaymentProcessorRegistry(List.of(
            new CreditCardPaymentProcessor(),
            new CreditCardPaymentProcessor()
        ))).isInstanceOf(DuplicateProcessorRegistrationException.class);
    }
}
```

## Spring Integration Test — Registry Wiring

```java
@SpringBootTest
class PaymentProcessorRegistryIntegrationTest {

    @Autowired
    private PaymentProcessorRegistry registry;

    @Test
    void registry_shouldHaveAllExpectedProcessorsAfterSpringWiring() {
        assertThat(registry.supportedKeys())
            .containsExactlyInAnyOrder(PaymentMethod.values());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void registry_shouldResolveEveryPaymentMethod(PaymentMethod method) {
        assertThatNoException().isThrownBy(() -> registry.resolve(method));
    }
}
```

## Abstract Factory Contract Test

```java
abstract class RepositoryFactoryContractTest {

    protected abstract RepositoryFactory factory();

    @Test
    void factory_shouldCreateNonNullOrderRepository() {
        assertThat(factory().createOrderRepository()).isNotNull();
    }

    @Test
    void factory_shouldCreateConsistentFamily() {
        var orderRepo = factory().createOrderRepository();
        var productRepo = factory().createProductRepository();

        assertThat(orderRepo).isNotNull();
        assertThat(productRepo).isNotNull();
    }
}

class JpaRepositoryFactoryTest extends RepositoryFactoryContractTest {
    @Override protected RepositoryFactory factory() { return new JpaRepositoryFactory(); }
}

class InMemoryRepositoryFactoryTest extends RepositoryFactoryContractTest {
    @Override protected RepositoryFactory factory() { return new InMemoryRepositoryFactory(); }
}
```

## Mutation Testing — Detecting Weak Factory Tests

```xml
<!-- pom.xml: add PIT mutation testing -->
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.0</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetClasses>
            <param>com.company.factory.*</param>
        </targetClasses>
        <targetTests>
            <param>com.company.factory.*Test</param>
        </targetTests>
        <mutationThreshold>85</mutationThreshold>
    </configuration>
</plugin>
```

```bash
# Run mutation tests:
mvn org.pitest:pitest-maven:mutationCoverage
```

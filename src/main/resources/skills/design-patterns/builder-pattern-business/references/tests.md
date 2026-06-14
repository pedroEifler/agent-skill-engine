# Testing Builder Patterns — Business Reference

## Fluent Builder Tests

```java
class HttpRequestConfigTest {

    @Test
    void build_shouldSucceedWithMinimalMandatoryFields() {
        var config = HttpRequestConfig.builder()
            .url("https://api.example.com/orders")
            .build();

        assertThat(config.getUrl()).isEqualTo("https://api.example.com/orders");
        assertThat(config.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(config.getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.isFollowRedirects()).isTrue();
    }

    @Test
    void build_shouldThrowWhenUrlIsMissing() {
        assertThatThrownBy(() -> HttpRequestConfig.builder().build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("url is required");
    }

    @Test
    void build_shouldThrowForGetWithBody() {
        assertThatThrownBy(() -> HttpRequestConfig.builder()
            .url("https://api.example.com")
            .method(HttpMethod.GET)
            .body("{}")
            .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("GET requests cannot have a body");
    }

    @Test
    void toBuilder_shouldProduceIndependentCopy() {
        var original = HttpRequestConfig.builder()
            .url("https://api.example.com/v1")
            .bearerToken("token-abc")
            .maxRetries(3)
            .build();

        var modified = original.toBuilder()
            .url("https://api.example.com/v2")
            .build();

        assertThat(modified.getUrl()).isEqualTo("https://api.example.com/v2");
        assertThat(modified.getHeaders()).isEqualTo(original.getHeaders());
        assertThat(modified.getMaxRetries()).isEqualTo(original.getMaxRetries());
        assertThat(original.getUrl()).isEqualTo("https://api.example.com/v1");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void build_shouldThrowForNegativeRetries(int retries) {
        assertThatThrownBy(() -> HttpRequestConfig.builder()
            .url("https://example.com")
            .maxRetries(retries)
            .build())
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

## Step Builder Tests — Compile-Time Guidance Verified

```java
class EmailMessageTest {

    @Test
    void build_shouldRequireAllMandatoryStepsInOrder() {
        var email = EmailMessage.builder()
            .to("alice@example.com")
            .subject("Hello")
            .body("Hi!")
            .build();

        assertThat(email.getTo()).isEqualTo("alice@example.com");
        assertThat(email.getPriority()).isEqualTo(EmailMessage.Priority.NORMAL);
        assertThat(email.getCc()).isEmpty();
    }

    @Test
    void build_shouldAcceptMultipleCcRecipients() {
        var email = EmailMessage.builder()
            .to("alice@example.com")
            .subject("Report")
            .body("See attached.")
            .cc("bob@example.com", "carol@example.com")
            .priority(EmailMessage.Priority.HIGH)
            .build();

        assertThat(email.getCc()).containsExactly("bob@example.com", "carol@example.com");
        assertThat(email.getPriority()).isEqualTo(EmailMessage.Priority.HIGH);
    }
}
```

## Test Data Builder Tests

```java
class OrderServiceTest {

    @Test
    void process_shouldSucceedForConfirmedOrder() {
        var order = anOrder().confirmed().build();

        assertThat(orderService.canProcess(order)).isTrue();
    }

    @Test
    void process_shouldFailForCancelledOrder() {
        var order = anOrder().cancelled().build();

        assertThatThrownBy(() -> orderService.process(order))
            .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void calculateTotal_shouldSumAllLineItems() {
        var order = anOrder()
            .withItem(lineItem().withPrice("10.00").build())
            .withItem(lineItem().withPrice("25.00").build())
            .withItem(lineItem().withPrice("5.00").build())
            .build();

        assertThat(orderService.calculateTotal(order))
            .isEqualByComparingTo("40.00");
    }
}
```

## Immutability Test

```java
class ImmutabilityTest {

    @Test
    void httpRequestConfig_shouldBeImmutable() {
        var config = HttpRequestConfig.builder()
            .url("https://api.example.com")
            .header("X-Custom", "value")
            .build();

        assertThatThrownBy(() -> config.getHeaders().put("X-Injected", "attack"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void emailMessage_ccList_shouldBeImmutable() {
        var email = EmailMessage.builder()
            .to("alice@example.com")
            .subject("Test")
            .body("Body")
            .cc("bob@example.com")
            .build();

        assertThatThrownBy(() -> email.getCc().add("evil@example.com"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
```

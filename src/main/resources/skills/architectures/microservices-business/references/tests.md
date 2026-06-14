# Testing Microservices — Business Reference

## Contract Testing — WireMock for Feign

```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "product-service.url=http://localhost:${wiremock.server.port}")
class OrderServiceFeignTest {

    @Autowired private OrderService orderService;

    @Test
    void placeOrder_shouldCallProductServiceAndSucceed() {
        stubFor(get(urlEqualTo("/api/products/" + PRODUCT_ID))
            .willReturn(okJson("""
                {"id": "%s", "name": "Widget", "price": 25.00}
                """.formatted(PRODUCT_ID))));

        var response = orderService.placeOrder(buildCommand());

        assertThat(response.total()).isEqualByComparingTo("50.00");
        verify(getRequestedFor(urlEqualTo("/api/products/" + PRODUCT_ID)));
    }

    @Test
    void placeOrder_shouldOpenCircuitAfterRepeatedFailures() {
        stubFor(get(urlEqualTo("/api/products/" + PRODUCT_ID))
            .willReturn(serverError()));

        for (int i = 0; i < 10; i++) {
            assertThatThrownBy(() -> orderService.placeOrder(buildCommand()));
        }

        // 11th call should fail fast via circuit breaker (no HTTP call)
        var before = WireMock.getAllServeEvents().size();
        assertThatThrownBy(() -> orderService.placeOrder(buildCommand()));
        var after = WireMock.getAllServeEvents().size();
        assertThat(after).isEqualTo(before); // no new request made
    }
}
```

## Contract Testing — Pact (Consumer Side)

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "product-service")
class ProductClientPactTest {

    @Pact(consumer = "order-service")
    public RequestResponsePact productByIdPact(PactDslWithProvider builder) {
        return builder
            .given("product with id 123 exists")
            .uponReceiving("a request for product 123")
            .path("/api/products/123")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody()
                .uuid("id", UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .stringType("name", "Widget")
                .decimalType("price", 25.00))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "productByIdPact")
    void shouldFetchProduct(MockServer mockServer) {
        var client = buildClient(mockServer.getUrl());
        var product = client.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(product.name()).isEqualTo("Widget");
    }
}
```

## Testcontainers — Kafka Integration

```java
@SpringBootTest
@Testcontainers
class OrderEventPublisherIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private PlaceOrderUseCase placeOrderUseCase;

    @Test
    void placeOrder_shouldPublishOrderPlacedEvent() throws Exception {
        try (var consumer = createTestConsumer()) {
            consumer.subscribe(List.of("order.placed"));

            placeOrderUseCase.execute(buildCommand());

            var records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.count()).isEqualTo(1);
            var event = deserialize(records.iterator().next().value(), OrderPlacedEvent.class);
            assertThat(event.total()).isEqualByComparingTo("50.00");
        }
    }
}
```

## End-to-End Test — Full Stack with Testcontainers Compose

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderFlowE2ETest {

    @Container
    static ComposeContainer environment = new ComposeContainer(
            new File("docker-compose-test.yml"))
        .withExposedService("api-gateway", 8080)
        .withExposedService("order-service", 8081)
        .withExposedService("product-service", 8082)
        .waitingFor("api-gateway", Wait.forHttp("/actuator/health").forStatusCode(200));

    @Test
    void completeOrderFlow_shouldSucceed() {
        var gatewayUrl = "http://" + environment.getServiceHost("api-gateway", 8080)
            + ":" + environment.getServicePort("api-gateway", 8080);

        var response = RestAssured.given()
            .baseUri(gatewayUrl)
            .contentType(ContentType.JSON)
            .body(buildOrderRequest())
            .post("/api/orders")
            .then()
            .statusCode(201)
            .extract().as(OrderResponse.class);

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }
}
```

## Chaos Testing — Simulating Latency/Failure

```java
@Test
void shouldDegradeGracefullyUnderLatency() {
    stubFor(get(urlEqualTo("/api/products/" + PRODUCT_ID))
        .willReturn(okJson(productJson()).withFixedDelay(5000))); // 5s delay

    var start = Instant.now();
    assertThatThrownBy(() -> orderService.fetchProduct(PRODUCT_ID).join())
        .hasCauseInstanceOf(TimeoutException.class);
    var elapsed = Duration.between(start, Instant.now());

    // TimeLimiter configured for 3s should cut this off before the 5s delay
    assertThat(elapsed).isLessThan(Duration.ofSeconds(4));
}
```

---
name: microservices-business
description: >
  Use this skill whenever a professional developer requests production-grade microservices architecture,
  Spring Cloud components, service-to-service communication, API Gateway setup, distributed tracing,
  event-driven architecture, or any enterprise microservices pattern in Java. Triggers include:
  "microservices", "Spring Cloud", "service mesh", "API Gateway", "Eureka", "Consul", "Feign Client",
  "OpenFeign", "circuit breaker", "Resilience4j", "distributed tracing", "Sleuth", "Zipkin", "Kafka",
  "RabbitMQ microservices", "event-driven", "Saga pattern", "CQRS microservices", "Config Server",
  "service discovery", "load balancing microservices", "JWT gateway", "inter-service communication",
  "microservice authentication", "distributed transaction", "outbox pattern microservices",
  "contract testing", "Spring Cloud Gateway", "reactive microservices". Produces clean, production-ready
  English code with no inline comments, full observability stack, resilience patterns, secure inter-service
  communication, and comprehensive testing strategy. ALWAYS use for any professional microservices task.
---

# Skill: Microservices Business

Generates production-grade Spring Boot microservices with Spring Cloud. Clean English code, no inline
comments, full resilience stack, observability, secure inter-service communication, and testable design.

---

## 1. Version Matrix

| Spring Boot | Spring Cloud  | Java | Status          |
|-------------|---------------|------|-----------------|
| 3.3.x       | 2023.0.x      | 21   | **Recommended** |
| 3.2.x       | 2023.0.x      | 17   | Active          |
| 2.7.x       | 2021.0.x      | 11   | Legacy          |

---

## 2. System Architecture

```
Internet
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│                     API GATEWAY :8080                    │
│  Auth filter │ Rate limit │ Circuit breaker │ Routing    │
└───────┬──────────────┬────────────────┬─────────────────┘
        │              │                │
        ▼              ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  order-svc   │ │ product-svc  │ │ payment-svc  │
│    :8081     │ │    :8082     │ │    :8083     │
│  PostgreSQL  │ │  PostgreSQL  │ │  PostgreSQL  │
└──────┬───────┘ └──────────────┘ └──────────────┘
       │  async events
       ▼
┌──────────────┐      ┌──────────────┐     ┌─────────────┐
│    Kafka     │      │   Eureka     │     │Config Server│
│   :9092      │      │   :8761      │     │    :8888    │
└──────────────┘      └──────────────┘     └─────────────┘
                      ┌──────────────┐
                      │    Zipkin    │
                      │   :9411      │ ← distributed tracing
                      └──────────────┘
```

---

## 3. Parent POM (Multi-Module)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.company</groupId>
    <artifactId>platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>eureka-server</module>
        <module>config-server</module>
        <module>api-gateway</module>
        <module>order-service</module>
        <module>product-service</module>
        <module>payment-service</module>
    </modules>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

---

## 4. Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml
# application.yml
server:
  port: 8761

spring:
  application:
    name: eureka-server
  security:
    user:
      name: ${EUREKA_USER:admin}
      password: ${EUREKA_PASSWORD:secret}

eureka:
  instance:
    hostname: ${HOSTNAME:localhost}
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
```

---

## 5. API Gateway

```java
@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 100
            redis-rate-limiter.burstCapacity: 200
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - name: CircuitBreaker
              args:
                name: order-service
                fallbackUri: forward:/fallback/orders
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE

        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - name: CircuitBreaker
              args:
                name: product-service
                fallbackUri: forward:/fallback/products

resilience4j:
  circuitbreaker:
    instances:
      order-service:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
      product-service:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/orders")
    public ResponseEntity<Map<String, String>> ordersFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "ORDER_SERVICE_UNAVAILABLE",
                "message", "Order service is currently unavailable. Please try again later."
            ));
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, String>> productsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "PRODUCT_SERVICE_UNAVAILABLE",
                         "message", "Product service is currently unavailable."));
    }
}
```

---

## 6. Order Service — Full Service Template

```yaml
# order-service/application.yml
server:
  port: ${PORT:8081}

spring:
  application:
    name: order-service
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/orders}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://admin:secret@localhost:8761/eureka/}
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers,prometheus
  tracing:
    sampling:
      probability: 1.0

resilience4j:
  circuitbreaker:
    instances:
      product-service:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      product-service:
        max-attempts: 3
        wait-duration: 500ms
        retry-exceptions:
          - feign.FeignException.ServiceUnavailable
```

---

## 7. Inter-Service Communication

```java
@FeignClient(
    name = "product-service",
    fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse findById(@PathVariable UUID id);

    @PostMapping("/api/products/batch")
    List<ProductResponse> findByIds(@RequestBody List<UUID> ids);
}

@Component
public class ProductClientFallbackFactory
        implements FallbackFactory<ProductClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallbackFactory.class);

    @Override
    public ProductClient create(Throwable cause) {
        log.error("Product service call failed", cause);
        return new ProductClient() {
            @Override
            public ProductResponse findById(UUID id) {
                throw new ProductServiceUnavailableException(id, cause);
            }

            @Override
            public List<ProductResponse> findByIds(List<UUID> ids) {
                throw new ProductServiceUnavailableException(cause);
            }
        };
    }
}
```

---

## 8. Event-Driven Communication

```java
public sealed interface OrderEvent
    permits OrderPlacedEvent, OrderCancelledEvent, OrderShippedEvent {
    UUID orderId();
    Instant occurredOn();
}

public record OrderPlacedEvent(
    UUID orderId,
    UUID customerId,
    List<OrderItemData> items,
    BigDecimal total,
    Instant occurredOn
) implements OrderEvent {}

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OrderEvent event) {
        var topic = switch (event) {
            case OrderPlacedEvent e    -> "order.placed";
            case OrderCancelledEvent e -> "order.cancelled";
            case OrderShippedEvent e   -> "order.shipped";
        };
        kafkaTemplate.send(topic, event.orderId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    throw new EventPublishException(topic, event.orderId(), ex);
                }
            });
    }
}

@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ProcessPaymentUseCase processPayment;

    @KafkaListener(topics = "order.placed", groupId = "payment-service")
    public void handle(OrderPlacedEvent event) {
        processPayment.execute(new ProcessPaymentCommand(
            event.orderId(), event.customerId(), event.total()));
    }

    @KafkaListener(topics = "order.cancelled", groupId = "payment-service")
    public void handle(OrderCancelledEvent event) {
        processPayment.refund(event.orderId());
    }
}
```

---

## 9. Distributed Tracing & Observability

```yaml
# Every service application.yml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_URL:http://localhost:9411/api/v2/spans}
  metrics:
    export:
      prometheus:
        enabled: true
```

```xml
<!-- Every service pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## 10. docker-compose.yml

```yaml
version: '3.8'
services:

  postgres-orders:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: orders
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports: ["5433:5432"]
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "postgres"]
      interval: 5s
      retries: 5

  postgres-products:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: products
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports: ["5434:5432"]

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    healthcheck:
      test: kafka-topics --bootstrap-server kafka:29092 --list
      interval: 10s
      retries: 5

  zipkin:
    image: openzipkin/zipkin
    ports: ["9411:9411"]

  eureka-server:
    build: ./eureka-server
    ports: ["8761:8761"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      retries: 5

  product-service:
    build: ./product-service
    ports: ["8082:8082"]
    environment:
      DB_URL: jdbc:postgresql://postgres-products:5432/products
      EUREKA_URL: http://admin:secret@eureka-server:8761/eureka/
      KAFKA_SERVERS: kafka:29092
      ZIPKIN_URL: http://zipkin:9411/api/v2/spans
    depends_on:
      eureka-server: { condition: service_healthy }
      postgres-products: { condition: service_healthy }
      kafka: { condition: service_healthy }

  order-service:
    build: ./order-service
    ports: ["8081:8081"]
    environment:
      DB_URL: jdbc:postgresql://postgres-orders:5432/orders
      EUREKA_URL: http://admin:secret@eureka-server:8761/eureka/
      KAFKA_SERVERS: kafka:29092
      ZIPKIN_URL: http://zipkin:9411/api/v2/spans
    depends_on:
      eureka-server: { condition: service_healthy }
      postgres-orders: { condition: service_healthy }
      kafka: { condition: service_healthy }

  api-gateway:
    build: ./api-gateway
    ports: ["8080:8080"]
    environment:
      EUREKA_URL: http://admin:secret@eureka-server:8761/eureka/
    depends_on:
      eureka-server: { condition: service_healthy }
```

---

## 11. Naming Conventions

```java
// Service applications → noun + ServiceApplication
public class OrderServiceApplication {}

// Feign clients → noun + Client
public interface ProductClient {}

// Event publishers → noun + EventPublisher
public class OrderEventPublisher {}

// Kafka consumers → noun + EventConsumer
public class PaymentEventConsumer {}

// Fallback factories → Client + FallbackFactory
public class ProductClientFallbackFactory {}

// Events → past tense + Event (record)
public record OrderPlacedEvent(...) {}

// Config classes → noun + Config
public class KafkaConfig {}
public class SecurityConfig {}
```

---

## 12. Checklist

- [ ] Unique `spring.application.name` per service
- [ ] All config via env variables with sensible defaults
- [ ] Eureka client configured with health check
- [ ] Feign clients with `FallbackFactory` (not `fallback` — factory gives you the cause)
- [ ] Circuit breaker + retry configured per downstream dependency
- [ ] Kafka topics declared with sensible partitions and retention
- [ ] Transactional outbox for guaranteed event delivery
- [ ] Distributed tracing (Micrometer + Zipkin) in every service
- [ ] Prometheus metrics endpoint exposed
- [ ] Docker health checks with `depends_on: condition: service_healthy`
- [ ] Contract tests (Pact or Spring Cloud Contract) for every Feign interface

---

## 13. Delivery Format

1. **Architecture diagram** (ASCII) with ports and communication types
2. **Parent POM** with version management
3. **Infrastructure services**: Eureka, Config Server, API Gateway
4. **Domain services**: one full service as template
5. **docker-compose.yml** with health checks
6. **Test skeletons**: WireMock for Feign, Testcontainers for Kafka

Load reference files:
- `references/architectures/feign.md` — Advanced Feign patterns, propagation, mTLS
- `references/architectures/messaging.md` — Kafka producer/consumer, exactly-once, DLQ
- `references/architectures/resilience.md` — Circuit breaker, bulkhead, rate limiter
- `references/architectures/security.md` — JWT propagation, mTLS, OAuth2 resource server
- `references/architectures/tests.md` — WireMock, Pact, Testcontainers Kafka

# Spring Boot 3.x — Business Reference

> Target: Spring Boot 3.3.x with Java 17+. Use `jakarta.*` namespaces.

## pom.xml — Spring Boot 3.3.x + Java 21
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <groupId>com.company</groupId>
    <artifactId>my-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.3</jjwt.version>
    </properties>

    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId></dependency>
        <dependency><groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version>
            <scope>runtime</scope></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version>
            <scope>runtime</scope></dependency>
        <dependency><groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    </dependencies>
</project>
```

## Entity (jakarta.*)
```java
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

## Records as DTOs (Java 17+)
```java
public record CreateProductRequest(
    @NotBlank @Size(max = 50)   String sku,
    @NotBlank @Size(max = 200)  String name,
    @NotNull @DecimalMin("0.01") BigDecimal price
) { }

public record ProductResponse(UUID id, String sku, String name, BigDecimal price, LocalDateTime createdAt) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(), product.getSku(),
            product.getName(), product.getPrice(), product.getCreatedAt()
        );
    }
}
```

## SecurityFilterChain (3.x — no inheritance)
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Flyway Migration Script
```sql
-- db/migration/V1__create_products_table.sql
CREATE TABLE products (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    sku        VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(200) NOT NULL,
    price      NUMERIC(12,2) NOT NULL CHECK (price > 0),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_sku ON products(sku);
```

## Dockerfile
```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

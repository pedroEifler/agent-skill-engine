# Spring Boot 2.x — Business Reference

> Target: Spring Boot 2.7.x with Java 8, 11, or 17. Use `javax.*` namespaces.

## pom.xml — Spring Boot 2.7.x
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
        <version>2.7.18</version>
    </parent>

    <groupId>com.company</groupId>
    <artifactId>my-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.11.5</jjwt.version>
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

## Entity (javax.*)
```java
import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

## Security Config (WebSecurityConfigurerAdapter — 2.x only)
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

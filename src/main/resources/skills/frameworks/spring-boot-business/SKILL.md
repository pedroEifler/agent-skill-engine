---
name: springboot-business
description: >
  Use this skill whenever a professional developer requests production-grade Spring Boot project scaffolding,
  REST API generation, microservice architecture, Spring Security setup, database configuration, or any
  enterprise-level Spring Boot code. Triggers include: "create a Spring Boot project", "scaffold a Spring
  Boot service", "Spring Boot REST API", "Spring Boot microservice", "Spring Boot with JWT", "Spring Boot
  production setup", "Spring Boot best practices", "Spring Data JPA repository", "Spring Boot template",
  "Spring Boot boilerplate", "enterprise Spring Boot", requests involving Spring Security, Spring Cloud,
  Flyway, Actuator, or Docker + Spring Boot. Produces clean, production-ready English code without inline
  comments, with full JavaDoc on public API, proper layered architecture, version-aware patterns, and
  testable design. ALWAYS use this skill for any professional Spring Boot code generation task.
---

# Skill: Spring Boot Business

Generates production-ready Spring Boot applications following enterprise standards. Clean English code,
no inline comments, full JavaDoc on public API, layered architecture, version-aware features.

---

## 1. Version Detection & Compatibility

Identify the Spring Boot and Java versions before generating any code.

### Detection (pom.xml):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>
```

### Compatibility matrix:

| Spring Boot | Min Java | Recommended | Status          |
|-------------|----------|-------------|-----------------|
| 2.7.x       | 8        | 11 / 17     | Legacy (EOL)    |
| 3.2.x       | 17       | 17 / 21     | Active          |
| **3.3.x**   | **17**   | **21**      | **Recommended** |

> Spring Boot 3.x requires Java 17+. For Java 8/11 projects, target Spring Boot 2.7.x.
> Load `references/springboot2.md` for 2.x; `references/springboot3.md` for 3.x.

---

## 2. Standard Project Layout

```
my-service/
├── src/
│   ├── main/
│   │   ├── java/com/company/service/
│   │   │   ├── ServiceApplication.java
│   │   │   ├── api/
│   │   │   │   ├── controller/
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       └── response/
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── infrastructure/
│   │   │   │   ├── config/
│   │   │   │   ├── security/
│   │   │   │   └── persistence/
│   │   │   └── exception/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/           ← Flyway scripts
│   └── test/
│       └── java/com/company/service/
│           ├── api/controller/
│           ├── domain/service/
│           └── integration/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── README.md
```

---

## 3. Naming Conventions

```java
// Controllers → PascalCase + Controller suffix
@RestController public class ProductController { }

// Services (interface + impl pattern)
public interface ProductService { }
@Service public class ProductServiceImpl implements ProductService { }

// Repositories
public interface ProductRepository extends JpaRepository<Product, Long> { }

// DTOs → PascalCase + Request/Response suffix
public record CreateProductRequest(...) { }
public record ProductResponse(...) { }

// Entities → PascalCase, singular, no suffix
@Entity public class Product { }

// Exceptions → descriptive + Exception suffix
public class ProductNotFoundException extends DomainException { }

// Constants → SCREAMING_SNAKE_CASE
public static final String BEARER_PREFIX = "Bearer ";
public static final int TOKEN_EXPIRY_HOURS = 24;
```

---

## 4. JavaDoc Standards

```java
/**
 * Manages the product catalog lifecycle including creation, updates, and soft deletion.
 *
 * <p>All mutating operations publish domain events to the application event bus,
 * enabling downstream services to react without tight coupling.</p>
 *
 * @author platform-team
 * @since 1.0
 */
public interface ProductService {

    /**
     * Creates a new product in the catalog.
     *
     * @param request the product creation payload; must not be null
     * @return the persisted product representation
     * @throws ProductAlreadyExistsException if a product with the same SKU already exists
     */
    ProductResponse create(CreateProductRequest request);

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the product ID; must not be null
     * @return the product representation
     * @throws ProductNotFoundException if no product exists with the given ID
     */
    ProductResponse findById(UUID id);
}
```

---

## 5. Exception Architecture

```java
// Base domain exception
public abstract class DomainException extends RuntimeException {
    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}

// Concrete exceptions
public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(UUID id) {
        super("PRODUCT_NOT_FOUND", "Product not found with id: " + id);
    }
}

public class DuplicateSkuException extends DomainException {
    public DuplicateSkuException(String sku) {
        super("DUPLICATE_SKU", "Product with SKU already exists: " + sku);
    }
}

// Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ProductNotFoundException ex) {
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "Invalid value")
            ));
        return new ValidationErrorResponse("VALIDATION_FAILED", fieldErrors, LocalDateTime.now());
    }
}
```

---

## 6. application.yml Structure

```yaml
spring:
  application:
    name: my-service
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: ${PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when_authorized

logging:
  level:
    com.company: INFO
```

---

## 7. Code Generation Checklist

- [ ] Spring Boot + Java version identified and compatible
- [ ] Architecture layer chosen (layered / hexagonal)
- [ ] All naming conventions followed
- [ ] JavaDoc on all public interfaces and methods
- [ ] DTOs separated from entities (Records for 3.x / Java 17+)
- [ ] Custom exception hierarchy defined
- [ ] Global exception handler with structured error responses
- [ ] `application.yml` with env-variable placeholders
- [ ] Flyway migration script for schema changes
- [ ] Unit tests (service layer with Mockito) + integration tests (MockMvc)
- [ ] Dockerfile and docker-compose for local development

---

## 8. Delivery Format

1. **Project structure** (for new services)
2. **pom.xml** with correct Spring Boot parent and curated dependencies
3. **application.yml** (dev + prod profiles)
4. **Production code** — clean, self-documenting, no inline comments
5. **Test classes** — unit + integration stubs
6. **Dockerfile** (if greenfield project)

Load the appropriate reference files:
- Spring Boot 2.x → `references/springboot2.md`
- Spring Boot 3.x → `references/springboot3.md`
- Spring Security / JWT → `references/security.md`
- Testing patterns → `references/tests.md`

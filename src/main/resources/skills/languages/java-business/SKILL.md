---
name: java-business
description: >
  Use this skill whenever a Java developer requests project scaffolding, class generation, architecture setup,
  or any production-grade Java code following industry standards. Triggers include: "create a Java project",
  "scaffold a Java service", "Java project structure", "Java boilerplate", "Java template", "set up a Java app",
  "Java best practices", "generate Java code", "Java design patterns", requests involving Spring Boot, Maven,
  Gradle, microservices, or enterprise Java. This skill produces clean, production-ready Java code in English
  without inline comments, with proper JavaDoc, PascalCase/camelCase naming, custom exceptions, and version-aware
  feature usage (Java 8 through latest stable LTS). ALWAYS use this skill for business Java code generation
  tasks, even when the request seems straightforward.
---

# Skill: Java Business

Generates production-ready Java projects and code following industry standards, targeting the specified
Java version (8+). No inline comments. Full JavaDoc on public API. English throughout.

---

## 1. Java Version Detection

**Always** identify or ask for the project's Java version before generating any code.

### Detection sources (in priority order):

1. **Maven** (`pom.xml`):
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
<!-- OR with maven-compiler-plugin -->
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration><release>17</release></configuration>
</plugin>
```

2. **Gradle** (`build.gradle` / `build.gradle.kts`):
```groovy
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
```

3. **`.java-version`** file (SDKMAN/jEnv)
4. **Ask the developer** if none of the above are found.

### Supported stable versions:

| Version | Type | Notable Features |
|---------|------|-----------------|
| **8**  | LTS  | Lambdas, Streams, Optional, default methods |
| **11** | LTS  | `var`, HttpClient, String API additions |
| **17** | LTS  | Records, Sealed classes, Pattern Matching instanceof, Text Blocks |
| **21** | LTS  | Virtual Threads, Sequenced Collections, Pattern Matching switch, Record Patterns |
| 22–24  | Non-LTS | Refinements, preview graduates |

> Never use features from a version higher than the project's target.

---

## 2. Standard Project Layout

```
my-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/company/project/
│   │   │       ├── Application.java
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── controller/
│   │   │       ├── exception/
│   │   │       └── util/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/company/project/
├── pom.xml or build.gradle[.kts]
├── .gitignore
└── README.md
```

---

## 3. Naming Conventions

```java
// Classes, Interfaces, Enums, Annotations → PascalCase
public class OrderService { }
public interface PaymentGateway { }
public enum OrderStatus { PENDING, CONFIRMED, CANCELLED }
public @interface Transactional { }

// Methods, variables, parameters → camelCase
private String customerName;
private int itemCount;
public BigDecimal calculateDiscount(BigDecimal price) { }

// Constants → SCREAMING_SNAKE_CASE
public static final int MAX_RETRY_ATTEMPTS = 3;
public static final String DEFAULT_CURRENCY = "USD";

// Packages → lowercase, reversed domain
package com.company.project.service;

// Test classes → suffix with Test or IT (integration)
class OrderServiceTest { }
class OrderRepositoryIT { }
```

---

## 4. JavaDoc Standards

Apply to all public classes, interfaces, and methods. Skip obvious getters/setters only if lombok is used.

```java
/**
 * Manages order lifecycle from creation through fulfillment.
 *
 * <p>This service coordinates inventory checks, payment processing,
 * and notification dispatch for every order state transition.</p>
 *
 * @author team-name
 * @version 2.1
 * @since 1.0
 */
public class OrderService {

    /**
     * Places a new order for the given customer.
     *
     * @param customerId the unique identifier of the customer; must not be null
     * @param items      the list of line items; must contain at least one entry
     * @return the persisted {@link Order} with assigned ID and timestamps
     * @throws CustomerNotFoundException if no customer exists with the given ID
     * @throws InsufficientStockException if any item exceeds available stock
     */
    public Order placeOrder(UUID customerId, List<LineItem> items) { }
}
```

---

## 5. Exception Handling

```java
// Domain exception hierarchy
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    protected DomainException(String message, Throwable cause) { super(message, cause); }
}

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " not found with id: " + id);
    }
}

public class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(String message) { super(message); }
}

// Usage
public Order findById(UUID id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Order", id));
}
```

---

## 6. Version-Specific Templates

Load the appropriate reference file based on the detected Java version:

- Java 8  → `references/java8.md`
- Java 11 → `references/java11.md`
- Java 17 → `references/java17.md`
- Java 21 → `references/java21.md`

---

## 7. Code Generation Checklist

Before generating any output, verify:

- [ ] Java version identified and feature usage constrained accordingly
- [ ] Package structure follows domain layout
- [ ] All names follow PascalCase/camelCase/SCREAMING_SNAKE_CASE conventions
- [ ] JavaDoc on all public types and methods
- [ ] Custom exception hierarchy defined
- [ ] No inline comments — code is self-documenting
- [ ] Build file (`pom.xml` / `build.gradle`) targets the correct version
- [ ] `.gitignore` included for new projects
- [ ] Test class stubs provided alongside production code

---

## 8. Delivery Format

Deliver in this order:
1. **Project/file structure** (for new projects)
2. **Build file** with correct Java version and standard dependencies
3. **Production code** — clean, no inline comments, self-documenting names
4. **Test stubs** — JUnit 5 skeleton matching each public method

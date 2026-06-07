# Java 8 — Professional Reference

## Key Features

### Lambdas & Functional Interfaces
```java
// Core functional interfaces
Function<String, Integer>  parse    = Integer::parseInt;
Consumer<String>           log      = System.out::println;
Supplier<List<String>>     newList  = ArrayList::new;
Predicate<String>          notBlank = s -> !s.isBlank();
BiFunction<Integer,Integer,Integer> add = Integer::sum;
```

### Streams
```java
List<Order> confirmed = orders.stream()
    .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
    .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
    .collect(Collectors.toUnmodifiableList());

Map<OrderStatus, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus));

OptionalDouble avg = orders.stream()
    .mapToDouble(o -> o.getTotal().doubleValue())
    .average();
```

### Optional
```java
public Optional<Customer> findByEmail(String email) {
    return customerRepository.findByEmail(email);
}

// Consumer pattern
findByEmail(email).ifPresent(c -> notificationService.welcome(c));

// Transform pattern
String name = findByEmail(email)
    .map(Customer::getFullName)
    .orElse("Unknown");

// Throw pattern
Customer customer = findByEmail(email)
    .orElseThrow(() -> new EntityNotFoundException("Customer", email));
```

### Default & Static Interface Methods
```java
public interface Auditable {
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    default boolean isStale(Duration threshold) {
        return getUpdatedAt().isBefore(LocalDateTime.now().minus(threshold));
    }

    static Comparator<Auditable> byCreatedAt() {
        return Comparator.comparing(Auditable::getCreatedAt);
    }
}
```

## pom.xml — Java 8
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.company</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.0</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.4.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

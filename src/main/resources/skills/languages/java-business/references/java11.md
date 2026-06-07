# Java 11 — Professional Reference

## Includes all Java 8 features, plus:

### Local Variable Type Inference
```java
var orders = new ArrayList<Order>();
var statusMap = new HashMap<OrderStatus, List<Order>>();
var client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build();
```

### String API Additions
```java
String raw = "  \n  ";
raw.isBlank();           // true
raw.strip();             // ""
raw.stripLeading();
raw.stripTrailing();
"line1\nline2".lines().collect(Collectors.toList());
"ab".repeat(3);          // "ababab"
```

### HttpClient
```java
var client = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)
    .connectTimeout(Duration.ofSeconds(10))
    .build();

var request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/orders"))
    .header("Authorization", "Bearer " + token)
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(payload))
    .timeout(Duration.ofSeconds(30))
    .build();

HttpResponse<String> response = client.send(request,
    HttpResponse.BodyHandlers.ofString());

if (response.statusCode() != 200) {
    throw new ExternalServiceException("Unexpected status: " + response.statusCode());
}
```

### Collection Factory Methods (Java 9, available from 11+)
```java
List<String> roles    = List.of("ADMIN", "USER", "VIEWER");
Set<String>  statuses = Set.of("ACTIVE", "INACTIVE");
Map<String, Integer> codes = Map.of("OK", 200, "NOT_FOUND", 404);
```

## pom.xml — Java 11
```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

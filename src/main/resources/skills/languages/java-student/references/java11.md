# Java 11 — Referência para Estudantes

## Inclui tudo do Java 8, mais:

### var — inferência de tipo local
```java
// Em vez de escrever o tipo completo:
ArrayList<String> lista = new ArrayList<String>();

// Java 11 infere o tipo automaticamente com var:
var lista = new ArrayList<String>(); // Java sabe que é ArrayList<String>

// Funciona em qualquer variável local:
var numero = 42;          // int
var texto = "Olá";       // String
var mapa = new HashMap<String, Integer>(); // HashMap<String, Integer>

// ❌ var NÃO funciona em:
// - atributos de classe
// - parâmetros de método
// - retorno de método
```

### Novos métodos de String
```java
String texto = "  Olá, mundo!  ";

// isBlank() → true se vazio ou só espaços
boolean vazio = "   ".isBlank(); // true

// strip() → igual trim(), mas funciona com Unicode
String semEspacos = texto.strip(); // "Olá, mundo!"

// lines() → divide a string em linhas (Stream)
"linha1\nlinha2\nlinha3".lines()
    .forEach(System.out::println);

// repeat() → repete a string N vezes
String hifens = "-".repeat(20); // "--------------------"
```

### HttpClient — cliente HTTP nativo
```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

// Criar o cliente HTTP
var cliente = HttpClient.newHttpClient();

// Criar a requisição
var requisicao = HttpRequest.newBuilder()
    .uri(URI.create("https://api.exemplo.com/dados"))
    .GET()
    .build();

// Fazer a chamada e receber a resposta
var resposta = cliente.send(requisicao,
    HttpResponse.BodyHandlers.ofString());

System.out.println("Status: " + resposta.statusCode());
System.out.println("Corpo: " + resposta.body());
```

## pom.xml para Java 11
```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

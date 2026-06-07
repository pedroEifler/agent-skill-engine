# Java 8 — Referência para Estudantes

## Recursos principais do Java 8

### Lambdas e Interfaces Funcionais
```java
// Antes do Java 8 (classe anônima)
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Executando...");
    }
};

// Com Java 8 (lambda) — muito mais simples!
Runnable r = () -> System.out.println("Executando...");

// Lambda com parâmetros
Comparator<String> comp = (a, b) -> a.compareTo(b);

// Interfaces funcionais do Java 8:
// Function<T,R>  → recebe T, retorna R
// Consumer<T>    → recebe T, não retorna nada
// Supplier<T>    → não recebe nada, retorna T
// Predicate<T>   → recebe T, retorna boolean
```

### Streams
```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

List<Integer> numeros = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Filtrar números pares e dobrar cada um
List<Integer> resultado = numeros.stream()
    .filter(n -> n % 2 == 0)    // mantém só os pares
    .map(n -> n * 2)             // dobra cada valor
    .collect(Collectors.toList()); // coleta em uma lista
```

### Optional — evitando NullPointerException
```java
import java.util.Optional;

// Criando um Optional
Optional<String> nome = Optional.ofNullable(buscarNomeDoBanco());

// Usando com segurança
String nomeOuPadrao = nome.orElse("Nome não encontrado");

// Verificando presença
if (nome.isPresent()) {
    System.out.println("Nome: " + nome.get());
}
```

### Interface default
```java
// Java 8 permite métodos com implementação em interfaces
public interface Saudavel {

    // Método abstrato (obrigatório implementar)
    String getNome();

    // Método default (opcional sobrescrever)
    default void cumprimentar() {
        System.out.println("Olá, " + getNome() + "!");
    }
}
```

## pom.xml para Java 8
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>br.com.meuprojeto</groupId>
    <artifactId>meu-projeto</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <!-- Define a versão do Java para compilação -->
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <!-- Define o encoding dos arquivos -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- JUnit 5 para testes -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

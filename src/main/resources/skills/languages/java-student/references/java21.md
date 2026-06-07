# Java 21 — Referência para Estudantes

## Inclui tudo do Java 8, 11 e 17, mais:

### Virtual Threads — concorrência simplificada
```java
import java.util.concurrent.Executors;

// Virtual Threads são "threads leves" gerenciadas pela JVM
// Permitem criar milhares de threads sem sobrecarregar o sistema

// Criar uma virtual thread simples:
Thread vt = Thread.ofVirtual().start(() -> {
    System.out.println("Executando em virtual thread!");
});

// Executor com virtual threads (para múltiplas tarefas):
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Cada tarefa roda em sua própria virtual thread
    for (int i = 0; i < 1000; i++) {
        final int id = i;
        executor.submit(() -> processarTarefa(id));
    }
} // executor fecha automaticamente aqui (try-with-resources)
```

### Pattern Matching no Switch
```java
// Java 21 permite usar switch com padrões de tipo
Object forma = new Circulo(5.0);

// switch com pattern matching — muito mais expressivo!
String descricao = switch (forma) {
    case Circulo c    -> "Círculo com raio " + c.raio();
    case Retangulo r  -> "Retângulo " + r.largura() + "x" + r.altura();
    case Triangulo t  -> "Triângulo";
    case null         -> "Forma nula";
    default           -> "Forma desconhecida";
};
```

### Sequenced Collections — ordem garantida
```java
import java.util.SequencedCollection;

// Nova interface que garante ordem e acesso ao primeiro/último elemento
// List, Deque e LinkedHashSet implementam SequencedCollection

var lista = new java.util.ArrayList<>(java.util.List.of("A", "B", "C"));

// Novos métodos:
String primeiro = lista.getFirst(); // "A"
String ultimo   = lista.getLast();  // "C"
lista.addFirst("Z");               // adiciona no início
lista.addLast("W");                // adiciona no final
lista.removeFirst();               // remove do início
```

### Record Patterns — desestruturação em pattern matching
```java
public record Ponto(int x, int y) { }

Object obj = new Ponto(3, 4);

// Java 21: desestrutura o record diretamente no pattern!
if (obj instanceof Ponto(int x, int y)) {
    System.out.println("x=" + x + ", y=" + y); // acessa x e y diretamente
}
```

## pom.xml para Java 21
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

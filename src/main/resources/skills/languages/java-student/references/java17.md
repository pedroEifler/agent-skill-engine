# Java 17 — Referência para Estudantes

## Inclui tudo do Java 8 e 11, mais:

### Records — classes de dados imutáveis
```java
// Antes (Java 8/11): precisava de construtor, getters, equals, hashCode, toString
public class Ponto {
    private final int x;
    private final int y;

    public Ponto(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }
    // + equals, hashCode, toString...
}

// Java 17 com Record: tudo isso em UMA linha!
public record Ponto(int x, int y) { }

// Usando o record:
var p = new Ponto(3, 4);
System.out.println(p.x()); // 3
System.out.println(p);     // Ponto[x=3, y=4]
```

### Pattern Matching com instanceof
```java
// Antes (Java 8): precisava fazer cast manualmente
Object obj = "Olá, Java!";
if (obj instanceof String) {
    String texto = (String) obj; // cast explícito
    System.out.println(texto.length());
}

// Java 17: pattern matching — declara e faz cast ao mesmo tempo!
if (obj instanceof String texto) {
    System.out.println(texto.length()); // 'texto' já é String aqui
}
```

### Sealed Classes — controle de herança
```java
// Sealed class: define EXATAMENTE quais classes podem estender ela
public sealed class Forma
    permits Circulo, Retangulo, Triangulo { }

// Cada subclasse deve ser final, sealed ou non-sealed
public final class Circulo extends Forma {
    private final double raio;
    public Circulo(double raio) { this.raio = raio; }
}

public final class Retangulo extends Forma {
    private final double largura, altura;
    public Retangulo(double largura, double altura) {
        this.largura = largura;
        this.altura = altura;
    }
}
```

### Text Blocks — strings multi-linha
```java
// Antes: concatenação feia e difícil de ler
String json = "{\n" +
              "  \"nome\": \"João\",\n" +
              "  \"idade\": 30\n" +
              "}";

// Java 17 com Text Block: muito mais limpo!
String json = """
        {
            "nome": "João",
            "idade": 30
        }
        """;
```

## pom.xml para Java 17
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

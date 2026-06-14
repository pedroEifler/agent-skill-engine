# Static Factory Methods — Referência para Estudantes

## O que são Static Factory Methods?

São métodos `static` que CRIAM e RETORNAM uma instância da classe,
em vez de você usar `new` diretamente. Você já usa isso sem perceber!

```java
// Exemplos que você já usa da API do Java:
List<String> lista = List.of("a", "b", "c");          // em vez de new ArrayList<>(...)
Optional<String> opcional = Optional.of("valor");      // em vez de new Optional<>(...)
LocalDate hoje = LocalDate.now();                       // em vez de new LocalDate(...)
```

## Por que usar Static Factory Method em vez de Construtor?

```java
// ❌ Com construtor: nomes não dizem nada sobre o significado
var pontoA = new Ponto(0, 0);
var pontoB = new Ponto(0, 0); // os dois são "iguais", mas o que significam?

// ✅ Com factory methods: o NOME explica a intenção!
public class Ponto {
    private final int x, y;

    // Construtor privado — só pode ser chamado pelos métodos factory
    private Ponto(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Factory method com nome descritivo: cria o ponto na origem.
     */
    public static Ponto origem() {
        return new Ponto(0, 0);
    }

    /**
     * Factory method: cria a partir de coordenadas cartesianas.
     */
    public static Ponto deCoordenadas(int x, int y) {
        return new Ponto(x, y);
    }

    /**
     * Factory method: cria a partir de coordenadas polares (raio e ângulo).
     * Faz a conversão internamente — o construtor não precisaria saber disso!
     */
    public static Ponto dePolar(double raio, double angulo) {
        int x = (int) (raio * Math.cos(angulo));
        int y = (int) (raio * Math.sin(angulo));
        return new Ponto(x, y);
    }
}

// Uso — muito mais claro!
var origem = Ponto.origem();
var ponto = Ponto.deCoordenadas(3, 4);
var pontoPolar = Ponto.dePolar(5.0, Math.PI / 4);
```

## Vantagens dos Static Factory Methods

```
1. NOMES descritivos — Ponto.origem() é mais claro que new Ponto(0, 0)

2. Não são OBRIGADOS a criar um novo objeto a cada chamada
   (podem reutilizar instâncias — cache, singleton, etc.)

   public static Boolean valueOf(boolean b) {
       return b ? Boolean.TRUE : Boolean.FALSE; // reutiliza instâncias existentes!
   }

3. Podem retornar SUBTIPOS — o tipo de retorno pode ser uma interface,
   e o método decide qual implementação concreta retornar.

   public static List<String> of(String... elementos) {
       return new ImmutableList<>(elementos); // retorna implementação específica
   }

4. Reduzem a verbosidade quando usados com generics
```

## Exemplo Completo: Validando na Criação

```java
/**
 * Email é um Value Object — só pode existir em estado válido.
 * O construtor é privado; só os factory methods criam instâncias.
 */
public final class Email {

    private final String endereco;

    private Email(String endereco) {
        this.endereco = endereco;
    }

    /**
     * Factory method que VALIDA antes de criar.
     * Se o email for inválido, lança exceção — nunca existe um Email inválido!
     */
    public static Email de(String endereco) {
        if (endereco == null || !endereco.matches("^[\\w.+-]+@[\\w-]+\\.[a-z]{2,}$")) {
            throw new EmailInvalidoException("Email inválido: " + endereco);
        }
        return new Email(endereco.toLowerCase());
    }

    @Override
    public String toString() { return endereco; }
}

// Uso:
var email = Email.de("usuario@exemplo.com"); // ok
// var invalido = Email.de("não é email");   // lança EmailInvalidoException
```

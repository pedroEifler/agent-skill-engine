# Lombok Builder Avançado — Referência para Estudantes

## @Builder com toBuilder() — copiar e modificar

```java
@Getter
@Builder(toBuilder = true)  // toBuilder = true permite copiar com modificações!
public class Configuracao {
    private final String host;
    private final int porta;
    @Builder.Default
    private final boolean ssl = true;
    @Builder.Default
    private final int timeout = 30;
}

// Uso: cria uma configuração e depois cria uma CÓPIA com uma mudança
var producao = Configuracao.builder()
    .host("api.empresa.com")
    .porta(443)
    .build();

// toBuilder() retorna um Builder já preenchido com os valores atuais!
var teste = producao.toBuilder()
    .host("api-teste.empresa.com") // só muda o host, o resto é igual
    .porta(8443)
    .ssl(false)
    .build();

System.out.println(producao.getHost()); // api.empresa.com
System.out.println(teste.getHost());    // api-teste.empresa.com
```

## @Builder com validação customizada

```java
@Getter
@ToString
@Builder(buildMethodName = "create") // muda o nome de .build() para .create()
public class Produto {

    private final String nome;
    private final BigDecimal preco;

    @Builder.Default
    private final int estoque = 0;

    /**
     * Lombok gera o método .build() / .create(), mas você pode sobrescrever
     * a classe Builder interna para adicionar validações!
     */
    public static class ProdutoBuilder {

        // Sobrescreve o método create() gerado pelo Lombok
        public Produto create() {
            // Validações antes de criar o objeto
            if (nome == null || nome.isBlank()) {
                throw new IllegalArgumentException("Nome do produto é obrigatório");
            }
            if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Preço deve ser maior que zero");
            }
            // Chama o método gerado pelo Lombok (que efetivamente cria o objeto)
            return new Produto(nome, preco, estoque == null ? 0 : estoque);
        }
    }
}

// Uso:
var produto = Produto.builder()
    .nome("Notebook")
    .preco(new BigDecimal("3000.00"))
    .estoque(10)
    .create(); // usa o nome customizado!

// Isso vai lançar exceção (validação customizada):
assertThrows(IllegalArgumentException.class, () ->
    Produto.builder().nome("").preco(BigDecimal.TEN).create()
);
```

## @Builder em Records (Java 16+)

```java
// Records com Lombok Builder — combinação poderosa!
@Builder
public record Endereco(
    String rua,
    String numero,
    String bairro,
    String cidade,
    String estado,
    String cep
) {}

// Uso:
var endereco = Endereco.builder()
    .rua("Avenida Brasil")
    .numero("1500")
    .bairro("Centro")
    .cidade("São Paulo")
    .estado("SP")
    .cep("01310-100")
    .build();
```

## @SuperBuilder — Builder com Herança

```java
// Para herança, use @SuperBuilder ao invés de @Builder!
@Getter
@SuperBuilder   // ← use este em vez de @Builder quando há herança!
public abstract class Veiculo {
    private final String marca;
    private final String modelo;
    private final int ano;
}

@Getter
@SuperBuilder   // ← também na subclasse!
public class Carro extends Veiculo {
    private final int numPortas;
    private final String combustivel;
}

// Uso — todos os campos da hierarquia disponíveis no Builder!
var carro = Carro.builder()
    .marca("Toyota")         // campo de Veiculo
    .modelo("Corolla")       // campo de Veiculo
    .ano(2024)               // campo de Veiculo
    .numPortas(4)            // campo de Carro
    .combustivel("Flex")     // campo de Carro
    .build();
```

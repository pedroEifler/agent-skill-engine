# Builder Pattern - Skills & Best Practices

## Intenção
Separar a construção de um objeto complexo da sua representação, permitindo criar diferentes representações com o mesmo processo de construção.

## Quando Usar
- Objetos com **muitos parâmetros** (especialmente opcionais).
- Quando a construção envolve **múltiplos passos** ou validações.
- Para evitar **construtores telescópicos** (múltiplos overloads).
- Para criar objetos **imutáveis** com muitos campos.

## Implementação Clássica (Java)

```java
public class Usuario {
    private final String nome;
    private final String email;
    private final Integer idade;
    private final String telefone;
    private final String endereco;

    private Usuario(Builder builder) {
        this.nome = builder.nome;
        this.email = builder.email;
        this.idade = builder.idade;
        this.telefone = builder.telefone;
        this.endereco = builder.endereco;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nome;
        private String email;
        private Integer idade;
        private String telefone;
        private String endereco;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder idade(Integer idade) {
            this.idade = idade;
            return this;
        }

        public Builder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public Builder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public Usuario build() {
            Objects.requireNonNull(nome, "Nome é obrigatório");
            Objects.requireNonNull(email, "Email é obrigatório");
            return new Usuario(this);
        }
    }
}
```

## Uso

```java
Usuario usuario = Usuario.builder()
    .nome("João Silva")
    .email("joao@email.com")
    .idade(30)
    .telefone("11999999999")
    .build();
```

## Builder com Lombok

```java
@Builder
@Getter
public class Usuario {
    private final String nome;
    private final String email;
    @Builder.Default
    private final Integer idade = 0;
    private final String telefone;
}
```

## Builder com Records (Java 16+)

```java
public record CriarUsuarioCommand(
    String nome,
    String email,
    Integer idade,
    String telefone
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nome;
        private String email;
        private Integer idade;
        private String telefone;

        public Builder nome(String nome) { this.nome = nome; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder idade(Integer idade) { this.idade = idade; return this; }
        public Builder telefone(String telefone) { this.telefone = telefone; return this; }

        public CriarUsuarioCommand build() {
            return new CriarUsuarioCommand(nome, email, idade, telefone);
        }
    }
}
```

## Best Practices

1. **Valide no `build()`** — garanta invariantes antes de criar o objeto.
2. **Retorne `this`** em cada setter do builder para fluent API.
3. **Builder como inner static class** — mantém coesão com a classe-alvo.
4. **Imutabilidade** — o objeto construído deve ser imutável (`final` fields, sem setters).
5. **Campos obrigatórios no construtor do Builder** — force o que é mandatório:
   ```java
   public Builder(String nome, String email) {
       this.nome = nome;
       this.email = email;
   }
   ```
6. **Use `@Builder` do Lombok** em projetos que já usam Lombok — evite boilerplate.
7. **Combine com Factory Method** — `Usuario.builder()` ao invés de `new Usuario.Builder()`.

## Anti-patterns a Evitar
- ❌ Builder para objetos com 1-2 campos (overkill).
- ❌ Builder mutável após `build()` — cada `build()` deve gerar um objeto novo.
- ❌ Expor o construtor da classe-alvo além do builder.
- ❌ Lógica de negócio no Builder — ele é apenas construtor.

## Variações
- **Step Builder**: força ordem de chamada via interfaces encadeadas.
- **Fluent Builder**: interface fluente para DSLs.
- **Director**: classe separada que orquestra a construção em cenários complexos.


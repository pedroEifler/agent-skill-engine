---
name: builder-pattern-student
description: >
  Use esta skill sempre que um estudante pedir para criar código usando o padrão Builder, quiser
  entender como construir objetos complexos passo a passo, ou quiser evitar construtores com muitos
  parâmetros. Triggers incluem: "builder pattern", "padrão builder", "construtor fluente", "fluent
  builder", "como evitar construtor com muitos parâmetros", "telescoping constructor", "builder java",
  "método encadeado", "method chaining", "builder com validação", "lombok builder", "design pattern
  builder java", "builder para estudar", "creational pattern builder", "como construir objetos complexos".
  Gera código em português com comentários explicativos sobre o "porquê" do padrão, comparando código
  SEM o padrão (o "telescoping constructor problem") e COM o padrão, mostrando o Builder manual e
  a versão com Lombok. SEMPRE use esta skill quando o estudante mencionar o padrão Builder.
---

# Skill: Design Pattern Builder para Estudantes 🏗️

Gera código Java demonstrando o padrão Builder, explicando o "porquê" com comparações entre o
problema (construtor telescópico) e a solução, com comentários didáticos em português.

---

## 1. O Problema que o Builder Resolve

Sempre comece mostrando o problema:

```java
// ❌ SEM o Builder — "Telescoping Constructor Problem" (problema do construtor telescópico)
// Quando um objeto tem muitos campos opcionais, você cria vários construtores:

public class Pedido {
    private Long id;
    private String cliente;
    private String produto;
    private int quantidade;
    private double desconto;
    private String cupom;
    private String enderecoEntrega;
    private boolean entregaExpressa;
    private String observacoes;

    // Construtor mínimo:
    public Pedido(String cliente, String produto, int quantidade) { ... }

    // Com desconto:
    public Pedido(String cliente, String produto, int quantidade, double desconto) { ... }

    // Com desconto e cupom:
    public Pedido(String cliente, String produto, int quantidade, double desconto, String cupom) { ... }

    // Com tudo... impossível de ler!
    public Pedido(String cliente, String produto, int quantidade, double desconto,
                  String cupom, String enderecoEntrega, boolean entregaExpressa,
                  String observacoes) { ... }
}

// Na hora de usar: qual valor vai em qual parâmetro???
var pedido = new Pedido("João", "Notebook", 1, 0.10, null, "Rua A, 123", true, null);
//                                                  ↑           ↑         ↑      ↑
//                              o que é cada um desses valores?????? Difícil de ler!
```

```
Problemas deste código:
1. Difícil de saber qual valor vai em qual parâmetro (null, null, true... o que é isso?)
2. Você precisa passar nulls para campos opcionais que não quer preencher
3. Adicionar um novo campo opcional exige atualizar todos os construtores existentes
4. Não há como garantir que o objeto foi criado em estado válido
```

---

## 2. O que é o Padrão Builder?

```
BUILDER: separa a CONSTRUÇÃO de um objeto complexo da sua REPRESENTAÇÃO.
Permite criar o objeto passo a passo, de forma legível e controlada.

Em vez de um construtor gigante, você usa uma sequência de métodos:
  Pedido.builder()
        .cliente("João")
        .produto("Notebook")
        .quantidade(1)
        .desconto(0.10)        ← cada método nomeia claramente o que está sendo definido
        .entregaExpressa(true)
        .build();              ← aqui o objeto é de fato criado e validado
```

---

## 3. Builder Manual — Do Zero

```java
/**
 * Classe Pedido com Builder interno.
 *
 * A classe principal é IMUTÁVEL (todos os campos são final) — depois de construída,
 * ninguém pode alterar seus valores. O Builder é o único que pode criar um Pedido.
 */
public class Pedido {

    // Todos os campos são final — o objeto é imutável após construção!
    private final Long id;
    private final String cliente;      // obrigatório
    private final String produto;      // obrigatório
    private final int quantidade;      // obrigatório
    private final double desconto;     // opcional (padrão: 0.0)
    private final String cupom;        // opcional (padrão: null)
    private final String enderecoEntrega; // opcional
    private final boolean entregaExpressa; // opcional (padrão: false)
    private final String observacoes;  // opcional (padrão: null)

    /**
     * Construtor PRIVADO: só pode ser chamado pelo Builder interno.
     * Isso garante que um Pedido só existe se foi criado via Builder.
     */
    private Pedido(Builder builder) {
        this.id = System.currentTimeMillis(); // gera ID automaticamente
        this.cliente = builder.cliente;
        this.produto = builder.produto;
        this.quantidade = builder.quantidade;
        this.desconto = builder.desconto;
        this.cupom = builder.cupom;
        this.enderecoEntrega = builder.enderecoEntrega;
        this.entregaExpressa = builder.entregaExpressa;
        this.observacoes = builder.observacoes;
    }

    /**
     * Ponto de entrada: cria um novo Builder para um Pedido.
     *
     * @param cliente  o nome do cliente (obrigatório)
     * @param produto  o produto solicitado (obrigatório)
     * @param quantidade  a quantidade desejada (obrigatório)
     * @return um novo Builder pronto para configuração
     */
    public static Builder builder(String cliente, String produto, int quantidade) {
        return new Builder(cliente, produto, quantidade);
    }

    // Getters (sem setters — o objeto é imutável!)
    public Long getId() { return id; }
    public String getCliente() { return cliente; }
    public String getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getDesconto() { return desconto; }
    public String getCupom() { return cupom; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public boolean isEntregaExpressa() { return entregaExpressa; }
    public String getObservacoes() { return observacoes; }

    @Override
    public String toString() {
        return "Pedido{cliente='" + cliente + "', produto='" + produto +
            "', quantidade=" + quantidade + ", desconto=" + desconto +
            ", entregaExpressa=" + entregaExpressa + "}";
    }

    /**
     * Builder interno: acumula os valores e constrói o Pedido no final.
     *
     * Padrão de nomes: cada método tem o mesmo nome do campo que define.
     * Todos os métodos retornam "this" (o próprio builder) para permitir
     * o encadeamento: .desconto(0.1).cupom("PROMO10").entregaExpressa(true)
     */
    public static class Builder {

        // Campos OBRIGATÓRIOS — definidos no construtor do Builder
        private final String cliente;
        private final String produto;
        private final int quantidade;

        // Campos OPCIONAIS — têm valores padrão sensatos
        private double desconto = 0.0;           // sem desconto por padrão
        private String cupom = null;             // sem cupom por padrão
        private String enderecoEntrega = null;   // sem endereço por padrão
        private boolean entregaExpressa = false; // entrega normal por padrão
        private String observacoes = null;       // sem observações por padrão

        /**
         * Construtor do Builder: recebe apenas os campos obrigatórios.
         * Campos opcionais ficam com seus valores padrão.
         */
        private Builder(String cliente, String produto, int quantidade) {
            // Valida os campos obrigatórios logo na entrada!
            if (cliente == null || cliente.isBlank())
                throw new IllegalArgumentException("Cliente é obrigatório");
            if (produto == null || produto.isBlank())
                throw new IllegalArgumentException("Produto é obrigatório");
            if (quantidade <= 0)
                throw new IllegalArgumentException("Quantidade deve ser maior que zero");

            this.cliente = cliente;
            this.produto = produto;
            this.quantidade = quantidade;
        }

        // Cada método abaixo define UM campo opcional e retorna "this"
        // para permitir o encadeamento de chamadas (fluent interface)

        public Builder desconto(double desconto) {
            if (desconto < 0 || desconto > 1)
                throw new IllegalArgumentException("Desconto deve ser entre 0 e 1");
            this.desconto = desconto;
            return this; // retorna o próprio Builder para encadeamento!
        }

        public Builder cupom(String cupom) {
            this.cupom = cupom;
            return this;
        }

        public Builder enderecoEntrega(String enderecoEntrega) {
            this.enderecoEntrega = enderecoEntrega;
            return this;
        }

        public Builder entregaExpressa(boolean entregaExpressa) {
            this.entregaExpressa = entregaExpressa;
            return this;
        }

        public Builder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        /**
         * Constrói e retorna o Pedido.
         * Aqui podem ser feitas validações de consistência entre os campos.
         *
         * @return o Pedido construído e validado
         * @throws IllegalStateException se os campos estão inconsistentes
         */
        public Pedido build() {
            // Validação de consistência: entrega expressa exige endereço!
            if (entregaExpressa && (enderecoEntrega == null || enderecoEntrega.isBlank())) {
                throw new IllegalStateException(
                    "Endereço de entrega é obrigatório para entrega expressa");
            }
            return new Pedido(this);
        }
    }
}
```

```java
// ✅ COM o Builder — legível, claro e fácil de entender!
var pedidoSimples = Pedido.builder("Maria", "Teclado", 2)
    .build();

var pedidoCompleto = Pedido.builder("João", "Notebook", 1)
    .desconto(0.10)                    // ← cada linha nomeia claramente o campo!
    .cupom("PROMO10")
    .enderecoEntrega("Rua das Flores, 42")
    .entregaExpressa(true)
    .observacoes("Entregar no portão azul")
    .build();

System.out.println(pedidoCompleto);
```

---

## 4. Builder com Lombok — A Forma Mais Rápida

```xml
<!-- pom.xml: adicione o Lombok para gerar o Builder automaticamente! -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>
    <scope>provided</scope>
</dependency>
```

```java
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Com Lombok, você NÃO precisa escrever o Builder manualmente!
 * As anotações geram tudo automaticamente em tempo de compilação.
 */
@Getter           // gera todos os getters automaticamente
@ToString         // gera toString() automaticamente
@Builder          // gera toda a classe Builder interna automaticamente!
public class Email {

    private final String para;          // obrigatório
    private final String assunto;       // obrigatório
    private final String corpo;         // obrigatório

    @Builder.Default                    // define o valor padrão para campos opcionais
    private final boolean html = false;

    @Builder.Default
    private final List<String> copia = new ArrayList<>();

    @Builder.Default
    private final int tentativasReenvio = 3;
}

// Uso — o Lombok gera exatamente este código de uso:
var email = Email.builder()
    .para("usuario@exemplo.com")
    .assunto("Bem-vindo!")
    .corpo("Olá, seja bem-vindo ao sistema!")
    .html(true)
    .build();

System.out.println(email);
```

---

## 5. Builder com Herança — Padrão de Self-Type

```java
/**
 * Quando você precisa de Builders que herdam uns dos outros,
 * use o padrão "recursive generics" para manter o encadeamento funcionando.
 */
public abstract class Notificacao {

    protected final String destinatario;
    protected final String mensagem;
    protected final boolean urgente;

    protected Notificacao(AbstractBuilder<?> builder) {
        this.destinatario = builder.destinatario;
        this.mensagem = builder.mensagem;
        this.urgente = builder.urgente;
    }

    // Builder abstrato genérico — T é o tipo do Builder concreto (self-type)
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {

        private String destinatario;
        private String mensagem;
        private boolean urgente = false;

        public T destinatario(String destinatario) {
            this.destinatario = destinatario;
            return self(); // retorna o tipo CONCRETO, não AbstractBuilder!
        }

        public T mensagem(String mensagem) {
            this.mensagem = mensagem;
            return self();
        }

        public T urgente(boolean urgente) {
            this.urgente = urgente;
            return self();
        }

        // Cada subclasse retorna "this" com o tipo correto
        @SuppressWarnings("unchecked")
        protected T self() { return (T) this; }

        public abstract Notificacao build();
    }
}

// Subclasse com campos extras — o encadeamento ainda funciona!
public class NotificacaoEmail extends Notificacao {

    private final String assunto;

    private NotificacaoEmail(Builder builder) {
        super(builder);
        this.assunto = builder.assunto;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBuilder<Builder> {
        private String assunto;

        public Builder assunto(String assunto) {
            this.assunto = assunto;
            return this; // retorna Builder, não AbstractBuilder
        }

        @Override
        public NotificacaoEmail build() { return new NotificacaoEmail(this); }
    }
}

// Uso: o encadeamento funciona com campos de AMBAS as classes!
var notificacao = NotificacaoEmail.builder()
    .destinatario("user@exemplo.com") // campo da classe base
    .mensagem("Seu pedido foi aprovado!") // campo da classe base
    .urgente(true)                   // campo da classe base
    .assunto("Aprovação de Pedido")  // campo da subclasse
    .build();
```

---

## 6. Comparação: Construtor vs Builder vs Lombok

```
┌─────────────────────┬────────────────────────────────────────────────────┐
│ Construtor simples  │ ✅ Ok para classes com 2-3 parâmetros obrigatórios │
│                     │ ❌ Impossível de ler com muitos parâmetros          │
├─────────────────────┼────────────────────────────────────────────────────┤
│ Builder manual      │ ✅ Controle total, imutabilidade, validação         │
│                     │ ✅ Sem dependência extra                            │
│                     │ ❌ Muito código repetitivo para escrever            │
├─────────────────────┼────────────────────────────────────────────────────┤
│ Builder com Lombok  │ ✅ Zero boilerplate, gerado automaticamente         │
│                     │ ✅ @Builder.Default para valores padrão             │
│                     │ ❌ Depende da biblioteca Lombok                     │
│                     │ ❌ Menos controle sobre validações complexas        │
└─────────────────────┴────────────────────────────────────────────────────┘
```

---

## 7. Checklist Antes de Gerar

- [ ] Mostrar o "telescoping constructor problem" como motivação?
- [ ] Construtor do Builder recebe apenas os campos obrigatórios?
- [ ] Cada método setter retorna `this` para encadeamento?
- [ ] Validações: campos obrigatórios no construtor do Builder, consistência no `build()`?
- [ ] Classe principal imutável (campos `final`, sem setters)?
- [ ] Comentário explicando por que cada método retorna `this`?

---

## 8. Formato de Entrega

1. **O problema**: o construtor telescópico com código difícil de ler
2. **A solução**: Builder manual passo a passo com comentários
3. **Versão Lombok**: mesma classe com `@Builder` (se o projeto usa Lombok)
4. **Exemplo de uso**: mostrando a legibilidade ganhou
5. **Dica**: quando usar construtor simples vs Builder

Consulte os arquivos de referência:
- `references/design-patterns/lombok-avancado.md` — @Builder com herança, toBuilder(), validações no Lombok
- `references/design-patterns/builder-gof.md` — Builder GoF original com Director e steps separados
- `references/design-patterns/testes.md` — Testando classes construídas com Builder

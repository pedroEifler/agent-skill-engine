---
name: clean-arch-student
description: >
  Use esta skill sempre que um estudante pedir para criar um projeto com Clean Architecture (Arquitetura Limpa),
  Hexagonal Architecture (Ports & Adapters), ou qualquer estrutura de projeto baseada nos princípios de
  separação de responsabilidades e inversão de dependência em Java. Triggers incluem: "clean architecture",
  "arquitetura limpa", "hexagonal", "ports and adapters", "como organizar meu projeto", "separar regras de negócio",
  "domain driven design para iniciantes", "DDD básico", "como não misturar camadas", "projeto bem estruturado Java",
  "como desacoplar o banco de dados", "use case Java". Gera código em português com comentários explicativos
  nas camadas, anotações das responsabilidades de cada classe, e exemplos didáticos do fluxo de dependências.
  SEMPRE use esta skill quando o estudante mencionar arquitetura de software ou organização de projetos Java.
---

# Skill: Clean Architecture para Estudantes 🏛️

Gera projetos Java com Clean Architecture, explicando cada camada com comentários didáticos em português.
O objetivo é ensinar o "porquê" de cada decisão arquitetural.

---

## 1. O que é Clean Architecture?

Sempre explique o conceito antes de gerar o código:

```
A ideia central: as regras de negócio NÃO devem depender de frameworks,
bancos de dados, ou interfaces de usuário. São elas que ditam as regras!

Camadas (de dentro para fora):
┌─────────────────────────────────────────────┐
│            Frameworks & Drivers             │  ← Spring, JPA, HTTP, CLI
│   ┌─────────────────────────────────────┐   │
│   │       Interface Adapters            │   │  ← Controllers, Presenters, Gateways
│   │   ┌─────────────────────────────┐   │   │
│   │   │      Application            │   │   │  ← Use Cases (regras de aplicação)
│   │   │   ┌─────────────────────┐   │   │   │
│   │   │   │      Domain         │   │   │   │  ← Entidades, regras de negócio
│   │   │   └─────────────────────┘   │   │   │
│   │   └─────────────────────────────┘   │   │
│   └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘

REGRA DE OURO: as setas de dependência sempre apontam para dentro.
O Domain não conhece ninguém. O Application conhece só o Domain. E assim por diante.
```

---

## 2. Estrutura de Pastas

```
meu-projeto/
├── src/main/java/br/com/projeto/
│   │
│   ├── domain/                          ← 🔴 NÚCLEO: zero dependências externas
│   │   ├── entity/                      ← Entidades de negócio (sem @Entity do JPA!)
│   │   │   └── Produto.java
│   │   ├── valueobject/                 ← Objetos de valor imutáveis
│   │   │   └── Dinheiro.java
│   │   ├── exception/                   ← Exceções de domínio puras
│   │   │   └── EstoqueInsuficienteException.java
│   │   └── port/                        ← Interfaces que o domínio define
│   │       ├── out/                     ← O que o domínio precisa do mundo externo
│   │       │   └── ProdutoRepositoryPort.java
│   │       └── in/                      ← O que o mundo externo pode pedir ao domínio
│   │           └── CriarProdutoUseCase.java
│   │
│   ├── application/                     ← 🟡 CASOS DE USO: orquestra o domínio
│   │   ├── usecase/
│   │   │   └── CriarProdutoUseCaseImpl.java
│   │   └── dto/                         ← Dados de entrada/saída dos use cases
│   │       ├── CriarProdutoInput.java
│   │       └── ProdutoOutput.java
│   │
│   ├── adapter/                         ← 🟢 ADAPTADORES: traduz o mundo externo
│   │   ├── in/                          ← Adapta entrada externa → use case
│   │   │   └── web/
│   │   │       └── ProdutoController.java
│   │   └── out/                         ← Adapta use case → mundo externo
│   │       └── persistence/
│   │           ├── ProdutoRepositoryAdapter.java
│   │           ├── ProdutoJpaRepository.java   ← interface Spring Data
│   │           └── ProdutoJpaEntity.java       ← @Entity do JPA fica aqui!
│   │
│   └── config/                          ← 🔵 CONFIGURAÇÃO: monta tudo junto
│       └── BeanConfig.java
│
├── src/test/java/br/com/projeto/
│   ├── domain/
│   ├── application/
│   └── adapter/
└── pom.xml
```

---

## 3. Camada de Domínio — O Coração do Sistema

```java
/**
 * Entidade de domínio que representa um Produto.
 *
 * IMPORTANTE: esta classe NÃO tem nenhuma anotação do Spring, JPA ou qualquer framework!
 * Ela é Java puro — pode ser testada sem subir nenhum servidor ou banco de dados.
 *
 * As regras de negócio ficam AQUI, não no service!
 */
public class Produto {

    private final Long id;
    private String nome;
    private Dinheiro preco;    // Objeto de Valor, não BigDecimal puro
    private int quantidadeEmEstoque;

    // Construtor para criar um produto NOVO (sem id ainda)
    public Produto(String nome, Dinheiro preco, int quantidadeInicial) {
        validarNome(nome);
        validarPreco(preco);
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = quantidadeInicial;
        this.id = null;
    }

    // Construtor para reconstituir um produto EXISTENTE (do banco de dados)
    public Produto(Long id, String nome, Dinheiro preco, int quantidade) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = quantidade;
    }

    /**
     * Regra de negócio: realiza uma venda reduzindo o estoque.
     * Esta lógica pertence ao Produto, não a um ProdutoService!
     *
     * @param quantidade a quantidade a ser vendida
     * @throws EstoqueInsuficienteException se não houver estoque suficiente
     */
    public void vender(int quantidade) {
        // A regra de negócio fica na entidade — não no controller nem no service!
        if (quantidade > this.quantidadeEmEstoque) {
            throw new EstoqueInsuficienteException(
                "Estoque insuficiente. Disponível: " + quantidadeEmEstoque +
                ", Solicitado: " + quantidade
            );
        }
        this.quantidadeEmEstoque -= quantidade;
    }

    // Validações privadas — protegem os invariantes do domínio
    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio");
        }
    }

    private void validarPreco(Dinheiro preco) {
        if (preco == null) {
            throw new IllegalArgumentException("Preço não pode ser nulo");
        }
    }

    // Getters (sem setters públicos — muda estado só por métodos de negócio!)
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Dinheiro getPreco() { return preco; }
    public int getQuantidadeEmEstoque() { return quantidadeEmEstoque; }
}
```

```java
/**
 * Objeto de Valor (Value Object) que representa dinheiro.
 *
 * Value Objects são IMUTÁVEIS e a igualdade é baseada no VALOR, não na identidade.
 * Dois Dinheiro(100, "BRL") são iguais, mesmo sendo objetos diferentes na memória.
 */
public final class Dinheiro {

    private final BigDecimal valor;
    private final String moeda;  // ex: "BRL", "USD"

    public Dinheiro(BigDecimal valor, String moeda) {
        // Garante que o valor seja sempre positivo e com 2 casas decimais
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor monetário não pode ser negativo");
        }
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.moeda = Objects.requireNonNull(moeda, "Moeda não pode ser nula");
    }

    // Operação imutável: retorna um NOVO Dinheiro, não altera este
    public Dinheiro somar(Dinheiro outro) {
        if (!this.moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException("Não é possível somar moedas diferentes");
        }
        return new Dinheiro(this.valor.add(outro.valor), this.moeda);
    }

    // Igualdade baseada no valor, não na referência de memória
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dinheiro)) return false;
        Dinheiro d = (Dinheiro) o;
        return valor.compareTo(d.valor) == 0 && moeda.equals(d.moeda);
    }

    @Override
    public int hashCode() { return Objects.hash(valor, moeda); }

    public BigDecimal getValor() { return valor; }
    public String getMoeda() { return moeda; }
}
```

---

## 4. Portas (Ports) — Contratos do Domínio

```java
/**
 * Porta de SAÍDA (Output Port): define o que o domínio precisa do mundo externo.
 *
 * Pense assim: o domínio diz "eu preciso salvar produtos, mas NÃO QUERO SABER como".
 * Quem vai implementar isso é o adaptador de persistência lá fora.
 *
 * Fica no pacote domain/port/out/
 */
public interface ProdutoRepositoryPort {

    /**
     * Salva um produto e retorna o produto com ID gerado.
     */
    Produto salvar(Produto produto);

    /**
     * Busca um produto pelo ID. Retorna Optional pois pode não existir.
     */
    Optional<Produto> buscarPorId(Long id);

    /**
     * Lista todos os produtos disponíveis.
     */
    List<Produto> listarTodos();
}
```

```java
/**
 * Porta de ENTRADA (Input Port / Use Case): define o que pode ser feito no sistema.
 *
 * É a interface que os adaptadores de entrada (controllers) usam.
 * Fica no pacote domain/port/in/
 */
public interface CriarProdutoUseCase {

    /**
     * Cria um novo produto no sistema.
     *
     * @param input os dados para criação do produto
     * @return os dados do produto criado, incluindo o ID gerado
     */
    ProdutoOutput executar(CriarProdutoInput input);
}
```

---

## 5. Camada de Application — Casos de Uso

```java
/**
 * Implementação do caso de uso "Criar Produto".
 *
 * Esta classe ORQUESTRA: ela usa as portas para coordenar as operações.
 * Note que ela usa interfaces (ports), nunca implementações concretas!
 * Isso é Inversão de Dependência (o "D" do SOLID).
 */
@Service  // O @Service do Spring pode ficar aqui — é apenas uma marcação
public class CriarProdutoUseCaseImpl implements CriarProdutoUseCase {

    // Usa a INTERFACE (porta), nunca a implementação concreta!
    private final ProdutoRepositoryPort produtoRepository;

    // Injeção via construtor — boa prática e facilita os testes
    public CriarProdutoUseCaseImpl(ProdutoRepositoryPort produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public ProdutoOutput executar(CriarProdutoInput input) {
        // 1. Converte o DTO de entrada em objeto de domínio
        var dinheiro = new Dinheiro(input.preco(), "BRL");
        var produto = new Produto(input.nome(), dinheiro, input.quantidadeInicial());

        // 2. Chama a porta de saída para persistir (não sabe SE é banco, arquivo, etc.)
        var produtoSalvo = produtoRepository.salvar(produto);

        // 3. Converte o objeto de domínio em DTO de saída
        return ProdutoOutput.de(produtoSalvo);
    }
}
```

```java
/**
 * DTO de entrada para o caso de uso CriarProduto.
 * Carrega apenas os dados necessários para a operação.
 */
public record CriarProdutoInput(String nome, BigDecimal preco, int quantidadeInicial) { }

/**
 * DTO de saída do caso de uso. Expõe apenas o necessário para o mundo externo.
 */
public record ProdutoOutput(Long id, String nome, BigDecimal preco, int estoque) {

    // Método de fábrica: converte entidade de domínio → DTO
    public static ProdutoOutput de(Produto produto) {
        return new ProdutoOutput(
            produto.getId(),
            produto.getNome(),
            produto.getPreco().getValor(),
            produto.getQuantidadeEmEstoque()
        );
    }
}
```

---

## 6. Camada de Adapter — Ligando ao Mundo Externo

```java
/**
 * Adaptador de ENTRADA: recebe requisições HTTP e chama o Use Case.
 *
 * Responsabilidade única: traduzir HTTP → Use Case e Use Case → HTTP.
 * Não tem regra de negócio aqui!
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    // Usa a INTERFACE do use case, nunca a implementação!
    private final CriarProdutoUseCase criarProdutoUseCase;

    public ProdutoController(CriarProdutoUseCase criarProdutoUseCase) {
        this.criarProdutoUseCase = criarProdutoUseCase;
    }

    @PostMapping
    public ResponseEntity<ProdutoOutput> criar(@RequestBody @Valid CriarProdutoRequest request) {
        // Converte request HTTP → input do use case
        var input = new CriarProdutoInput(request.nome(), request.preco(), request.quantidadeInicial());

        // Chama o use case (não sabe o que acontece lá dentro!)
        var output = criarProdutoUseCase.executar(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }
}
```

```java
/**
 * Adaptador de SAÍDA: implementa a porta do domínio usando JPA.
 *
 * Este é o único lugar onde JPA/Spring Data aparece no lado de persistência.
 * Traduz: Entidade de Domínio ↔ Entidade JPA
 */
@Component
public class ProdutoRepositoryAdapter implements ProdutoRepositoryPort {

    // Repositório Spring Data — fica AQUI, não no domínio!
    private final ProdutoJpaRepository jpaRepository;

    public ProdutoRepositoryAdapter(ProdutoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Produto salvar(Produto produto) {
        // 1. Converte objeto de domínio → entidade JPA
        var entidadeJpa = ProdutoJpaEntity.de(produto);

        // 2. Salva no banco via Spring Data
        var salvo = jpaRepository.save(entidadeJpa);

        // 3. Converte entidade JPA → objeto de domínio e retorna
        return salvo.paraDominio();
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return jpaRepository.findById(id)
            .map(ProdutoJpaEntity::paraDominio); // converte para domínio
    }

    @Override
    public List<Produto> listarTodos() {
        return jpaRepository.findAll().stream()
            .map(ProdutoJpaEntity::paraDominio)
            .collect(Collectors.toList());
    }
}
```

```java
/**
 * Entidade JPA — AQUI ficam as anotações @Entity, @Table, etc.
 * O domínio nunca vê isso!
 */
@Entity
@Table(name = "produto")
public class ProdutoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private int quantidadeEmEstoque;

    // Converte domínio → JPA
    public static ProdutoJpaEntity de(Produto produto) {
        var entidade = new ProdutoJpaEntity();
        entidade.id = produto.getId();
        entidade.nome = produto.getNome();
        entidade.preco = produto.getPreco().getValor();
        entidade.quantidadeEmEstoque = produto.getQuantidadeEmEstoque();
        return entidade;
    }

    // Converte JPA → domínio
    public Produto paraDominio() {
        return new Produto(id, nome, new Dinheiro(preco, "BRL"), quantidadeEmEstoque);
    }
}
```

---

## 7. Checklist Antes de Gerar

- [ ] Domínio sem nenhuma importação de framework (Spring, JPA, etc.)?
- [ ] Regras de negócio nas entidades de domínio?
- [ ] Portas (interfaces) definidas no domínio?
- [ ] Use cases usando apenas interfaces (ports)?
- [ ] Conversão domínio ↔ JPA no adaptador, não no domínio?
- [ ] Controller usando interface do use case?
- [ ] Comentários explicando o "porquê" de cada camada?

---

## 8. Formato de Entrega

1. **Diagrama de camadas** textual explicando o fluxo
2. **Estrutura de pastas** comentada
3. **Código por camada** — Domain → Application → Adapter → Config
4. **Dica**: explique o que aconteceria se misturássemos as camadas

Consulte os arquivos de referência para exemplos completos:
- `references/domain.md` — Value Objects, Aggregates, Domain Events
- `references/application.md` — Use Cases complexos, CQRS básico
- `references/adapters.md` — Adapters Web, Persistence, Messaging
- `references/tests.md` — Como testar cada camada isoladamente

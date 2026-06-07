---
name: mvc-student
description: >
  Use esta skill sempre que um estudante pedir para criar um projeto com padrão MVC (Model-View-Controller),
  seja com Spring MVC, Thymeleaf, JSP, ou qualquer implementação MVC em Java. Triggers incluem: "padrão MVC",
  "model view controller", "Spring MVC", "Thymeleaf", "projeto MVC Java", "como funciona o MVC",
  "separar model view controller", "MVC com Spring Boot", "página HTML com Spring", "formulário com Spring MVC",
  "MVC para estudar", "template Thymeleaf", "controller retornando view", "mvc jsp java". Gera código em
  português com comentários explicativos em cada camada (Model, View, Controller), exemplos de formulários,
  validações e fluxo de dados. SEMPRE use esta skill quando o estudante mencionar MVC ou Spring MVC,
  mesmo que o pedido pareça simples.
---

# Skill: MVC para Estudantes 🎨

Gera projetos Java com padrão MVC, explicando cada camada com comentários didáticos em português.
Foco em Spring MVC com Thymeleaf para renderização de páginas.

---

## 1. O que é MVC?

Sempre explique o conceito antes de gerar o código:

```
MVC divide a aplicação em três partes com responsabilidades bem definidas:

┌─────────────┐     requisição      ┌─────────────┐
│   Browser   │ ─────────────────── │  Controller │
│  (usuário)  │                     │  (C)        │
└─────────────┘                     └──────┬──────┘
       ▲                                   │ chama
       │ HTML renderizado                  ▼
┌──────┴──────┐     dados           ┌─────────────┐
│    View     │ ◄─────────────────── │    Model    │
│  (V) HTML   │                     │  (M) dados  │
└─────────────┘                     └─────────────┘

MODEL      → Os dados e as regras de negócio (classes Java, entidades)
VIEW       → O que o usuário vê (HTML com Thymeleaf ou JSP)
CONTROLLER → O intermediário: recebe requisições e devolve respostas
```

---

## 2. Estrutura de Projeto MVC

```
meu-projeto/
├── src/main/java/br/com/projeto/
│   ├── controller/                    ← Recebe requisições HTTP, chama service, manda para view
│   │   └── ProdutoController.java
│   ├── service/                       ← Regras de negócio (entre controller e repository)
│   │   └── ProdutoService.java
│   ├── repository/                    ← Acesso ao banco de dados
│   │   └── ProdutoRepository.java
│   ├── model/                         ← Classes que representam os dados (o "M" do MVC)
│   │   └── Produto.java
│   └── MeuProjetoApplication.java
│
├── src/main/resources/
│   ├── templates/                     ← Views HTML com Thymeleaf (o "V" do MVC)
│   │   ├── produto/
│   │   │   ├── lista.html             ← Página que lista os produtos
│   │   │   ├── formulario.html        ← Formulário para criar/editar
│   │   │   └── detalhes.html         ← Detalhes de um produto
│   │   └── layout/
│   │       └── base.html             ← Layout base reutilizável
│   ├── static/                        ← Arquivos estáticos (CSS, JS, imagens)
│   │   ├── css/
│   │   └── js/
│   └── application.properties
└── pom.xml
```

---

## 3. O Model — Representando os Dados

```java
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * MODEL: representa os dados de um Produto.
 *
 * No MVC, o Model carrega os dados entre o Controller e a View.
 * Aqui também colocamos a entidade JPA para persistir no banco.
 */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank → o nome não pode ficar vazio (validação de formulário)
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    // @NotNull e @Positive garantem um preço válido no formulário
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @Column(nullable = false)
    private BigDecimal preco;

    @Size(max = 500, message = "Descrição pode ter no máximo 500 caracteres")
    private String descricao;

    // Construtor padrão obrigatório para o JPA e Thymeleaf
    public Produto() {}

    public Produto(String nome, BigDecimal preco) {
        this.nome = nome;
        this.preco = preco;
    }

    // Getters e Setters (necessários para o Thymeleaf popular o formulário)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
```

---

## 4. O Controller — O Intermediário

```java
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: recebe as requisições do browser e decide o que fazer.
 *
 * @Controller (não @RestController!) retorna o nome de um template HTML,
 * não JSON. É a diferença entre MVC tradicional e API REST.
 */
@Controller
// Define o prefixo de URL para todos os métodos desta classe
@RequestMapping("/produtos")
public class ProdutoController {

    // O service é injetado — controller não acessa o banco diretamente!
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Exibe a lista de todos os produtos.
     * GET /produtos
     *
     * @param model objeto que carrega dados do controller para a view (HTML)
     * @return nome do template Thymeleaf a renderizar ("produto/lista")
     */
    @GetMapping
    public String listar(Model model) {
        // Adiciona a lista no "model" — a view acessa com th:each="produto : ${produtos}"
        model.addAttribute("produtos", produtoService.listarTodos());
        // Retorna o nome do arquivo: src/main/resources/templates/produto/lista.html
        return "produto/lista";
    }

    /**
     * Exibe o formulário para criar um novo produto.
     * GET /produtos/novo
     */
    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        // Passa um objeto vazio para o Thymeleaf preencher o formulário
        model.addAttribute("produto", new Produto());
        return "produto/formulario";
    }

    /**
     * Processa o envio do formulário de criação.
     * POST /produtos
     *
     * @param produto preenchido pelo Thymeleaf com os dados do formulário
     * @param bindingResult contém os erros de validação (se houver)
     */
    @PostMapping
    public String salvar(@ModelAttribute @Valid Produto produto,
                         BindingResult bindingResult) {
        // Se houver erros de validação, volta ao formulário com as mensagens de erro
        if (bindingResult.hasErrors()) {
            return "produto/formulario";
        }
        produtoService.salvar(produto);
        // Redireciona para a lista após salvar (padrão PRG: Post/Redirect/Get)
        return "redirect:/produtos";
    }

    /**
     * Exibe o formulário de edição com os dados do produto preenchidos.
     * GET /produtos/{id}/editar
     */
    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        var produto = produtoService.buscarPorId(id); // lança exceção se não encontrar
        model.addAttribute("produto", produto);
        return "produto/formulario";
    }

    /**
     * Processa a exclusão de um produto.
     * DELETE /produtos/{id}  (ou POST via formulário HTML)
     */
    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        return "redirect:/produtos";
    }
}
```

---

## 5. A View — Templates Thymeleaf

```html
<!-- templates/produto/lista.html -->
<!DOCTYPE html>
<!--
  VIEW: o que o usuário vê.
  th:* são atributos do Thymeleaf — processados no servidor antes de enviar ao browser.
-->
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Lista de Produtos</title>
    <!-- Bootstrap para estilização rápida -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h1>Produtos</h1>
    <a th:href="@{/produtos/novo}" class="btn btn-primary mb-3">Novo Produto</a>

    <!-- th:if verifica se a lista está vazia -->
    <p th:if="${#lists.isEmpty(produtos)}" class="text-muted">Nenhum produto cadastrado.</p>

    <!-- th:each itera sobre a lista de produtos enviada pelo controller -->
    <table class="table" th:unless="${#lists.isEmpty(produtos)}">
        <thead>
            <tr>
                <th>ID</th><th>Nome</th><th>Preço</th><th>Ações</th>
            </tr>
        </thead>
        <tbody>
            <!-- Para cada produto na lista ${produtos}: -->
            <tr th:each="produto : ${produtos}">
                <!-- th:text insere o valor da variável como texto -->
                <td th:text="${produto.id}"></td>
                <td th:text="${produto.nome}"></td>
                <!-- Formata o preço com 2 casas decimais -->
                <td th:text="${#numbers.formatDecimal(produto.preco, 1, 2)}"></td>
                <td>
                    <!-- th:href gera a URL dinamicamente com o ID do produto -->
                    <a th:href="@{/produtos/{id}/editar(id=${produto.id})}"
                       class="btn btn-sm btn-warning">Editar</a>
                    <!-- Formulário para deletar (HTML não suporta DELETE, usamos POST) -->
                    <form th:action="@{/produtos/{id}/deletar(id=${produto.id})}"
                          method="post" style="display:inline">
                        <button type="submit" class="btn btn-sm btn-danger"
                                onclick="return confirm('Confirmar exclusão?')">
                            Deletar
                        </button>
                    </form>
                </td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>
```

```html
<!-- templates/produto/formulario.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <!-- th:text muda o título dependendo se é criação ou edição -->
    <title th:text="${produto.id != null} ? 'Editar Produto' : 'Novo Produto'">Produto</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h1 th:text="${produto.id != null} ? 'Editar Produto' : 'Novo Produto'">Produto</h1>

    <!--
      th:object vincula o formulário ao objeto "produto" do Model.
      th:action define a URL de envio — POST /produtos (novo) ou POST /produtos/{id} (edição).
    -->
    <form th:action="@{/produtos}" th:object="${produto}" method="post">

        <!-- Campo oculto para enviar o ID na edição -->
        <input type="hidden" th:field="*{id}">

        <div class="mb-3">
            <label class="form-label">Nome</label>
            <!--
              th:field="*{nome}" vincula ao campo "nome" do objeto produto.
              O * referencia o th:object do formulário.
            -->
            <input type="text" th:field="*{nome}" class="form-control"
                   th:classappend="${#fields.hasErrors('nome')} ? 'is-invalid'">
            <!-- Mostra a mensagem de erro de validação se houver -->
            <div class="invalid-feedback" th:errors="*{nome}"></div>
        </div>

        <div class="mb-3">
            <label class="form-label">Preço</label>
            <input type="number" step="0.01" th:field="*{preco}" class="form-control"
                   th:classappend="${#fields.hasErrors('preco')} ? 'is-invalid'">
            <div class="invalid-feedback" th:errors="*{preco}"></div>
        </div>

        <div class="mb-3">
            <label class="form-label">Descrição</label>
            <textarea th:field="*{descricao}" class="form-control" rows="3"></textarea>
        </div>

        <button type="submit" class="btn btn-success">Salvar</button>
        <a th:href="@{/produtos}" class="btn btn-secondary">Cancelar</a>
    </form>
</div>
</body>
</html>
```

---

## 6. application.properties para MVC

```properties
# ===== Banco de Dados H2 (em memória para estudar) =====
spring.datasource.url=jdbc:h2:mem:mvcdb
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# ===== Thymeleaf =====
# Durante desenvolvimento, desabilita o cache para ver mudanças na view sem reiniciar
spring.thymeleaf.cache=false

# ===== Servidor =====
server.port=8080
```

---

## 7. pom.xml para Spring MVC + Thymeleaf

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <!-- Spring MVC + Thymeleaf: renderiza páginas HTML no servidor -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <!-- Spring Web: suporte a controllers, requisições HTTP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- Spring Data JPA: acesso ao banco de dados -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <!-- Validação de formulários (@NotBlank, @Size, etc.) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <!-- Banco H2 em memória (ótimo para estudar!) -->
    <dependency>
        <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- Testes -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 8. Checklist Antes de Gerar

- [ ] Versão do Spring Boot e Java identificadas?
- [ ] `@Controller` (não `@RestController`) nos controllers MVC?
- [ ] Model com getters/setters (obrigatório para Thymeleaf)?
- [ ] Validações `@Valid` + `BindingResult` no controller?
- [ ] Templates Thymeleaf com `th:object`, `th:field` e `th:errors`?
- [ ] Padrão PRG (Post/Redirect/Get) após salvar/deletar?
- [ ] `spring.thymeleaf.cache=false` no `application.properties`?

---

## 9. Formato de Entrega

1. **Explicação do fluxo** MVC: Browser → Controller → Service → View
2. **Estrutura de pastas** comentada
3. **pom.xml** com dependências corretas
4. **Model** (entidade com validações)
5. **Controller** com comentários em cada método
6. **Templates Thymeleaf** com `th:*` explicados
7. **Dica**: próximos passos (adicionar autenticação, paginação, etc.)

Consulte os arquivos de referência para mais detalhes:
- `references/thymeleaf.md` — Expressões, layouts, fragmentos
- `references/flash.md` — Mensagens de sucesso/erro entre redirecionamentos
- `references/security.md` — Login com Spring Security + Thymeleaf
- `references/tests.md` — Testando controllers MVC com MockMvc

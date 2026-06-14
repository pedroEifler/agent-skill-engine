---
name: springboot-student
description: >
  Use esta skill sempre que um estudante pedir para criar um projeto Spring Boot, API REST, configuração
  de banco de dados, segurança com Spring Security, ou qualquer código Spring Boot com fins de aprendizado.
  Triggers incluem: "crie um projeto Spring Boot", "API REST com Spring", "Spring Boot para estudar",
  "como usar Spring Boot", "Spring Data JPA", "Spring Security tutorial", "projeto Spring Boot completo",
  "Spring Boot com comentários", "template Spring Boot", "como funciona o Spring Boot". Gera código em
  português com comentários explicativos, JavaDoc, boas práticas pedagógicas e versões compatíveis com
  Java 17+ (Spring Boot 3.x) ou Java 11/8 (Spring Boot 2.x). SEMPRE use esta skill para estudantes de Spring Boot.
---

# Skill: Spring Boot para Estudantes 🌱

Gera projetos e código Spring Boot com boas práticas de aprendizado, comentários explicativos em português,
e padrões corretos para a versão do Spring Boot informada.

---

## 1. Verificação de Versão

**SEMPRE** identifique ou pergunte a versão do Spring Boot e do Java antes de gerar código.

### Tabela de compatibilidade:

| Spring Boot | Java mínimo | Java recomendado | Suporte         |
|-------------|-------------|------------------|-----------------|
| **2.7.x**   | 8           | 11 / 17          | Legado (EOL)    |
| **3.0.x**   | 17          | 17               | EOL             |
| **3.1.x**   | 17          | 17 / 21          | EOL             |
| **3.2.x**   | 17          | 17 / 21          | Ativo           |
| **3.3.x**   | 17          | 21               | **Recomendado** |

> ⚠️ Spring Boot 3.x requer Java 17+. Se o estudante usar Java 8 ou 11, use Spring Boot 2.7.x.

### Detectar versão no `pom.xml`:
```xml
<!-- O parent define a versão do Spring Boot -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>
```

---

## 2. Estrutura de Projeto Spring Boot

```
meu-projeto/
├── src/
│   ├── main/
│   │   ├── java/br/com/projeto/
│   │   │   ├── MeuProjetoApplication.java     ← Classe principal com @SpringBootApplication
│   │   │   ├── controller/                    ← Recebe as requisições HTTP (@RestController)
│   │   │   │   └── ProdutoController.java
│   │   │   ├── service/                       ← Regras de negócio (@Service)
│   │   │   │   └── ProdutoService.java
│   │   │   ├── repository/                    ← Acesso ao banco de dados (@Repository)
│   │   │   │   └── ProdutoRepository.java
│   │   │   ├── model/                         ← Entidades JPA (@Entity) e DTOs
│   │   │   │   ├── entity/
│   │   │   │   │   └── Produto.java
│   │   │   │   └── dto/
│   │   │   │       ├── ProdutoRequestDTO.java
│   │   │   │       └── ProdutoResponseDTO.java
│   │   │   ├── exception/                     ← Exceções customizadas e handler global
│   │   │   │   ├── RecursoNaoEncontradoException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── config/                        ← Configurações (Security, CORS, etc.)
│   │   │       └── SecurityConfig.java
│   │   └── resources/
│   │       ├── application.properties         ← Configurações da aplicação
│   │       ├── application-dev.properties     ← Configurações de desenvolvimento
│   │       └── db/migration/                  ← Scripts Flyway (se usado)
│   └── test/
│       └── java/br/com/projeto/
│           ├── controller/
│           └── service/
├── pom.xml
└── README.md
```

---

## 3. Anotações Principais — Explicadas

Sempre explique as anotações ao gerar código:

```java
// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// Essa anotação "liga" toda a mágica do Spring Boot!
@SpringBootApplication
public class MeuProjetoApplication {

    // Método principal: inicia o servidor embutido (Tomcat por padrão)
    public static void main(String[] args) {
        SpringApplication.run(MeuProjetoApplication.class, args);
    }
}
```

```java
// @RestController = @Controller + @ResponseBody
// Indica que esta classe responde requisições HTTP e retorna JSON automaticamente
@RestController
// @RequestMapping define o caminho base de todas as rotas desta classe
@RequestMapping("/api/produtos")
public class ProdutoController {

    // @Autowired injeta automaticamente o serviço — Spring cria a instância para você!
    // Boa prática: injetar via construtor (mais fácil de testar)
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // @GetMapping → responde requisições GET em /api/produtos
    @GetMapping
    public List<ProdutoResponseDTO> listarTodos() {
        return produtoService.listarTodos();
    }

    // @PathVariable extrai o {id} da URL
    @GetMapping("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }

    // @PostMapping → responde requisições POST
    // @RequestBody converte o JSON do corpo da requisição em objeto Java
    // ResponseEntity permite controlar o status HTTP da resposta
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@RequestBody @Valid ProdutoRequestDTO dto) {
        ProdutoResponseDTO criado = produtoService.criar(dto);
        // HTTP 201 Created = recurso criado com sucesso
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(@PathVariable Long id,
                                        @RequestBody @Valid ProdutoRequestDTO dto) {
        return produtoService.atualizar(id, dto);
    }

    // @DeleteMapping → responde requisições DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        // HTTP 204 No Content = deletado com sucesso, sem corpo na resposta
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4. Entidade JPA e Repository

```java
import jakarta.persistence.*; // Spring Boot 3.x usa jakarta (não javax!)

/**
 * Entidade que representa um produto no banco de dados.
 * A anotação @Entity diz ao JPA para criar uma tabela "produto" no banco.
 */
@Entity
// @Table permite customizar o nome da tabela no banco
@Table(name = "produto")
public class Produto {

    // @Id → chave primária da tabela
    // @GeneratedValue → o banco gera o ID automaticamente (auto incremento)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column → customiza a coluna no banco (nome, tamanho, obrigatoriedade)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "preco", nullable = false)
    private BigDecimal preco;

    // @CreationTimestamp → preenche automaticamente na criação
    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    // Construtores, getters e setters...
}
```

```java
// JpaRepository já fornece: save, findById, findAll, delete, count e muito mais!
// Não precisa escrever SQL para operações básicas
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Spring Data cria a query automaticamente pelo nome do método!
    // Equivale a: SELECT * FROM produto WHERE nome LIKE '%:nome%'
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // @Query para queries mais complexas
    @Query("SELECT p FROM Produto p WHERE p.preco BETWEEN :min AND :max")
    List<Produto> buscarPorFaixaDePreco(@Param("min") BigDecimal min,
                                         @Param("max") BigDecimal max);
}
```

---

## 5. Tratamento Global de Exceções

```java
/**
 * Captura exceções de toda a aplicação e retorna respostas padronizadas.
 * Sem isso, o Spring retorna uma página de erro feia por padrão.
 */
@RestControllerAdvice // Intercepta exceções de todos os controllers
public class GlobalExceptionHandler {

    // Captura quando um recurso não é encontrado → retorna 404
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return Map.of(
            "erro", "Recurso não encontrado",
            "mensagem", ex.getMessage()
        );
    }

    // Captura erros de validação (@Valid) → retorna 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidacao(MethodArgumentNotValidException ex) {
        // Coleta todos os erros de validação campo a campo
        Map<String, String> errosPorCampo = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
            errosPorCampo.put(erro.getField(), erro.getDefaultMessage())
        );
        return Map.of("erros", errosPorCampo);
    }
}
```

---

## 6. application.properties Comentado

```properties
# ===== Banco de Dados =====
# URL de conexão com H2 em memória (ótimo para estudar!)
spring.datasource.url=jdbc:h2:mem:meubanco
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Habilita o console web do H2: acesse http://localhost:8080/h2-console
spring.h2.console.enabled=true

# ===== JPA / Hibernate =====
# create-drop: cria as tabelas ao iniciar e remove ao encerrar (bom para estudar)
# update: atualiza as tabelas sem apagar dados (cuidado em produção!)
# validate: apenas valida, não altera nada (use em produção)
spring.jpa.hibernate.ddl-auto=create-drop

# Mostra as queries SQL no console — muito útil para aprender!
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ===== Servidor =====
server.port=8080
```

---

## 7. Checklist Antes de Gerar

- [ ] Versão do Spring Boot e Java identificadas?
- [ ] Compatibilidade Java ↔ Spring Boot verificada?
- [ ] Estrutura de pacotes definida?
- [ ] Entidade + Repository + Service + Controller criados?
- [ ] DTOs separados da entidade?
- [ ] Exceções customizadas + handler global?
- [ ] application.properties com comentários explicativos?
- [ ] Comentários nas anotações principais?

---

## 8. Formato de Entrega

1. **Explicação** do que será criado e do fluxo (Request → Controller → Service → Repository → DB)
2. **pom.xml** com versão correta do Spring Boot
3. **application.properties** comentado
4. **Código** com comentários em português
5. **Dica de aprendizado**: próximos passos sugeridos

Consulte os arquivos de referência para detalhes por versão:
- Spring Boot 2.x → `references/frameworks/springboot2.md`
- Spring Boot 3.x → `references/frameworks/springboot3.md`
- Spring Security → `references/frameworks/security.md`
- Testes → `references/frameworks/tests.md`

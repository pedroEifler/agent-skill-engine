# Spring Boot - Skills & Best Practices

## Estrutura do Projeto
- Organize por **feature/domínio**, não por camada técnica:
  ```
  com.empresa.produto.usuario/
  ├── UsuarioController.java
  ├── UsuarioService.java
  ├── UsuarioRepository.java
  └── Usuario.java
  ```
- Mantenha a classe `@SpringBootApplication` na raiz do pacote base.

## Injeção de Dependência
- Prefira **injeção por construtor** (nunca use `@Autowired` em campos).
- Marque dependências como `final` — garanta imutabilidade.
- Use `@RequiredArgsConstructor` (Lombok) ou declare o construtor explicitamente.
- Para múltiplas implementações, use `@Qualifier` ou `@Primary`.

## Controllers (API REST)
- Anote controllers com `@RestController` e defina `@RequestMapping` no nível da classe.
- Retorne `ResponseEntity<T>` para controle explícito de status HTTP.
- Use DTOs (records) para request/response — nunca exponha entidades diretamente.
- Valide inputs com `@Valid` e Bean Validation (`@NotNull`, `@Size`, `@Email`).
- Documente APIs com SpringDoc/OpenAPI.

## Service Layer
- Anote com `@Service` e mantenha lógica de negócio aqui.
- Use `@Transactional` no nível do método — apenas onde necessário.
- `@Transactional(readOnly = true)` para operações de leitura.
- Nunca lance exceções genéricas — crie exceções de domínio.

## Persistência (Spring Data JPA)
- Repositories devem estender `JpaRepository<T, ID>`.
- Use **query methods** derivados para consultas simples.
- Para queries complexas, use `@Query` com JPQL ou native queries.
- Evite N+1: use `@EntityGraph` ou `JOIN FETCH`.
- Configure `spring.jpa.open-in-view=false` para evitar lazy loading em controllers.

## Tratamento de Erros
- Crie um `@RestControllerAdvice` global para tratamento de exceções.
- Mapeie exceções de domínio para HTTP status codes apropriados.
- Retorne um body padronizado de erro (RFC 7807 Problem Details).
- Nunca exponha stack traces em produção.

## Configuração
- Use `application.yml` ou `application.properties` com profiles (`dev`, `prod`).
- Externalize configurações sensíveis via variáveis de ambiente.
- Crie `@ConfigurationProperties` para grupos de configuração tipados.

## Segurança
- Use Spring Security com configuração baseada em `SecurityFilterChain`.
- Implemente autenticação stateless com JWT para APIs.
- Proteja endpoints com `@PreAuthorize` para autorização granular.

## Testes
- `@SpringBootTest` para testes de integração (use sparingly).
- `@WebMvcTest` para testar controllers isoladamente.
- `@DataJpaTest` para testar repositories.
- Use `@MockBean` para substituir beans em testes de integração.
- Testcontainers para testes com banco de dados real.

## Observabilidade
- Use Spring Boot Actuator para health checks e métricas.
- Structured logging com SLF4J + Logback.
- Adicione correlation IDs para rastreamento distribuído.

## Performance
- Habilite compressão HTTP: `server.compression.enabled=true`.
- Use cache com `@Cacheable` para dados frequentemente acessados.
- Configure connection pool (HikariCP) adequadamente.
- Use paginação (`Pageable`) para listagens grandes.


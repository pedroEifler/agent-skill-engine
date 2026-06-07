# Testes no Spring Boot — Referência para Estudantes

## Tipos de teste

```
Unitário    → testa uma classe isolada (sem banco, sem servidor)
Integração  → testa múltiplas camadas juntas (com banco em memória)
E2E         → testa do HTTP até o banco (MockMvc ou RestAssured)
```

## Teste de Service (unitário com Mockito)
```java
// @ExtendWith(MockitoExtension.class) → ativa o Mockito no JUnit 5
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    // @Mock → cria um "dublê" do repository (não acessa o banco de verdade)
    @Mock
    private ProdutoRepository produtoRepository;

    // @InjectMocks → injeta os mocks no service
    @InjectMocks
    private ProdutoService produtoService;

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void deveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange: configura o mock para simular que o produto não existe
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert: verifica que a exceção correta é lançada
        assertThrows(
            RecursoNaoEncontradoException.class,
            () -> produtoService.buscarPorId(99L)
        );
    }
}
```

## Teste de Controller (E2E com MockMvc)
```java
// @SpringBootTest → sobe o contexto completo do Spring
// @AutoConfigureMockMvc → configura o MockMvc automaticamente
@SpringBootTest
@AutoConfigureMockMvc
class ProdutoControllerTest {

    // MockMvc simula requisições HTTP sem precisar de um servidor real
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // para converter objetos em JSON

    @Test
    @DisplayName("GET /api/produtos deve retornar 200")
    void deveRetornarListaDeProdutos() throws Exception {
        mockMvc.perform(get("/api/produtos"))           // faz a requisição GET
            .andExpect(status().isOk())                 // espera status 200
            .andExpect(content().contentType(
                MediaType.APPLICATION_JSON));           // espera JSON
    }

    @Test
    @DisplayName("POST /api/produtos deve criar produto e retornar 201")
    void deveCriarProduto() throws Exception {
        var dto = new ProdutoRequestDTO("Notebook", new BigDecimal("3500.00"));

        mockMvc.perform(post("/api/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))) // converte para JSON
            .andExpect(status().isCreated())           // espera status 201
            .andExpect(jsonPath("$.nome").value("Notebook")); // verifica o nome no JSON
    }
}
```

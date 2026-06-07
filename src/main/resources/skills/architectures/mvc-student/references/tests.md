# Testes no Spring MVC — Referência para Estudantes

## Testando o Controller com MockMvc

```java
// @WebMvcTest → sobe apenas a camada web (controller), sem banco de dados
// Muito mais rápido que @SpringBootTest!
@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    // MockMvc simula requisições HTTP sem precisar de servidor real
    @Autowired
    private MockMvc mockMvc;

    // @MockBean cria um "dublê" do service para o controller usar
    @MockBean
    private ProdutoService produtoService;

    @Test
    @DisplayName("GET /produtos deve retornar status 200 e a view 'produto/lista'")
    void deveListarProdutos() throws Exception {
        // Configura o mock para retornar uma lista de produtos
        var produtos = List.of(
            new Produto("Notebook", new BigDecimal("3000")),
            new Produto("Mouse", new BigDecimal("80"))
        );
        when(produtoService.listarTodos()).thenReturn(produtos);

        // Faz a requisição GET e verifica o resultado
        mockMvc.perform(get("/produtos"))
            .andExpect(status().isOk())                    // espera HTTP 200
            .andExpect(view().name("produto/lista"))       // espera a view correta
            .andExpect(model().attributeExists("produtos")) // espera o atributo no model
            .andExpect(model().attribute("produtos", hasSize(2))); // lista com 2 itens
    }

    @Test
    @DisplayName("POST /produtos com dados válidos deve redirecionar para a lista")
    void deveSalvarERedirecionarParaLista() throws Exception {
        mockMvc.perform(post("/produtos")
                .param("nome", "Teclado")
                .param("preco", "150.00")
                .with(csrf()))                             // ← obrigatório com Spring Security!
            .andExpect(status().is3xxRedirection())        // espera redirecionamento (302)
            .andExpect(redirectedUrl("/produtos"));        // redireciona para a lista
    }

    @Test
    @DisplayName("POST /produtos com nome vazio deve voltar ao formulário com erro")
    void deveVoltarAoFormularioComErroDeValidacao() throws Exception {
        mockMvc.perform(post("/produtos")
                .param("nome", "")          // nome vazio → falha na validação @NotBlank
                .param("preco", "100.00")
                .with(csrf()))
            .andExpect(status().isOk())                    // não redireciona, volta ao form
            .andExpect(view().name("produto/formulario"))  // view do formulário
            .andExpect(model().hasErrors())                // model tem erros de validação
            .andExpect(model().attributeHasFieldErrors("produto", "nome")); // erro no campo nome
    }
}
```

## Testando com Banco em Memória (Teste de Integração)

```java
// @SpringBootTest + @AutoConfigureMockMvc → testa tudo junto (controller + service + banco)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Cada teste roda em sua própria transação e desfaz ao fim
class ProdutoIntegracaoTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProdutoRepository produtoRepository;

    @Test
    @DisplayName("Deve criar produto e aparecer na lista")
    void deveCriarEExibirProdutoNaLista() throws Exception {
        // Salva diretamente no banco
        produtoRepository.save(new Produto("Monitor", new BigDecimal("1200")));

        // Verifica que aparece na resposta HTML
        mockMvc.perform(get("/produtos"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Monitor")));
    }
}
```

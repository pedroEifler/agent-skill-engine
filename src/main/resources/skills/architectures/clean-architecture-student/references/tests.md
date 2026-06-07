# Testes na Clean Architecture — Referência para Estudantes

## A grande vantagem: testar o domínio sem banco de dados!

```java
/**
 * Teste da entidade de domínio Produto.
 * NÃO precisa do Spring, banco de dados ou nenhum framework!
 * Executa em milissegundos.
 */
class ProdutoTest {

    @Test
    @DisplayName("Deve reduzir estoque ao vender")
    void deveReduzirEstoqueAoVender() {
        // Arrange: cria o produto com 10 unidades
        var produto = new Produto("Notebook", new Dinheiro(new BigDecimal("3000"), "BRL"), 10);

        // Act: vende 3 unidades
        produto.vender(3);

        // Assert: estoque deve ser 7
        assertThat(produto.getQuantidadeEmEstoque()).isEqualTo(7);
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente")
    void deveLancarExcecaoComEstoqueInsuficiente() {
        var produto = new Produto("Notebook", new Dinheiro(new BigDecimal("3000"), "BRL"), 5);

        // Tenta vender mais do que tem em estoque
        assertThatThrownBy(() -> produto.vender(10))
            .isInstanceOf(EstoqueInsuficienteException.class)
            .hasMessageContaining("Disponível: 5");
    }
}
```

## Teste de Use Case com Mock da Porta

```java
@ExtendWith(MockitoExtension.class)
class CriarProdutoUseCaseTest {

    // Criamos um "dublê" da porta — não acessa banco de dados real!
    @Mock
    private ProdutoRepositoryPort produtoRepository;

    @InjectMocks
    private CriarProdutoUseCaseImpl useCase;

    @Test
    @DisplayName("Deve criar produto e retornar com ID")
    void deveCriarProduto() {
        // Arrange: configura o mock para simular o salvamento
        var input = new CriarProdutoInput("Teclado", new BigDecimal("200"), 50);
        var produtoSalvo = new Produto(1L, "Teclado",
            new Dinheiro(new BigDecimal("200"), "BRL"), 50);

        when(produtoRepository.salvar(any(Produto.class))).thenReturn(produtoSalvo);

        // Act
        var output = useCase.executar(input);

        // Assert
        assertThat(output.id()).isEqualTo(1L);
        assertThat(output.nome()).isEqualTo("Teclado");

        // Verifica que a porta foi chamada exatamente 1 vez
        verify(produtoRepository, times(1)).salvar(any());
    }
}
```

## Teste do Adapter de Persistência (teste de integração)

```java
// Testa apenas o adaptador JPA — isola da camada de application
@DataJpaTest  // Sobe apenas o contexto JPA, não o Spring Web inteiro
class ProdutoRepositoryAdapterTest {

    @Autowired
    private ProdutoJpaRepository jpaRepository;

    private ProdutoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProdutoRepositoryAdapter(jpaRepository);
    }

    @Test
    @DisplayName("Deve salvar e recuperar produto pelo ID")
    void deveSalvarERecuperarProduto() {
        var produto = new Produto("Mouse", new Dinheiro(new BigDecimal("80"), "BRL"), 100);

        var salvo = adapter.salvar(produto);
        var encontrado = adapter.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Mouse");
    }
}
```

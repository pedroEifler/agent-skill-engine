# Testando o Padrão Builder — Referência para Estudantes

## Testando o objeto construído

```java
class PedidoBuilderTest {

    @Test
    @DisplayName("Deve construir pedido com todos os campos opcionais")
    void deveConstruirPedidoCompleto() {
        var pedido = Pedido.builder("Ana", "Notebook", 2)
            .desconto(0.15)
            .cupom("BLACK15")
            .enderecoEntrega("Rua das Flores, 10")
            .entregaExpressa(true)
            .observacoes("Frágil")
            .build();

        // Verifica cada campo individualmente
        assertThat(pedido.getCliente()).isEqualTo("Ana");
        assertThat(pedido.getProduto()).isEqualTo("Notebook");
        assertThat(pedido.getQuantidade()).isEqualTo(2);
        assertThat(pedido.getDesconto()).isEqualTo(0.15);
        assertThat(pedido.getCupom()).isEqualTo("BLACK15");
        assertThat(pedido.isEntregaExpressa()).isTrue();
    }

    @Test
    @DisplayName("Deve usar valores padrão para campos opcionais não informados")
    void deveUsarValoresPadrao() {
        var pedido = Pedido.builder("Carlos", "Mouse", 1).build();

        assertThat(pedido.getDesconto()).isEqualTo(0.0);
        assertThat(pedido.getCupom()).isNull();
        assertThat(pedido.isEntregaExpressa()).isFalse();
        assertThat(pedido.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente é nulo")
    void deveLancarExcecaoParaClienteNulo() {
        assertThatThrownBy(() -> Pedido.builder(null, "Mouse", 1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cliente é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade é zero")
    void deveLancarExcecaoParaQuantidadeZero() {
        assertThatThrownBy(() -> Pedido.builder("Ana", "Notebook", 0).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Quantidade deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve lançar exceção ao pedir entrega expressa sem endereço")
    void deveLancarExcecaoEntregaExpressaSemEndereco() {
        assertThatThrownBy(() ->
            Pedido.builder("Ana", "Notebook", 1)
                .entregaExpressa(true)
                // sem .enderecoEntrega(...)
                .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Endereço de entrega é obrigatório");
    }
}
```

## Testando o Builder GoF (Director + Builder)

```java
class RelatorioBuilderTest {

    private RelatorioDirector director;

    @BeforeEach
    void setUp() { director = new RelatorioDirector(); }

    @Test
    @DisplayName("Builder HTML deve gerar tags HTML válidas")
    void deveGerarHtmlComTagsCorretas() {
        var html = director.construirRelatorioMensal(new RelatorioHtmlBuilder(), "Junho");

        assertThat(html).contains("<html>")
                        .contains("<h1>")
                        .contains("Junho")
                        .contains("</html>");
    }

    @Test
    @DisplayName("Builder TXT deve gerar texto sem tags HTML")
    void deveGerarTextoSemTagsHtml() {
        var txt = director.construirRelatorioMensal(new RelatorioTxtBuilder(), "Junho");

        assertThat(txt).doesNotContain("<html>")
                       .doesNotContain("<h1>")
                       .contains("JUNHO")
                       .contains("=====");
    }

    @Test
    @DisplayName("Ambos os builders devem conter o mês no resultado")
    void ambosBuilderDevemConterOMes() {
        var html = director.construirRelatorioMensal(new RelatorioHtmlBuilder(), "Julho");
        var txt  = director.construirRelatorioMensal(new RelatorioTxtBuilder(), "Julho");

        assertThat(html).contains("Julho");
        assertThat(txt).contains("JULHO");
    }
}
```

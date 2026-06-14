# Testando o Padrão Factory — Referência para Estudantes

## Testando a Simple Factory

```java
class PagamentoFactoryTest {

    @Test
    @DisplayName("Deve criar PagamentoCartao para o tipo CARTAO")
    void deveCriarPagamentoCartao() {
        var pagamento = PagamentoFactory.criar(TipoPagamento.CARTAO);

        // Verifica que o tipo retornado é o esperado
        assertThat(pagamento).isInstanceOf(PagamentoCartao.class);
    }

    @Test
    @DisplayName("Deve criar PagamentoPix para o tipo PIX")
    void deveCriarPagamentoPix() {
        var pagamento = PagamentoFactory.criar(TipoPagamento.PIX);

        assertThat(pagamento).isInstanceOf(PagamentoPix.class);
    }
}
```

## Testando o Factory Method (com classes abstratas)

```java
class ServicoNotificacaoTest {

    @Test
    @DisplayName("ServicoNotificacaoEmail deve usar NotificadorEmail")
    void deveUsarNotificadorEmail() {
        // Cria uma "espiã" para capturar o que o Factory Method criou
        var servico = new ServicoNotificacaoEmailEspiao();

        servico.enviar("Teste", "user@exemplo.com");

        assertThat(servico.notificadorCriado).isInstanceOf(NotificadorEmail.class);
    }

    // Subclasse de teste que "espia" qual notificador foi criado
    static class ServicoNotificacaoEmailEspiao extends ServicoNotificacaoEmail {
        Notificador notificadorCriado;

        @Override
        protected Notificador criarNotificador() {
            notificadorCriado = super.criarNotificador();
            return notificadorCriado;
        }
    }
}
```

## Testando a Abstract Factory

```java
class FabricaUITest {

    @Test
    @DisplayName("FabricaUIClara deve criar componentes claros")
    void deveCriarComponentesClaros() {
        FabricaUI fabrica = new FabricaUIClara();

        assertThat(fabrica.criarBotao()).isInstanceOf(BotaoClaro.class);
        assertThat(fabrica.criarCampoTexto()).isInstanceOf(CampoTextoClaro.class);
    }

    @Test
    @DisplayName("FabricaUIEscura deve criar componentes escuros")
    void deveCriarComponentesEscuros() {
        FabricaUI fabrica = new FabricaUIEscura();

        assertThat(fabrica.criarBotao()).isInstanceOf(BotaoEscuro.class);
        assertThat(fabrica.criarCampoTexto()).isInstanceOf(CampoTextoEscuro.class);
    }

    @Test
    @DisplayName("Aplicacao deve renderizar com a fábrica correta")
    void deveRenderizarComponentesDaFabricaInjetada() {
        var fabricaEscura = new FabricaUIEscura();
        var app = new Aplicacao(fabricaEscura);

        // Verifica que não lança exceção e usa os componentes certos
        assertDoesNotThrow(app::renderizar);
    }
}
```

## Testando o Factory com Spring (injeção de lista)

```java
@ExtendWith(MockitoExtension.class)
class PagamentoFactorySpringTest {

    @Test
    @DisplayName("Deve encontrar implementação correta na lista injetada")
    void deveEncontrarImplementacaoCorreta() {
        // Simula o que o Spring faria: injeta uma lista de implementações
        var implementacoes = List.of(
            new PagamentoCartao(),
            new PagamentoPix()
        );
        var factory = new PagamentoFactory(implementacoes);

        var pagamento = factory.criar(TipoPagamento.PIX);

        assertThat(pagamento).isInstanceOf(PagamentoPix.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nenhuma implementação suporta o tipo")
    void deveLancarExcecaoParaTipoNaoSuportado() {
        var factory = new PagamentoFactory(List.of(new PagamentoCartao()));

        assertThatThrownBy(() -> factory.criar(TipoPagamento.PIX))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("não suportado");
    }
}
```

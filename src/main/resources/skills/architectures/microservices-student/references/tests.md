# Testes em Microserviços — Referência para Estudantes

## Testando chamadas Feign com WireMock

WireMock "finge" ser o outro serviço — sem precisar subir o servico-produtos de verdade!

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-contract-wiremock</artifactId>
    <scope>test</scope>
</dependency>
```

```java
// @AutoConfigureWireMock → sobe um servidor HTTP falso na porta configurada
@SpringBootTest
@AutoConfigureWireMock(port = 8082)  // mesma porta do servico-produtos real
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Test
    @DisplayName("Deve criar pedido buscando produto no serviço externo")
    void deveCriarPedidoComSucesso() throws Exception {
        // Configura o WireMock para responder como se fosse o servico-produtos
        stubFor(get(urlEqualTo("/api/produtos/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 1,
                        "nome": "Notebook",
                        "preco": 3000.00
                    }
                    """)));

        // Agora o pedidoService vai chamar o WireMock, não o serviço real
        var pedido = pedidoService.criarPedido(1L, 1L, 2);

        assertThat(pedido.getNomeProduto()).isEqualTo("Notebook");
        assertThat(pedido.getTotal()).isEqualByComparingTo("6000.00");
    }

    @Test
    @DisplayName("Deve usar fallback quando serviço de produtos cai")
    void deveUsarFallbackQuandoServicoFora() {
        // Configura o WireMock para retornar erro 503 (serviço indisponível)
        stubFor(get(urlEqualTo("/api/produtos/99"))
            .willReturn(aResponse().withStatus(503)));

        // O circuit breaker deve entrar em ação e usar o fallback
        assertThatThrownBy(() -> pedidoService.criarPedido(1L, 99L, 1))
            .isInstanceOf(ProdutoNaoDisponivelException.class);
    }
}
```

## Testando Mensageria com RabbitMQ Embarcado

```java
@SpringBootTest
class PagamentoConsumerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Test
    @DisplayName("Deve processar pagamento ao receber evento na fila")
    void deveProcessarPagamento() throws Exception {
        var evento = new PedidoCriadoEvent(1L, new BigDecimal("150.00"));

        // Envia mensagem para a fila
        rabbitTemplate.convertAndSend(RabbitConfig.FILA_PEDIDOS, evento);

        // Aguarda o consumidor processar (assíncrono!)
        Thread.sleep(500);

        // Verifica que o pagamento foi criado
        assertThat(pagamentoRepository.findByPedidoId(1L)).isPresent();
    }
}
```

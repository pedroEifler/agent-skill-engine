# Testando o Padrão Singleton — Referência para Estudantes

## Testando que só existe uma instância

```java
class SingletonTest {

    @Test
    @DisplayName("getInstancia() deve retornar sempre o mesmo objeto")
    void deveRetornarSempreAMesmaInstancia() {
        var instancia1 = GerenciadorConfiguracao.getInstancia();
        var instancia2 = GerenciadorConfiguracao.getInstancia();

        // == compara referência de memória — devem ser o MESMO objeto!
        assertThat(instancia1).isSameAs(instancia2);
    }

    @Test
    @DisplayName("Múltiplas chamadas devem retornar o mesmo hashCode")
    void deveTerMesmoHashCode() {
        int hash1 = GerenciadorConfiguracao.getInstancia().hashCode();
        int hash2 = GerenciadorConfiguracao.getInstancia().hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }
}
```

## Testando thread-safety do Double-Checked Locking

```java
class SingletonThreadSafetyTest {

    @Test
    @DisplayName("Deve criar apenas UMA instância mesmo com 100 threads concorrentes")
    void deveCriarUmaInstanciaComMultiplosThreads() throws InterruptedException {
        int numThreads = 100;
        var latch = new CountDownLatch(numThreads);

        // Conjunto thread-safe para guardar as instâncias coletadas
        var instancias = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // Dispara 100 threads ao mesmo tempo
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                instancias.add(PoolConexoes.getInstancia()); // cada thread busca a instância
                latch.countDown();
            }).start();
        }

        // Aguarda todas as threads terminarem
        latch.await(5, TimeUnit.SECONDS);

        // Se é Singleton de verdade, todas as threads pegaram o MESMO objeto!
        assertThat(instancias).hasSize(1);
    }
}
```

## Testando código que USA Singleton (com injeção)

```java
class ServicoPedidoTest {

    @Test
    @DisplayName("Deve registrar log ao processar pedido")
    void deveLogarAoProcessarPedido() {
        // Cria um "logger fake" que podemos verificar depois
        var loggerFake = mock(Logger.class);
        var servico = new ServicoPedidoTestavel(loggerFake);

        servico.processar(new Pedido(42L));

        // Verifica que o log foi chamado com os parâmetros corretos
        verify(loggerFake).log("INFO", "Processando pedido: 42");
    }
}
```

## Testando Enum Singleton

```java
class LoggerEnumTest {

    @BeforeEach
    void limparLogs() {
        // Enum Singleton tem estado persistente entre testes — precisa limpar!
        Logger.INSTANCIA.limparParaTeste();
    }

    @Test
    @DisplayName("Deve registrar log com o nível e mensagem corretos")
    void deveRegistrarLog() {
        Logger.INSTANCIA.log("INFO", "Teste de log");

        var logs = Logger.INSTANCIA.getTodosLogs();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0)).contains("[INFO]").contains("Teste de log");
    }

    @Test
    @DisplayName("Instância do Enum deve ser sempre a mesma")
    void enumDeveSerSempreaaMesmaInstancia() {
        // Enums em Java garantem uma única instância por definição
        assertThat(Logger.INSTANCIA).isSameAs(Logger.INSTANCIA);
        assertThat(Logger.valueOf("INSTANCIA")).isSameAs(Logger.INSTANCIA);
    }
}
```

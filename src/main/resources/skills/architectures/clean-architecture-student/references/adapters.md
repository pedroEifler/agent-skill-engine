# Adaptadores — Referência para Estudantes

## Adapter de Mensageria (Kafka/RabbitMQ)

```java
/**
 * Porta de saída para envio de eventos de domínio via mensageria.
 * O domínio não sabe se é Kafka, RabbitMQ, SQS, etc.
 */
public interface EventoPublicadorPort {
    void publicar(PedidoConfirmadoEvent evento);
}

/**
 * Adaptador concreto que usa Kafka.
 * Só este arquivo importa classes do Kafka!
 */
@Component
public class KafkaPedidoPublicadorAdapter implements EventoPublicadorPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPedidoPublicadorAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publicar(PedidoConfirmadoEvent evento) {
        // Converte o evento de domínio para a mensagem do Kafka
        kafkaTemplate.send("pedidos-confirmados", evento.pedidoId().toString(), evento);
    }
}
```

## Adapter de Cache

```java
/**
 * Decorator de cache: envolve o repositório real e adiciona cache transparentemente.
 * O use case não precisa saber que existe cache!
 */
@Component
@Primary // Spring vai injetar este ao invés do original
public class ProdutoRepositoryCacheAdapter implements ProdutoRepositoryPort {

    private final ProdutoRepositoryPort repositorioReal;   // o adaptador JPA
    private final Map<Long, Produto> cache = new ConcurrentHashMap<>();

    public ProdutoRepositoryCacheAdapter(
            @Qualifier("produtoRepositoryAdapter") ProdutoRepositoryPort repositorioReal) {
        this.repositorioReal = repositorioReal;
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        // Verifica o cache primeiro; se não tiver, busca no banco e guarda no cache
        if (cache.containsKey(id)) {
            return Optional.of(cache.get(id));
        }
        var produto = repositorioReal.buscarPorId(id);
        produto.ifPresent(p -> cache.put(id, p));
        return produto;
    }

    @Override
    public Produto salvar(Produto produto) {
        var salvo = repositorioReal.salvar(produto);
        // Invalida o cache ao salvar para evitar dados desatualizados
        if (salvo.getId() != null) cache.remove(salvo.getId());
        return salvo;
    }

    @Override
    public List<Produto> listarTodos() {
        return repositorioReal.listarTodos();
    }
}
```

## Config — Montando tudo junto

```java
/**
 * Classe de configuração que "monta" a aplicação.
 * É aqui que conectamos as implementações às interfaces.
 */
@Configuration
public class BeanConfig {

    /**
     * Cria o Use Case injetando o adaptador de persistência.
     * O Spring gerencia a criação e o ciclo de vida dos objetos.
     */
    @Bean
    public CriarProdutoUseCase criarProdutoUseCase(ProdutoRepositoryPort produtoRepository) {
        // Conecta o Use Case ao Adaptador de Persistência
        return new CriarProdutoUseCaseImpl(produtoRepository);
    }
}
```

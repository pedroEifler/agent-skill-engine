# Camada de Application — Referência para Estudantes

## Use Case complexo com múltiplas portas

```java
/**
 * Caso de uso: Realizar Pedido
 * Orquestra o domínio e múltiplas portas para completar a operação.
 *
 * Note que este use case:
 * 1. Usa APENAS interfaces (ports), nunca implementações
 * 2. Não tem lógica de negócio — ela fica no Domínio
 * 3. Faz a orquestração: busca dados, chama domínio, persiste, notifica
 */
@Service
public class RealizarPedidoUseCaseImpl implements RealizarPedidoUseCase {

    // Todas são INTERFACES — o use case não sabe se é banco SQL, NoSQL, etc.
    private final ClienteRepositoryPort clienteRepository;
    private final ProdutoRepositoryPort produtoRepository;
    private final PedidoRepositoryPort pedidoRepository;
    private final NotificacaoPort notificacao;   // pode ser email, SMS, push...

    public RealizarPedidoUseCaseImpl(
            ClienteRepositoryPort clienteRepository,
            ProdutoRepositoryPort produtoRepository,
            PedidoRepositoryPort pedidoRepository,
            NotificacaoPort notificacao) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.notificacao = notificacao;
    }

    @Override
    @Transactional  // Garante que tudo salva ou nada salva
    public PedidoOutput executar(RealizarPedidoInput input) {
        // 1. Valida que o cliente existe
        var cliente = clienteRepository.buscarPorId(input.clienteId())
            .orElseThrow(() -> new ClienteNaoEncontradoException(input.clienteId()));

        // 2. Cria o pedido (objeto de domínio puro)
        var pedido = new Pedido(cliente.getId());

        // 3. Adiciona cada item — o domínio valida as regras
        for (var itemInput : input.itens()) {
            var produto = produtoRepository.buscarPorId(itemInput.produtoId())
                .orElseThrow(() -> new ProdutoNaoEncontradoException(itemInput.produtoId()));
            pedido.adicionarItem(produto, itemInput.quantidade());
        }

        // 4. Confirma o pedido (domínio valida se não está vazio, etc.)
        pedido.confirmar();

        // 5. Persiste
        var pedidoSalvo = pedidoRepository.salvar(pedido);

        // 6. Notifica — não sabe se é email, SMS, etc.
        notificacao.notificar(cliente, "Pedido #" + pedidoSalvo.getId() + " confirmado!");

        // 7. Retorna o DTO de saída
        return PedidoOutput.de(pedidoSalvo);
    }
}
```

## CQRS Básico — Separar Leitura de Escrita

CQRS = Command Query Responsibility Segregation
Ideia: separe as operações que MUDAM dados das que apenas LEEM dados.

```java
// COMMAND: muda estado, não retorna dados complexos
public interface CriarProdutoCommand {
    ProdutoOutput executar(CriarProdutoInput input);
}

// QUERY: apenas lê, não muda nada
public interface BuscarProdutoQuery {
    ProdutoOutput buscarPorId(Long id);
    List<ProdutoOutput> listarTodos();
    List<ProdutoOutput> buscarPorNome(String nome);
}

// O service implementa ambas as interfaces
@Service
public class ProdutoService implements CriarProdutoCommand, BuscarProdutoQuery {
    // ... implementações separadas
}

// O controller pode injetar só o que precisa:
@RestController
public class ProdutoController {

    // Este controller só faz leitura — injeta apenas a query
    private final BuscarProdutoQuery query;

    public ProdutoController(BuscarProdutoQuery query) {
        this.query = query;
    }
}
```

# Camada de Domínio — Referência para Estudantes

## Aggregates (Agregados)

Um Aggregate é um grupo de entidades que formam uma unidade coesa.
O Aggregate Root é o "chefe" — toda comunicação com o grupo passa por ele.

```java
/**
 * Aggregate Root: Pedido
 * Controla os itens do pedido e garante que as regras sejam respeitadas.
 *
 * Regra de ouro: nunca acesse PedidoItem diretamente de fora — passe pelo Pedido!
 */
public class Pedido {

    private final Long id;
    private final Long clienteId;
    // Lista interna: o mundo externo só lê uma cópia imutável
    private final List<PedidoItem> itens = new ArrayList<>();
    private StatusPedido status;
    private Dinheiro total;

    public Pedido(Long clienteId) {
        this.clienteId = Objects.requireNonNull(clienteId);
        this.status = StatusPedido.RASCUNHO;
        this.total = new Dinheiro(BigDecimal.ZERO, "BRL");
        this.id = null;
    }

    /**
     * Adiciona um item ao pedido.
     * Regra: não pode adicionar item a pedido já confirmado.
     */
    public void adicionarItem(Produto produto, int quantidade) {
        if (status != StatusPedido.RASCUNHO) {
            throw new PedidoJaConfirmadoException("Não é possível alterar pedido confirmado");
        }
        var item = new PedidoItem(produto.getId(), produto.getPreco(), quantidade);
        itens.add(item);
        // Recalcula o total sempre que um item é adicionado
        recalcularTotal();
    }

    /**
     * Confirma o pedido — muda o status e bloqueia novas alterações.
     */
    public void confirmar() {
        if (itens.isEmpty()) {
            throw new PedidoVazioException("Não é possível confirmar pedido sem itens");
        }
        this.status = StatusPedido.CONFIRMADO;
    }

    private void recalcularTotal() {
        this.total = itens.stream()
            .map(PedidoItem::getSubtotal)
            .reduce(new Dinheiro(BigDecimal.ZERO, "BRL"), Dinheiro::somar);
    }

    // Expõe uma cópia imutável — o mundo externo não pode alterar a lista diretamente
    public List<PedidoItem> getItens() { return Collections.unmodifiableList(itens); }
    public StatusPedido getStatus() { return status; }
    public Dinheiro getTotal() { return total; }
}
```

## Domain Events (Eventos de Domínio)

Domain Events notificam que algo importante aconteceu no domínio.
Permitem desacoplar reações (enviar email, atualizar estoque) do evento em si.

```java
/**
 * Evento que representa que um pedido foi confirmado.
 * É imutável — representa um fato que já aconteceu no passado.
 */
public record PedidoConfirmadoEvent(
    Long pedidoId,
    Long clienteId,
    Dinheiro total,
    LocalDateTime confirmedAt
) { }

// No Aggregate Root, publicamos o evento:
public void confirmar(ApplicationEventPublisher eventPublisher) {
    if (itens.isEmpty()) throw new PedidoVazioException("Pedido sem itens");
    this.status = StatusPedido.CONFIRMADO;

    // Publica o evento — quem quiser reagir, escuta!
    eventPublisher.publishEvent(
        new PedidoConfirmadoEvent(id, clienteId, total, LocalDateTime.now())
    );
}

// Em outro lugar, ouvimos o evento:
@Component
public class EnviarEmailAoPedidoConfirmado {

    @EventListener // Escuta o evento automaticamente
    public void handle(PedidoConfirmadoEvent evento) {
        System.out.println("Enviando email para o cliente " + evento.clienteId());
    }
}
```

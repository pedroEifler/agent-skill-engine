# Factory com Spring — Referência para Estudantes

## Combinando Factory + Strategy + Spring

O Spring pode injetar TODAS as implementações de uma interface automaticamente!
Isso elimina o `switch` da Factory tradicional.

```java
/**
 * Interface comum — cada implementação tem um "tipo" que ela atende.
 */
public interface Pagamento {
    boolean suporta(TipoPagamento tipo);
    void processar(double valor);
}

@Component
public class PagamentoCartao implements Pagamento {
    @Override
    public boolean suporta(TipoPagamento tipo) {
        return tipo == TipoPagamento.CARTAO;
    }

    @Override
    public void processar(double valor) {
        System.out.println("Processando R$ " + valor + " via Cartão");
    }
}

@Component
public class PagamentoPix implements Pagamento {
    @Override
    public boolean suporta(TipoPagamento tipo) {
        return tipo == TipoPagamento.PIX;
    }

    @Override
    public void processar(double valor) {
        System.out.println("Processando R$ " + valor + " via PIX");
    }
}
```

```java
/**
 * A "Factory" agora é só um localizador: o Spring injeta TODAS
 * as implementações de Pagamento em uma List automaticamente!
 *
 * Para adicionar um novo tipo de pagamento, basta criar uma nova
 * classe com @Component — NÃO precisa editar esta classe!
 */
@Component
public class PagamentoFactory {

    private final List<Pagamento> implementacoes;

    // Spring injeta automaticamente TODOS os @Component que implementam Pagamento
    public PagamentoFactory(List<Pagamento> implementacoes) {
        this.implementacoes = implementacoes;
    }

    public Pagamento criar(TipoPagamento tipo) {
        return implementacoes.stream()
            .filter(p -> p.suporta(tipo))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tipo não suportado: " + tipo));
    }
}
```

```java
// Uso em um service:
@Service
public class ProcessadorPagamentoService {

    private final PagamentoFactory factory;

    public ProcessadorPagamentoService(PagamentoFactory factory) {
        this.factory = factory;
    }

    public void processar(TipoPagamento tipo, double valor) {
        Pagamento pagamento = factory.criar(tipo);
        pagamento.processar(valor);
    }
}
```

## Factory usando Map (alternativa mais direta)

```java
/**
 * Outra forma: usar um Map<TipoPagamento, Pagamento> montado no construtor.
 * Mais rápido (O(1)) e ainda mais simples de ler.
 */
@Component
public class PagamentoFactoryComMap {

    private final Map<TipoPagamento, Pagamento> mapaImplementacoes;

    public PagamentoFactoryComMap(List<Pagamento> implementacoes) {
        // Constrói o mapa: para cada implementação, descobre qual tipo ela suporta
        this.mapaImplementacoes = new HashMap<>();
        for (var impl : implementacoes) {
            for (var tipo : TipoPagamento.values()) {
                if (impl.suporta(tipo)) {
                    mapaImplementacoes.put(tipo, impl);
                }
            }
        }
    }

    public Pagamento criar(TipoPagamento tipo) {
        var pagamento = mapaImplementacoes.get(tipo);
        if (pagamento == null) {
            throw new IllegalArgumentException("Tipo não suportado: " + tipo);
        }
        return pagamento;
    }
}
```

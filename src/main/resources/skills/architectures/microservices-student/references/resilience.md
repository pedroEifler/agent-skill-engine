# Resiliência com Resilience4j — Referência para Estudantes

## O que é Circuit Breaker?

```
Problema: o servico-produtos está lento. Cada chamada demora 30s e trava o servico-pedidos.

Solução — Circuit Breaker (disjuntor):

FECHADO (normal)    ABERTO (bloqueado)   MEIO-ABERTO (testando)
┌──────────────┐    ┌──────────────┐     ┌──────────────┐
│ Chamadas     │    │ Chamadas     │     │ Deixa passar │
│ passam       │ ──>│ BLOQUEADAS   │ ──> │ algumas      │
│ normalmente  │    │ → fallback!  │     │ chamadas     │
└──────────────┘    └──────────────┘     └──────────────┘
                    ↑                    ↑
              após X falhas         após Y segundos
```

## Configurando Resilience4j

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

```yaml
# application.yml do servico-pedidos
resilience4j:
  circuitbreaker:
    instances:
      servico-produtos:                  # nome do circuit breaker
        sliding-window-size: 10          # janela de 10 chamadas para calcular a taxa de erro
        failure-rate-threshold: 50       # abre o circuito se 50% das chamadas falharem
        wait-duration-in-open-state: 10s # fica aberto por 10 segundos antes de testar
        permitted-number-of-calls-in-half-open-state: 3  # testa com 3 chamadas

  retry:
    instances:
      servico-produtos:
        max-attempts: 3        # tenta no máximo 3 vezes
        wait-duration: 1s      # espera 1 segundo entre tentativas
```

## Usando Circuit Breaker no Service

```java
@Service
public class PedidoService {

    private final ProdutoClient produtoClient;

    /**
     * @CircuitBreaker: se falhar muito, abre o circuito e chama o método fallback.
     * @Retry: tenta novamente automáticamente antes de desistir.
     * name: deve ser o mesmo nome configurado no application.yml
     */
    @CircuitBreaker(name = "servico-produtos", fallbackMethod = "buscarProdutoFallback")
    @Retry(name = "servico-produtos")
    public ProdutoDTO buscarProduto(Long produtoId) {
        return produtoClient.buscarPorId(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    /**
     * Fallback: executado quando o circuit breaker abre ou todas as tentativas falham.
     * Assinatura deve ter os mesmos parâmetros + Throwable no final.
     */
    public ProdutoDTO buscarProdutoFallback(Long produtoId, Throwable t) {
        System.out.println("⚠️ Circuit breaker ativo para produto " + produtoId + ": " + t.getMessage());
        // Retorna um produto padrão ou lança uma exceção de negócio
        return new ProdutoDTO(produtoId, "Produto temporariamente indisponível",
            BigDecimal.ZERO);
    }
}
```

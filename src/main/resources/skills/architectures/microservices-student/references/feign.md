# Feign Client — Referência para Estudantes

## O que acontece por baixo dos panos?

```
Você escreve:           O Feign gera automaticamente:
                        (você nunca vê este código!)

@FeignClient(           public class ProdutoClientImpl {
  name="servico-          RestTemplate restTemplate;
  produtos")
interface               ProdutoDTO buscarPorId(Long id) {
ProdutoClient {           return restTemplate.getForObject(
  @GetMapping(            "http://servico-produtos/api/produtos/" + id,
    "/api/produtos/{id}") ProdutoDTO.class
  ProdutoDTO              );
  buscarPorId(          }
    @PathVariable       }
    Long id);
}
```

## Feign com Fallback — o que fazer quando o serviço cai?

```java
/**
 * Fallback: executado quando o servico-produtos está fora do ar.
 * Evita que o erro se propague — retorna um valor padrão.
 */
@Component
public class ProdutoClientFallback implements ProdutoClient {

    @Override
    public Optional<ProdutoDTO> buscarPorId(Long id) {
        // Retorna vazio ao invés de lançar exceção
        System.out.println("⚠️ servico-produtos indisponível! Usando fallback para id=" + id);
        return Optional.empty();
    }
}

// No Feign Client, vincule o fallback:
@FeignClient(
    name = "servico-produtos",
    fallback = ProdutoClientFallback.class  // ← usa o fallback quando der erro
)
public interface ProdutoClient {
    @GetMapping("/api/produtos/{id}")
    Optional<ProdutoDTO> buscarPorId(@PathVariable("id") Long id);
}
```

```yaml
# Habilitar fallback no application.yml
feign:
  circuitbreaker:
    enabled: true  # necessário para o fallback funcionar
```

## Feign com Timeout

```yaml
# Configurar tempo limite para chamadas Feign
spring:
  cloud:
    openfeign:
      client:
        config:
          # "default" aplica para todos os FeignClients
          default:
            connect-timeout: 3000   # 3 segundos para conectar
            read-timeout: 5000      # 5 segundos para ler a resposta
          # Configuração específica para um serviço
          servico-produtos:
            connect-timeout: 2000
            read-timeout: 3000
```

## Feign passando Headers (ex: token JWT)

```java
/**
 * Interceptor que adiciona o token JWT em todas as chamadas Feign.
 * O token do usuário logado é repassado automaticamente para os outros serviços.
 */
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Pega o token do contexto de segurança atual
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token) {
            // Adiciona o header Authorization em todas as requisições Feign
            template.header("Authorization", "Bearer " + token);
        }
    }
}
```

# Configuração com @Profile e @Qualifier — Referência para Estudantes

## Trocando adaptadores com @Profile

```properties
# application.properties

# Opção 1: persistência em memória (ótimo para começar a estudar!)
spring.profiles.active=memoria

# Opção 2: persistência com banco real
# spring.profiles.active=jpa

# Opção 3: combinando vários profiles
# spring.profiles.active=jpa,email,multi-canal
```

```bash
# Você também pode escolher o profile ao rodar a aplicação:
java -jar app.jar --spring.profiles.active=memoria

# Ou via variável de ambiente:
export SPRING_PROFILES_ACTIVE=jpa
```

## Quando há mais de um adaptador SEM @Profile — usando @Qualifier

```java
/**
 * Se você tiver DOIS adaptadores do MESMO tipo registrados ao mesmo tempo
 * (sem @Profile), o Spring não sabe qual injetar — dá erro!
 *
 * Solução: @Qualifier para escolher explicitamente.
 */
@Component("repositorioJpa")
public class TarefaJpaAdapter implements TarefaRepositoryPort { /* ... */ }

@Component("repositorioMemoria")
public class TarefaInMemoryAdapter implements TarefaRepositoryPort { /* ... */ }

// No service, escolha explicitamente:
@Service
public class TarefaService implements CriarTarefaPort {

    private final TarefaRepositoryPort repository;

    public TarefaService(@Qualifier("repositorioJpa") TarefaRepositoryPort repository) {
        this.repository = repository;
    }
    // ...
}
```

## Configuração Manual com @Configuration

```java
/**
 * Outra forma de "ligar" o núcleo aos adaptadores: configuração explícita.
 * Útil quando você quer deixar bem claro QUAL adaptador está sendo usado.
 */
@Configuration
public class HexagonalBeanConfig {

    /**
     * Define qual implementação de TarefaRepositoryPort será usada.
     * Para trocar, basta mudar esta linha — o núcleo (TarefaService) nem percebe!
     */
    @Bean
    public TarefaRepositoryPort tarefaRepositoryPort(TarefaJpaRepository jpaRepository) {
        return new TarefaJpaAdapter(jpaRepository);
        // Para usar em memória, troque para:
        // return new TarefaInMemoryAdapter();
    }

    @Bean
    public NotificadorPort notificadorPort(JavaMailSender mailSender) {
        return new EmailNotificadorAdapter(mailSender);
    }

    // O Spring injeta automaticamente os beans acima no TarefaService
    @Bean
    public TarefaService tarefaService(TarefaRepositoryPort repository, NotificadorPort notificador) {
        return new TarefaService(repository, notificador);
    }
}
```

## Profiles para Ambientes (dev, test, prod)

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# application-dev.yml (perfil de desenvolvimento)
spring:
  config:
    activate:
      on-profile: dev
# Usa adaptador em memória — rápido para desenvolver
app:
  adapter:
    repository: memoria
    notificador: console

---
# application-prod.yml (perfil de produção)
spring:
  config:
    activate:
      on-profile: prod
# Usa adaptadores reais
app:
  adapter:
    repository: jpa
    notificador: email
```

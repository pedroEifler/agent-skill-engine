# Singleton Manual vs Spring — Referência para Estudantes

## Quando usar cada abordagem?

```
Singleton Manual → quando NÃO há Spring no projeto
                   (aplicações Java SE, bibliotecas, ferramentas)

Spring @Service  → SEMPRE que estiver usando Spring Boot
                   O Spring gerencia o ciclo de vida, é mais testável,
                   e você ganha injeção de dependência de graça!
```

## No Spring: todo @Component já é Singleton!

```java
// @Service é Singleton por padrão — zero código extra necessário!
@Service
public class ServicoEmail {

    // O Spring injeta os colaboradores automaticamente
    private final JavaMailSender mailSender;

    public ServicoEmail(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviar(String para, String assunto, String corpo) {
        // implementação...
    }
}

// Em qualquer lugar que precisar do ServicoEmail, basta injetar:
@Service
public class ServicoPedido {
    private final ServicoEmail servicoEmail; // Spring injeta a MESMA instância

    public ServicoPedido(ServicoEmail servicoEmail) {
        this.servicoEmail = servicoEmail;
    }
}
```

## Forçando múltiplas instâncias no Spring com @Scope

```java
// Se QUISER múltiplas instâncias (comportamento contrário ao Singleton):
@Component
@Scope("prototype") // uma nova instância cada vez que for injetado
public class RelatorioTemporario {
    private final List<String> linhas = new ArrayList<>();
    // cada injeção cria um novo objeto — útil para estado temporário por request!
}
```

## Configuração de beans como Singleton com @Bean

```java
@Configuration
public class AppConfig {

    /**
     * Este @Bean é Singleton por padrão — o Spring chama este método
     * UMA ÚNICA VEZ e reutiliza a instância em todos os lugares.
     */
    @Bean
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper; // mesma instância em toda a aplicação!
    }
}
```

## Comparação prática: Logger manual vs Spring

```java
// ❌ Logger Singleton manual — sem injeção, difícil de testar
public class ProcessadorArquivo {
    public void processar(String arquivo) {
        Logger.INSTANCIA.log("INFO", "Processando: " + arquivo); // acoplado!
    }
}

// ✅ Logger via Spring — injeção de dependência, fácil de testar
@Service
public class ProcessadorArquivoSpring {
    private static final org.slf4j.Logger log =
        LoggerFactory.getLogger(ProcessadorArquivoSpring.class);

    public void processar(String arquivo) {
        log.info("Processando: {}", arquivo); // SLF4J cuida de tudo!
    }
}
```

## Quando o Singleton Manual AINDA faz sentido?

```java
// 1. Fora do Spring (Java SE puro):
public enum AppConfig {
    INSTANCIA;
    private final Properties props = carregarPropriedades();

    private Properties carregarPropriedades() {
        var p = new Properties();
        try (var stream = getClass().getResourceAsStream("/config.properties")) {
            p.load(stream);
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível carregar configurações", e);
        }
        return p;
    }

    public String get(String chave) { return props.getProperty(chave); }
}

// 2. Biblioteca reutilizável (não pode depender do Spring):
public class ConectorExterno {
    // Singleton necessário pois a biblioteca não sabe o contexto do usuário
}
```

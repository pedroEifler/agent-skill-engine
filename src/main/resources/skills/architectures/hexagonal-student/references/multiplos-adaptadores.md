# Múltiplos Adaptadores — Referência para Estudantes

## Adaptador de Entrada: Mensageria (RabbitMQ)

```java
/**
 * Adaptador DRIVING #3: escuta uma fila e aciona o núcleo.
 *
 * Mesma ideia do REST Controller e do CLI Runner — só muda QUEM aciona,
 * o núcleo (TarefaService) continua o mesmo!
 */
@Component
public class TarefaEventListener {

    private final CriarTarefaPort criarTarefa;

    public TarefaEventListener(CriarTarefaPort criarTarefa) {
        this.criarTarefa = criarTarefa;
    }

    /**
     * Quando chega uma mensagem na fila "fila.criar-tarefa", cria a tarefa
     * usando a MESMA porta que o REST Controller usa!
     */
    @RabbitListener(queues = "fila.criar-tarefa")
    public void aoReceberMensagem(CriarTarefaMessage mensagem) {
        System.out.println("📥 Mensagem recebida: " + mensagem.titulo());
        criarTarefa.executar(mensagem.titulo());
    }
}

public record CriarTarefaMessage(String titulo) { }
```

## Adaptador de Saída: Notificação por Email vs SMS

```java
/**
 * Adaptador DRIVEN: envia email quando uma tarefa é concluída.
 */
@Component
@Profile("email")
public class EmailNotificadorAdapter implements NotificadorPort {

    private final JavaMailSender mailSender;

    public EmailNotificadorAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void notificarConclusao(Tarefa tarefa) {
        var mensagem = new SimpleMailMessage();
        mensagem.setTo("usuario@exemplo.com");
        mensagem.setSubject("Tarefa Concluída!");
        mensagem.setText("A tarefa '" + tarefa.getTitulo() + "' foi concluída.");
        mailSender.send(mensagem);
        System.out.println("📧 Email enviado para tarefa: " + tarefa.getTitulo());
    }
}
```

```java
/**
 * Adaptador DRIVEN alternativo: "envia" SMS (simulado para estudo).
 *
 * Para trocar de email para SMS, basta mudar o profile ativo!
 * O TarefaService (núcleo) não precisa saber nada sobre essa troca.
 */
@Component
@Profile("sms")
public class SmsNotificadorAdapter implements NotificadorPort {

    @Override
    public void notificarConclusao(Tarefa tarefa) {
        // Em um projeto real, chamaria uma API de SMS (Twilio, etc.)
        System.out.println("📱 SMS simulado: Tarefa '" + tarefa.getTitulo() + "' concluída!");
    }
}
```

## Adaptador de Saída: Composite (envia para todos os canais!)

```java
/**
 * Adaptador DRIVEN especial: combina VÁRIOS notificadores em um só.
 *
 * Demonstra o poder de Ports & Adapters: você pode até combinar adapters
 * sem o núcleo perceber qualquer diferença!
 */
@Component
@Profile("multi-canal")
public class NotificadorCompositeAdapter implements NotificadorPort {

    private final List<NotificadorPort> notificadores;

    // Spring injeta TODOS os beans que implementam NotificadorPort
    public NotificadorCompositeAdapter(List<NotificadorPort> notificadores) {
        this.notificadores = notificadores;
    }

    @Override
    public void notificarConclusao(Tarefa tarefa) {
        // Notifica por TODOS os canais disponíveis (email, SMS, push...)
        notificadores.forEach(n -> n.notificarConclusao(tarefa));
    }
}
```

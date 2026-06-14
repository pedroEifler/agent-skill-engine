# Mensageria — Referência para Estudantes

## RabbitMQ vs Kafka — quando usar cada um?

```
RabbitMQ → melhor para:           Kafka → melhor para:
• Tarefas assíncronas             • Streaming de eventos em tempo real
• Filas de trabalho (jobs)        • Histórico de eventos (log imutável)
• Rotear mensagens complexas      • Alto volume (milhões de msgs/seg)
• Confirmação de entrega          • Replay de eventos
```

## RabbitMQ — Configuração Completa

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

```java
/**
 * Configura as filas e exchanges do RabbitMQ.
 *
 * Exchange → recebe a mensagem e decide para qual fila enviar
 * Queue    → fila onde as mensagens ficam esperando ser consumidas
 * Binding  → regra que conecta exchange à fila
 */
@Configuration
public class RabbitConfig {

    public static final String FILA_PEDIDOS = "fila.pedidos";
    public static final String EXCHANGE_PEDIDOS = "exchange.pedidos";
    public static final String ROUTING_KEY_PEDIDO_CRIADO = "pedido.criado";

    // Declara a fila — durable=true: a fila sobrevive ao reinício do RabbitMQ
    @Bean
    public Queue filaPedidos() {
        return QueueBuilder.durable(FILA_PEDIDOS).build();
    }

    // Exchange do tipo Topic: roteia mensagens por padrão de routing key
    @Bean
    public TopicExchange exchangePedidos() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    // Binding: conecta a fila ao exchange com a routing key
    @Bean
    public Binding bindingPedidos(Queue filaPedidos, TopicExchange exchangePedidos) {
        return BindingBuilder
            .bind(filaPedidos)                          // fila de destino
            .to(exchangePedidos)                        // exchange de origem
            .with(ROUTING_KEY_PEDIDO_CRIADO);           // padrão de chave de roteamento
    }

    // Configura serialização JSON para as mensagens
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

```java
// Produtor — envia mensagem
@Service
public class PedidoPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publicar(PedidoCriadoEvent evento) {
        // Envia para o exchange com a routing key → RabbitMQ roteará para a fila correta
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_PEDIDOS,
            RabbitConfig.ROUTING_KEY_PEDIDO_CRIADO,
            evento
        );
        System.out.println("📤 Evento enviado: " + evento);
    }
}

// Consumidor — recebe mensagem
@Service
public class PagamentoConsumer {

    // @RabbitListener → este método é chamado automaticamente quando chega uma mensagem
    @RabbitListener(queues = RabbitConfig.FILA_PEDIDOS)
    public void processar(PedidoCriadoEvent evento) {
        System.out.println("📥 Processando pagamento para pedido: " + evento.pedidoId());
        // lógica de pagamento aqui...
    }
}
```

## Kafka — Configuração Básica

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092  # endereço do broker Kafka
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: servico-pagamentos  # grupo de consumidores — importante!
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "br.com.projeto.*"
```

```java
// Produtor Kafka
@Service
public class PedidoKafkaPublisher {
    private final KafkaTemplate<String, PedidoCriadoEvent> kafkaTemplate;

    public void publicar(PedidoCriadoEvent evento) {
        // Envia para o tópico "pedidos-criados"
        kafkaTemplate.send("pedidos-criados", evento.pedidoId().toString(), evento);
        System.out.println("📤 Evento Kafka enviado: " + evento);
    }
}

// Consumidor Kafka
@Service
public class PagamentoKafkaConsumer {

    @KafkaListener(topics = "pedidos-criados", groupId = "servico-pagamentos")
    public void processar(PedidoCriadoEvent evento) {
        System.out.println("📥 Kafka: processando pedido " + evento.pedidoId());
    }
}
```

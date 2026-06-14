# Messaging — Business Reference

## Kafka — Production Configuration

```java
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties props) {
        var config = new HashMap<String, Object>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");               // wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // exactly-once semantics
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties props) {
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumer().getGroupId());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // manual ack
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.company.*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> cf) {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new DeadLetterPublishingRecoverer(kafkaTemplate()),
            new FixedBackOff(1000L, 3)));
        return factory;
    }
}
```

## Dead Letter Queue (DLQ)

```java
@KafkaListener(topics = "order.placed", groupId = "payment-service")
public void handle(OrderPlacedEvent event, Acknowledgment ack) {
    try {
        processPayment.execute(event);
        ack.acknowledge();
    } catch (TransientException e) {
        throw e; // triggers retry → DLQ after max attempts
    } catch (PermanentException e) {
        log.error("Permanent failure for order {}: {}", event.orderId(), e.getMessage());
        ack.acknowledge(); // ack to avoid retry, alert separately
    }
}

// DLQ consumer for manual review
@KafkaListener(topics = "order.placed.DLT", groupId = "payment-service-dlq")
public void handleDlq(OrderPlacedEvent event, @Header KafkaHeaders.DLT_EXCEPTION_MESSAGE String reason) {
    alertingService.notify("Payment DLQ", event.orderId(), reason);
    dlqRepository.save(DeadLetterRecord.of(event, reason));
}
```

## Transactional Outbox

```java
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id private UUID id = UUID.randomUUID();
    @Column private String aggregateType;
    @Column private String aggregateId;
    @Column private String eventType;
    @Column(columnDefinition = "jsonb") private String payload;
    @Column private Instant createdAt = Instant.now();
    @Column private boolean published = false;
}

@Service
@Transactional
public class PlaceOrderUseCase {

    @Override
    public OrderResponse execute(PlaceOrderCommand cmd) {
        var order = buildOrder(cmd);
        order.confirm();
        orderRepository.save(order);
        // Save event to outbox in the SAME transaction
        outboxRepository.save(OutboxEvent.from(new OrderPlacedEvent(...)));
        return OrderResponse.from(order);
    }
}

@Scheduled(fixedDelay = 1000)
@Transactional
public void publishOutboxEvents() {
    outboxRepository.findUnpublished(PageRequest.of(0, 100)).forEach(event -> {
        kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
        event.setPublished(true);
    });
}
```

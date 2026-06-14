---
name: microservices-student
description: >
  Use esta skill sempre que um estudante pedir para criar um projeto com microserviços, arquitetura
  distribuída, comunicação entre serviços, API Gateway, Service Discovery, ou qualquer componente
  típico de microserviços em Java com Spring Boot. Triggers incluem: "microserviços", "microservices",
  "como separar serviços", "comunicação entre microserviços", "API Gateway", "Eureka", "Service Discovery",
  "Feign Client", "Spring Cloud", "como funciona microserviço", "projeto com vários serviços",
  "comunicação REST entre serviços", "mensageria entre serviços", "Kafka microserviços", "RabbitMQ",
  "circuit breaker", "Resilience4j para estudar". Gera código em português com comentários explicativos
  em cada componente, diagramas textuais do fluxo, e exemplos didáticos de comunicação síncrona e
  assíncrona. SEMPRE use esta skill quando o estudante mencionar microserviços ou Spring Cloud.
---

# Skill: Microserviços para Estudantes 🔧

Gera projetos Java com arquitetura de microserviços, explicando cada componente com comentários
didáticos em português. Foco em Spring Boot + Spring Cloud.

---

## 1. O que são Microserviços?

Sempre explique o conceito antes de gerar o código:

```
Monolito vs Microserviços:

MONOLITO (tudo junto):               MICROSERVIÇOS (separados):
┌──────────────────────┐             ┌──────────┐  ┌──────────┐
│    Minha Aplicação   │             │ Pedidos  │  │ Produtos │
│  ┌────────────────┐  │             │ Serviço  │  │ Serviço  │
│  │   Pedidos      │  │             └────┬─────┘  └────┬─────┘
│  │   Produtos     │  │                  │              │
│  │   Pagamentos   │  │             ┌────┴─────┐  ┌────┴─────┐
│  │   Usuários     │  │             │Pagamentos│  │ Usuários │
│  └────────────────┘  │             │ Serviço  │  │ Serviço  │
└──────────────────────┘             └──────────┘  └──────────┘

Cada serviço:
✅ Tem seu próprio banco de dados
✅ Pode ser implantado independentemente
✅ Pode escalar individualmente
✅ Tem uma responsabilidade única
```

---

## 2. Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY                            │
│         (porta de entrada única — 8080)                     │
│    Roteia para o serviço certo, autentica, limita taxa      │
└──────────────────────────┬──────────────────────────────────┘
                           │
         ┌─────────────────┼──────────────────┐
         ▼                 ▼                  ▼
┌────────────────┐ ┌────────────────┐ ┌────────────────┐
│ SERVIÇO DE     │ │ SERVIÇO DE     │ │ SERVIÇO DE     │
│ PEDIDOS        │ │ PRODUTOS       │ │ PAGAMENTOS     │
│ :8081          │ │ :8082          │ │ :8083          │
└───────┬────────┘ └───────┬────────┘ └───────┬────────┘
        │                  │                   │
        └──────────────────┴───────────────────┘
                           │
                    ┌──────┴──────┐
                    │   EUREKA    │
                    │  (registro) │
                    │    :8761    │
                    └─────────────┘

Comunicação SÍNCRONA:  Pedidos → Produtos (Feign Client via HTTP)
Comunicação ASSÍNCRONA: Pedidos → Kafka → Pagamentos (event-driven)
```

---

## 3. Estrutura de Múltiplos Projetos

```
sistema-loja/
├── eureka-server/              ← Registro de serviços (quem está online?)
│   ├── src/
│   └── pom.xml
├── api-gateway/                ← Porta de entrada única
│   ├── src/
│   └── pom.xml
├── servico-produtos/           ← Gerencia o catálogo de produtos
│   ├── src/
│   └── pom.xml
├── servico-pedidos/            ← Gerencia pedidos dos clientes
│   ├── src/
│   └── pom.xml
├── servico-pagamentos/         ← Processa pagamentos
│   ├── src/
│   └── pom.xml
└── docker-compose.yml          ← Sobe tudo junto com um comando!
```

---

## 4. Eureka Server — Registro de Serviços

O Eureka é como uma "lista telefônica" dos serviços. Cada serviço se registra aqui ao iniciar.

```xml
<!-- pom.xml do eureka-server -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>

<dependencies>
    <!-- Eureka Server: provê o painel de registro de serviços -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <!-- Gerencia versões de todo o ecossistema Spring Cloud -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```java
/**
 * Classe principal do Eureka Server.
 *
 * @EnableEurekaServer → ativa o servidor de registro.
 * Acesse http://localhost:8761 para ver o painel com todos os serviços registrados!
 */
@SpringBootApplication
@EnableEurekaServer  // ← esta anotação faz toda a mágica!
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml
# eureka-server/application.yml
server:
  port: 8761  # porta padrão do Eureka

eureka:
  instance:
    hostname: localhost
  client:
    # O próprio servidor não precisa se registrar em si mesmo
    register-with-eureka: false
    fetch-registry: false
```

---

## 5. Serviço de Produtos — Registrando-se no Eureka

```xml
<!-- pom.xml do servico-produtos -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- Eureka Client: permite que este serviço se registre no Eureka Server -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

```yaml
# servico-produtos/application.yml
server:
  port: 8082

spring:
  application:
    # MUITO IMPORTANTE: este é o nome que outros serviços usam para encontrar este!
    name: servico-produtos
  datasource:
    url: jdbc:h2:mem:produtosdb
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

eureka:
  client:
    # Endereço do Eureka Server para se registrar
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

```java
/**
 * Controller do Serviço de Produtos.
 *
 * Este serviço expõe uma API REST que OUTROS serviços podem chamar.
 * O Feign Client do serviço-pedidos vai chamar estes endpoints!
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // Este endpoint é chamado pelo serviço-pedidos via Feign Client
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return produtoRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }
}
```

---

## 6. Serviço de Pedidos — Chamando outro Serviço com Feign

O Feign Client permite chamar outro serviço como se fosse um método Java normal!

```xml
<!-- Adicione no pom.xml do servico-pedidos -->
<!-- Feign Client: facilita chamadas HTTP entre microserviços -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
/**
 * Classe principal — @EnableFeignClients ativa a geração automática dos clientes HTTP.
 */
@SpringBootApplication
@EnableFeignClients  // ← obrigatório para usar @FeignClient!
public class ServicoPedidosApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServicoPedidosApplication.class, args);
    }
}
```

```java
/**
 * Feign Client para o Serviço de Produtos.
 *
 * @FeignClient: o Spring gera automaticamente a implementação desta interface!
 * Você só declara os métodos e o Spring cuida de fazer as chamadas HTTP.
 *
 * name = "servico-produtos" → nome exato do serviço no Eureka (spring.application.name)
 * O Feign usa o Eureka para descobrir o endereço real do serviço automaticamente!
 */
@FeignClient(name = "servico-produtos")
public interface ProdutoClient {

    // Este método vai fazer um GET para http://servico-produtos/api/produtos/{id}
    // Tudo transparente — parece um método local, mas na verdade é uma chamada HTTP!
    @GetMapping("/api/produtos/{id}")
    Optional<ProdutoDTO> buscarPorId(@PathVariable("id") Long id);
}
```

```java
/**
 * Serviço de Pedidos — usa o Feign Client para buscar dados do serviço de produtos.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoClient produtoClient;  // injeção do Feign Client

    public PedidoService(PedidoRepository pedidoRepository, ProdutoClient produtoClient) {
        this.pedidoRepository = pedidoRepository;
        this.produtoClient = produtoClient;
    }

    public Pedido criarPedido(Long clienteId, Long produtoId, int quantidade) {
        // Busca informações do produto NO OUTRO SERVIÇO via Feign Client
        // Por baixo dos panos, o Feign faz uma chamada HTTP GET para o servico-produtos
        var produto = produtoClient.buscarPorId(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

        // Cria o pedido com as informações recebidas
        var pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setProdutoId(produtoId);
        pedido.setNomeProduto(produto.nome()); // dado veio do outro serviço!
        pedido.setQuantidade(quantidade);
        pedido.setTotal(produto.preco().multiply(BigDecimal.valueOf(quantidade)));
        pedido.setStatus("CRIADO");

        return pedidoRepository.save(pedido);
    }
}
```

---

## 7. API Gateway — Porta de Entrada Única

```xml
<!-- pom.xml do api-gateway -->
<dependencies>
    <!-- Spring Cloud Gateway: roteia requisições para os serviços certos -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <!-- Eureka Client: para descobrir os endereços dos serviços -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

```yaml
# api-gateway/application.yml
server:
  port: 8080  # porta única de entrada para TODOS os serviços

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # Regra: qualquer requisição começando com /api/produtos vai para servico-produtos
        - id: rota-produtos
          uri: lb://servico-produtos    # lb:// = load balancer (usa o Eureka para descobrir)
          predicates:
            - Path=/api/produtos/**     # padrão de URL que ativa esta rota
          filters:
            - StripPrefix=0             # mantém o prefixo /api/produtos

        # Regra: /api/pedidos vai para servico-pedidos
        - id: rota-pedidos
          uri: lb://servico-pedidos
          predicates:
            - Path=/api/pedidos/**
```

---

## 8. Comunicação Assíncrona com RabbitMQ

Quando o Pedido é criado, enviamos uma mensagem para o serviço de Pagamentos processar.

```java
/**
 * Produtor de mensagens no serviço-pedidos.
 * Envia um evento "pedido criado" para o RabbitMQ.
 */
@Service
public class PedidoEventPublisher {

    // RabbitTemplate é o "enviador" de mensagens do Spring
    private final RabbitTemplate rabbitTemplate;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarPedidoCriado(Pedido pedido) {
        var evento = new PedidoCriadoEvent(pedido.getId(), pedido.getTotal());

        // Envia a mensagem para a fila "fila.pedidos"
        // O serviço de pagamentos está "ouvindo" esta fila!
        rabbitTemplate.convertAndSend("fila.pedidos", evento);
        System.out.println("📤 Mensagem enviada para fila.pedidos: " + evento);
    }
}
```

```java
/**
 * Consumidor de mensagens no serviço-pagamentos.
 * Escuta a fila e processa o pagamento quando um pedido é criado.
 */
@Service
public class PagamentoListener {

    @RabbitListener(queues = "fila.pedidos") // ← escuta esta fila continuamente
    public void processarPagamento(PedidoCriadoEvent evento) {
        System.out.println("📥 Pedido recebido para pagamento: " + evento.pedidoId());
        // Aqui processaria o pagamento de fato...
        System.out.println("✅ Pagamento processado para pedido: " + evento.pedidoId());
    }
}
```

---

## 9. docker-compose.yml — Subindo Tudo Junto

```yaml
# docker-compose.yml na raiz do projeto
version: '3.8'
services:

  # Registro de serviços
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"   # painel web disponível em http://localhost:8761

  # Fila de mensagens
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"   # porta da fila
      - "15672:15672" # painel web: http://localhost:15672 (guest/guest)

  # Serviços da aplicação
  servico-produtos:
    build: ./servico-produtos
    ports:
      - "8082:8082"
    depends_on:
      - eureka-server   # espera o Eureka iniciar primeiro

  servico-pedidos:
    build: ./servico-pedidos
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - rabbitmq

  servico-pagamentos:
    build: ./servico-pagamentos
    ports:
      - "8083:8083"
    depends_on:
      - eureka-server
      - rabbitmq

  # Porta de entrada única
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"   # acesse todos os serviços por aqui!
    depends_on:
      - eureka-server
```

---

## 10. Checklist Antes de Gerar

- [ ] `spring.application.name` único em cada serviço?
- [ ] `eureka.client.service-url.defaultZone` configurado?
- [ ] `@EnableFeignClients` na classe principal do serviço que faz chamadas?
- [ ] Nome no `@FeignClient(name=...)` bate com o `spring.application.name` do serviço alvo?
- [ ] API Gateway com prefixo `lb://` para usar o load balancer?
- [ ] docker-compose com `depends_on` na ordem correta?

---

## 11. Formato de Entrega

1. **Diagrama textual** da arquitetura com todos os serviços
2. **Cada serviço** em ordem: Eureka → Gateway → Serviços de domínio
3. **application.yml** de cada serviço com comentários
4. **Comunicação síncrona** (Feign Client) e **assíncrona** (RabbitMQ/Kafka)
5. **docker-compose.yml** para rodar tudo
6. **Dica**: ordem de inicialização dos serviços

Consulte os arquivos de referência:
- `references/feign.md` — Feign Client avançado, fallback, timeout
- `references/messaging.md` — RabbitMQ e Kafka com comentários
- `references/resilience.md` — Circuit Breaker com Resilience4j
- `references/config.md` — Spring Cloud Config Server
- `references/tests.md` — Testando microserviços com WireMock

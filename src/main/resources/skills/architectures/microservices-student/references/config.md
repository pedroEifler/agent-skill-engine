# Spring Cloud Config Server — Referência para Estudantes

## O que é o Config Server?

```
Problema: 10 microserviços, cada um com seu application.yml.
Mudar uma configuração = atualizar 10 arquivos e reimplantar tudo!

Solução: Config Server centraliza TODAS as configurações em um só lugar.

┌─────────────────────┐
│   Git Repository    │  ← configurações versionadas aqui
│  config-repo/       │
│  ├── servico-       │
│  │   produtos.yml   │
│  ├── servico-       │
│  │   pedidos.yml    │
│  └── application.yml│
└──────────┬──────────┘
           │ sincroniza
    ┌──────▼──────┐
    │   CONFIG    │  ← porta 8888
    │   SERVER    │
    └──────┬──────┘
           │ fornece configurações
    ┌──────┴──────┬──────────────┐
    ▼             ▼              ▼
servico-      servico-      api-gateway
produtos      pedidos
```

## Config Server

```xml
<!-- pom.xml do config-server -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableConfigServer  // ← ativa o servidor de configuração
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

```yaml
# config-server/application.yml
server:
  port: 8888

spring:
  cloud:
    config:
      server:
        git:
          # Repositório Git com os arquivos de configuração
          uri: https://github.com/seu-usuario/config-repo
          default-label: main  # branch padrão
          clone-on-start: true
```

## Config Client (em cada microserviço)

```xml
<!-- pom.xml de cada microserviço -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

```yaml
# bootstrap.yml (carregado ANTES do application.yml — necessário para o Config Client)
spring:
  application:
    name: servico-produtos  # busca o arquivo "servico-produtos.yml" no Config Server
  config:
    import: configserver:http://localhost:8888
```

## Atualizando Configurações sem Reiniciar (@RefreshScope)

```java
/**
 * @RefreshScope → este bean é recriado quando você chama POST /actuator/refresh.
 * Útil para atualizar configurações sem reiniciar o serviço!
 */
@RestController
@RefreshScope  // ← as configurações injetadas aqui se atualizam dinamicamente
public class ConfigController {

    // Valor buscado do Config Server — se mudar no Git, atualiza ao fazer o refresh
    @Value("${app.taxa-desconto:0.1}")
    private double taxaDesconto;

    @GetMapping("/taxa")
    public double getTaxa() {
        return taxaDesconto;
    }
}
```

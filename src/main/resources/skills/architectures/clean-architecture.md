# Clean Architecture - Skills & Best Practices

## Princípio Fundamental
A regra de dependência: dependências apontam **sempre para dentro** (das camadas externas para as internas). Camadas internas nunca conhecem camadas externas.

## Camadas

### 1. Domain (Entities)
- Contém **entidades de domínio** e **value objects** — regras de negócio puras.
- Zero dependências de frameworks, bibliotecas ou infraestrutura.
- Entidades encapsulam comportamento (não são meros data holders).
- Value Objects são imutáveis e comparados por valor.

```
domain/
├── model/
│   ├── Usuario.java
│   └── Email.java (Value Object)
├── exception/
│   └── UsuarioNaoEncontradoException.java
└── gateway/
    └── UsuarioGateway.java (interface/port)
```

### 2. Use Cases (Application)
- Orquestram o fluxo de negócio — **um use case por ação do sistema**.
- Dependem apenas da camada Domain.
- Definem **ports** (interfaces) para dependências externas.
- Não contêm lógica de apresentação nem de infraestrutura.

```
usecase/
├── CriarUsuarioUseCase.java
├── BuscarUsuarioPorIdUseCase.java
└── ListarUsuariosUseCase.java
```

### 3. Interface Adapters (Adapters)
- **Controllers**: convertem HTTP requests em chamadas a use cases.
- **Presenters/DTOs**: transformam entidades de domínio em respostas da API.
- **Gateways/Repositories**: implementam as interfaces definidas na camada de domínio.

```
adapter/
├── controller/
│   ├── UsuarioController.java
│   └── dto/
│       ├── CriarUsuarioRequest.java
│       └── UsuarioResponse.java
├── gateway/
│   └── UsuarioGatewayImpl.java
└── mapper/
    └── UsuarioMapper.java
```

### 4. Infrastructure (Frameworks & Drivers)
- Configurações de framework (Spring, JPA, etc.).
- Implementações concretas de persistência, mensageria, HTTP clients.
- Esta camada é **plugável** — pode ser substituída sem afetar o domínio.

```
infrastructure/
├── persistence/
│   ├── UsuarioEntity.java (JPA Entity)
│   ├── UsuarioJpaRepository.java
│   └── UsuarioGatewayJpa.java
├── config/
│   └── BeanConfiguration.java
└── external/
    └── EmailServiceImpl.java
```

## Regras de Ouro

1. **Entidades de domínio ≠ Entidades JPA** — mantenha separadas. Use mappers para converter.
2. **Use Cases recebem e retornam objetos de domínio** — nunca DTOs de controller.
3. **Inversão de dependência**: use cases definem interfaces (ports), infraestrutura implementa (adapters).
4. **Um Use Case = Uma responsabilidade** — evite "God Services" com dezenas de métodos.
5. **Testabilidade**: use cases são testáveis sem framework — apenas mocks das interfaces.

## Comunicação entre Camadas

```
Controller → UseCase → Gateway(interface)
                            ↑
                    GatewayImpl (infrastructure)
```

## Mapeamento de Objetos
- **Request DTO → Domain Model**: no controller ou em um mapper dedicado.
- **Domain Model → Response DTO**: no controller ou presenter.
- **Domain Model ↔ JPA Entity**: no gateway de infraestrutura.
- Use bibliotecas como MapStruct ou mapeie manualmente para manter controle.

## Testes por Camada
- **Domain**: testes unitários puros (sem framework).
- **Use Cases**: testes unitários com mocks dos gateways.
- **Adapters**: testes de integração (`@WebMvcTest`, `@DataJpaTest`).
- **E2E**: `@SpringBootTest` com Testcontainers.

## Anti-patterns a Evitar
- ❌ Entidade JPA na camada de domínio.
- ❌ Use case dependendo de `HttpServletRequest` ou `@Transactional`.
- ❌ Controller com lógica de negócio.
- ❌ Camada de domínio importando Spring/JPA/Hibernate.
- ❌ "Anemic Domain Model" — entidades sem comportamento.


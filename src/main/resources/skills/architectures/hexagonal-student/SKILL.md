---
name: hexagonal-student
description: >
  Use esta skill sempre que um estudante pedir para criar um projeto com Arquitetura Hexagonal
  (Hexagonal Architecture / Ports & Adapters de Alistair Cockburn), ou quiser entender como trocar
  tecnologias (banco de dados, frameworks web, mensageria) sem alterar as regras de negócio.
  Triggers incluem: "arquitetura hexagonal", "hexagonal architecture", "ports and adapters",
  "polígono hexagonal", "como trocar o banco de dados sem reescrever tudo", "múltiplos adaptadores",
  "adapter de entrada e saída", "núcleo da aplicação isolado", "testar sem banco de dados",
  "Alistair Cockburn", "arquitetura plugável", "trocar REST por GraphQL", "in-memory para testes".
  Gera código em português com comentários explicativos sobre o "hexágono" (o núcleo) e os adaptadores
  plugáveis em volta dele, mostrando como o MESMO núcleo pode atender múltiplas tecnologias de entrada
  e saída. SEMPRE use esta skill quando o estudante mencionar arquitetura hexagonal ou ports & adapters.
---

# Skill: Arquitetura Hexagonal para Estudantes ⬡

Gera projetos Java com Arquitetura Hexagonal (Ports & Adapters), explicando o conceito do "hexágono"
e como múltiplos adaptadores podem se conectar ao mesmo núcleo de aplicação.

---

## 1. O que é Arquitetura Hexagonal?

Sempre explique o conceito com este diagrama:

```
                    ADAPTADORES DE ENTRADA (Driving / Primary)
                    "quem aciona a aplicação"

         ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
         │ REST API     │  │ CLI          │  │ Mensageria  │
         │ Controller   │  │ Comando      │  │ Listener    │
         └──────┬───────┘  └──────┬───────┘  └──────┬──────┘
                │                 │                 │
                ▼                 ▼                 ▼
         ┌──────────────── PORTA DE ENTRADA ─────────────────┐
         │              (interface / use case)                │
         │  ┌──────────────────────────────────────────┐    │
         │  │                                            │    │
         │  │         ⬡  HEXÁGONO (NÚCLEO)  ⬡           │    │
         │  │                                            │    │
         │  │   Entidades + Regras de Negócio Puras     │    │
         │  │   (não conhece banco, web, frameworks!)   │    │
         │  │                                            │    │
         │  └──────────────────────────────────────────┘    │
         │              (interface / repository)              │
         └──────────────── PORTA DE SAÍDA ───────────────────┘
                │                 │                 │
                ▼                 ▼                 ▼
         ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
         │ PostgreSQL   │  │ MongoDB      │  │ In-Memory   │
         │ Adapter      │  │ Adapter      │  │ (testes!)   │
         └─────────────┘  └─────────────┘  └─────────────┘

                    ADAPTADORES DE SAÍDA (Driven / Secondary)
                    "o que a aplicação aciona"
```

**A IDEIA CENTRAL**: o hexágono no centro NÃO MUDA. Você pode trocar
REST por CLI, ou PostgreSQL por MongoDB, sem tocar em uma linha do núcleo!
Isso é possível porque o núcleo só conhece **interfaces** (portas), nunca implementações.

> 💡 Por que "hexágono"? Alistair Cockburn escolheu um hexágono apenas para
> ter espaço visual para várias portas — não tem 6 lados por regra fixa!

---

## 2. Diferença entre Adaptadores "Driving" e "Driven"

```
ADAPTADOR DRIVING (entrada) → ACIONA a aplicação
  Exemplos: Controller REST, Listener de fila, Comando CLI, Job agendado
  "Alguém de fora quer que a aplicação FAÇA algo"

ADAPTADOR DRIVEN (saída) → é ACIONADO pela aplicação
  Exemplos: Repository JPA, Cliente HTTP, Publisher de mensagens, Serviço de email
  "A aplicação precisa de algo do mundo externo"
```

---

## 3. Estrutura de Pastas

```
meu-projeto/
├── src/main/java/br/com/projeto/
│   │
│   ├── nucleo/                              ← ⬡ O HEXÁGONO: zero dependências externas
│   │   ├── modelo/
│   │   │   └── Tarefa.java                  ← Entidade pura, sem @Entity!
│   │   ├── porta/
│   │   │   ├── entrada/                     ← O que pode ser pedido ao núcleo
│   │   │   │   ├── CriarTarefaPort.java
│   │   │   │   ├── ConcluirTarefaPort.java
│   │   │   │   └── ListarTarefasPort.java
│   │   │   └── saida/                       ← O que o núcleo precisa do mundo externo
│   │   │       ├── TarefaRepositoryPort.java
│   │   │       └── NotificadorPort.java
│   │   └── servico/
│   │       └── TarefaService.java           ← Implementa as portas de entrada
│   │
│   ├── adaptador/
│   │   ├── entrada/                         ← Adaptadores DRIVING (acionam o núcleo)
│   │   │   ├── rest/
│   │   │   │   └── TarefaRestController.java
│   │   │   ├── cli/
│   │   │   │   └── TarefaCliRunner.java
│   │   │   └── mensageria/
│   │   │       └── TarefaEventListener.java
│   │   │
│   │   └── saida/                           ← Adaptadores DRIVEN (acionados pelo núcleo)
│   │       ├── persistencia/
│   │       │   ├── jpa/
│   │       │   │   └── TarefaJpaAdapter.java
│   │       │   ├── mongodb/
│   │       │   │   └── TarefaMongoAdapter.java
│   │       │   └── memoria/
│   │       │       └── TarefaInMemoryAdapter.java   ← para testes!
│   │       └── notificacao/
│   │           ├── EmailNotificadorAdapter.java
│   │           └── SmsNotificadorAdapter.java
│   │
│   └── config/
│       └── HexagonalBeanConfig.java         ← Conecta tudo
│
└── pom.xml
```

---

## 4. O Hexágono — Modelo de Domínio Puro

```java
/**
 * Entidade de domínio: Tarefa.
 *
 * Esta classe é Java PURO — sem @Entity, sem @Component, sem nenhuma
 * importação de framework! Pode ser usada em CLI, Web, Mensageria...
 * sem nenhuma alteração.
 */
public class Tarefa {

    private final Long id;
    private String titulo;
    private StatusTarefa status;
    private LocalDateTime criadaEm;

    public Tarefa(String titulo) {
        // Regra de negócio: validação na criação
        if (titulo == null || titulo.isBlank()) {
            throw new TituloInvalidoException("Título da tarefa não pode ser vazio");
        }
        this.id = null;
        this.titulo = titulo;
        this.status = StatusTarefa.PENDENTE;
        this.criadaEm = LocalDateTime.now();
    }

    // Construtor para reconstituir do banco de dados
    public Tarefa(Long id, String titulo, StatusTarefa status, LocalDateTime criadaEm) {
        this.id = id;
        this.titulo = titulo;
        this.status = status;
        this.criadaEm = criadaEm;
    }

    /**
     * Regra de negócio: concluir a tarefa.
     * Esta lógica fica AQUI, no hexágono — não no controller, não no adapter!
     */
    public void concluir() {
        if (status == StatusTarefa.CONCLUIDA) {
            throw new TarefaJaConcluidaException("Tarefa já está concluída");
        }
        this.status = StatusTarefa.CONCLUIDA;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public StatusTarefa getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}

public enum StatusTarefa {
    PENDENTE, CONCLUIDA
}
```

---

## 5. Portas de Entrada — O que o Hexágono Oferece

```java
/**
 * Porta de ENTRADA: define a operação "Criar Tarefa".
 *
 * Qualquer adaptador (REST, CLI, mensageria) pode chamar esta porta
 * para criar uma tarefa — sem saber COMO é implementado por dentro!
 */
public interface CriarTarefaPort {
    Tarefa executar(String titulo);
}

/**
 * Porta de ENTRADA: define a operação "Concluir Tarefa".
 */
public interface ConcluirTarefaPort {
    Tarefa executar(Long tarefaId);
}

/**
 * Porta de ENTRADA: define a operação "Listar Tarefas".
 */
public interface ListarTarefasPort {
    List<Tarefa> executar();
}
```

---

## 6. Portas de Saída — O que o Hexágono Precisa

```java
/**
 * Porta de SAÍDA: o núcleo precisa "salvar" e "buscar" tarefas,
 * mas NÃO QUER SABER se é PostgreSQL, MongoDB, ou uma lista em memória.
 *
 * Quem implementa: TarefaJpaAdapter, TarefaMongoAdapter ou TarefaInMemoryAdapter
 */
public interface TarefaRepositoryPort {
    Tarefa salvar(Tarefa tarefa);
    Optional<Tarefa> buscarPorId(Long id);
    List<Tarefa> listarTodas();
}

/**
 * Porta de SAÍDA: o núcleo precisa "notificar" alguém quando uma tarefa é concluída,
 * mas NÃO QUER SABER se é email, SMS, push notification, etc.
 *
 * Quem implementa: EmailNotificadorAdapter ou SmsNotificadorAdapter
 */
public interface NotificadorPort {
    void notificarConclusao(Tarefa tarefa);
}
```

---

## 7. O Service — Implementação do Núcleo

```java
/**
 * TarefaService: implementa TODAS as portas de entrada.
 *
 * Observe: ele usa SOMENTE interfaces (portas de saída)!
 * Não importa de onde vêm os dados, nem para onde vão as notificações.
 * Isso é o que permite trocar os adaptadores sem mudar esta classe.
 */
@Service
public class TarefaService implements CriarTarefaPort, ConcluirTarefaPort, ListarTarefasPort {

    // Dependência por INTERFACE (porta de saída) — não por implementação concreta!
    private final TarefaRepositoryPort repository;
    private final NotificadorPort notificador;

    // Injeção via construtor — o Spring decide QUAL implementação injetar
    public TarefaService(TarefaRepositoryPort repository, NotificadorPort notificador) {
        this.repository = repository;
        this.notificador = notificador;
    }

    @Override
    public Tarefa executar(String titulo) {  // implementa CriarTarefaPort
        var tarefa = new Tarefa(titulo); // regra de validação já roda aqui (no construtor)
        return repository.salvar(tarefa);
    }

    @Override
    public Tarefa concluir(Long tarefaId) { // implementa ConcluirTarefaPort
        var tarefa = repository.buscarPorId(tarefaId)
            .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));

        tarefa.concluir(); // regra de negócio na entidade

        var salva = repository.salvar(tarefa);
        notificador.notificarConclusao(salva); // efeito colateral via porta de saída

        return salva;
    }

    @Override
    public List<Tarefa> listar() { // implementa ListarTarefasPort
        return repository.listarTodas();
    }
}
```

---

## 8. Múltiplos Adaptadores de Entrada — O Mesmo Núcleo, Várias "Portas"

```java
/**
 * Adaptador DRIVING #1: REST API.
 * Traduz requisições HTTP em chamadas às portas de entrada.
 */
@RestController
@RequestMapping("/api/tarefas")
public class TarefaRestController {

    private final CriarTarefaPort criarTarefa;
    private final ConcluirTarefaPort concluirTarefa;
    private final ListarTarefasPort listarTarefas;

    public TarefaRestController(CriarTarefaPort criarTarefa,
                                ConcluirTarefaPort concluirTarefa,
                                ListarTarefasPort listarTarefas) {
        this.criarTarefa = criarTarefa;
        this.concluirTarefa = concluirTarefa;
        this.listarTarefas = listarTarefas;
    }

    @PostMapping
    public ResponseEntity<Tarefa> criar(@RequestBody Map<String, String> body) {
        var tarefa = criarTarefa.executar(body.get("titulo"));
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefa);
    }

    @PatchMapping("/{id}/concluir")
    public Tarefa concluir(@PathVariable Long id) {
        return concluirTarefa.executar(id);
    }

    @GetMapping
    public List<Tarefa> listar() {
        return listarTarefas.executar();
    }
}
```

```java
/**
 * Adaptador DRIVING #2: linha de comando (CLI).
 *
 * IMPORTANTE: usa as MESMAS portas de entrada do REST Controller!
 * O núcleo (TarefaService) é EXATAMENTE o mesmo — só muda quem o aciona.
 */
@Component
public class TarefaCliRunner implements CommandLineRunner {

    private final CriarTarefaPort criarTarefa;
    private final ListarTarefasPort listarTarefas;

    public TarefaCliRunner(CriarTarefaPort criarTarefa, ListarTarefasPort listarTarefas) {
        this.criarTarefa = criarTarefa;
        this.listarTarefas = listarTarefas;
    }

    @Override
    public void run(String... args) throws Exception {
        // Se rodar com argumento "demo", cria tarefas de exemplo via CLI!
        if (args.length > 0 && args[0].equals("demo")) {
            criarTarefa.executar("Estudar arquitetura hexagonal");
            criarTarefa.executar("Praticar com exemplos");

            System.out.println("=== Tarefas criadas via CLI ===");
            listarTarefas.executar().forEach(t ->
                System.out.println("- " + t.getTitulo() + " [" + t.getStatus() + "]"));
        }
    }
}
```

---

## 9. Múltiplos Adaptadores de Saída — Trocando o Banco Sem Dor

```java
/**
 * Adaptador DRIVEN #1: persistência com JPA/PostgreSQL.
 */
@Component
@Profile("jpa") // ativo quando spring.profiles.active=jpa
public class TarefaJpaAdapter implements TarefaRepositoryPort {

    private final TarefaJpaRepository jpaRepository; // interface Spring Data

    public TarefaJpaAdapter(TarefaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Tarefa salvar(Tarefa tarefa) {
        var entidade = TarefaJpaEntity.de(tarefa);
        return jpaRepository.save(entidade).paraDominio();
    }

    @Override
    public Optional<Tarefa> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(TarefaJpaEntity::paraDominio);
    }

    @Override
    public List<Tarefa> listarTodas() {
        return jpaRepository.findAll().stream()
            .map(TarefaJpaEntity::paraDominio)
            .collect(Collectors.toList());
    }
}
```

```java
/**
 * Adaptador DRIVEN #2: persistência EM MEMÓRIA — perfeito para testes e demos!
 *
 * Esta implementação NÃO usa banco de dados nenhum. Só uma lista Java.
 * Troque o profile e o núcleo continua funcionando IDENTICAMENTE.
 */
@Component
@Profile("memoria") // ativo quando spring.profiles.active=memoria
public class TarefaInMemoryAdapter implements TarefaRepositoryPort {

    // Simula um "banco de dados" com um Map em memória
    private final Map<Long, Tarefa> dados = new ConcurrentHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    @Override
    public Tarefa salvar(Tarefa tarefa) {
        Long id = tarefa.getId() != null ? tarefa.getId() : proximoId.getAndIncrement();
        var salva = new Tarefa(id, tarefa.getTitulo(), tarefa.getStatus(), tarefa.getCriadaEm());
        dados.put(id, salva);
        return salva;
    }

    @Override
    public Optional<Tarefa> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<Tarefa> listarTodas() {
        return new ArrayList<>(dados.values());
    }
}
```

```properties
# application.properties — escolha o adaptador de persistência sem mudar o núcleo!
spring.profiles.active=memoria
# ou: spring.profiles.active=jpa
```

---

## 10. Checklist Antes de Gerar

- [ ] Núcleo (modelo + portas + service) sem nenhuma importação de framework?
- [ ] Portas de entrada e saída claramente separadas em pastas diferentes?
- [ ] Pelo menos 2 adaptadores de entrada (ex: REST + CLI) usando as mesmas portas?
- [ ] Pelo menos 2 adaptadores de saída (ex: JPA + InMemory) implementando a mesma porta?
- [ ] Comentários explicando "por que" o núcleo não conhece os adaptadores?
- [ ] `@Profile` para alternar entre adaptadores de saída?

---

## 11. Formato de Entrega

1. **Diagrama do hexágono** explicando entrada e saída
2. **Estrutura de pastas** comentada
3. **Núcleo**: modelo + portas + service
4. **Adaptadores de entrada**: pelo menos 2 tecnologias diferentes
5. **Adaptadores de saída**: pelo menos 2 tecnologias diferentes (uma sempre em memória!)
6. **Dica**: como adicionar um terceiro adaptador sem tocar no núcleo

Consulte os arquivos de referência:
- `references/multiplos-adaptadores.md` — GraphQL, gRPC, Mensageria como entrada
- `references/testes.md` — Testando o núcleo isoladamente com adapter in-memory
- `references/configuracao.md` — Gerenciando múltiplos adaptadores com @Profile e @Qualifier

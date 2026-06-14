# Testando a Arquitetura Hexagonal — Referência para Estudantes

## A grande vantagem: testar o núcleo SEM banco, SEM Spring, SEM nada externo!

```java
/**
 * Teste do TarefaService usando o adaptador EM MEMÓRIA.
 *
 * Não precisamos de @SpringBootTest, banco de dados ou mocks complicados!
 * Usamos a implementação real de TarefaInMemoryAdapter — é rápida e funcional.
 */
class TarefaServiceTest {

    private TarefaRepositoryPort repository;
    private NotificadorPort notificador;
    private TarefaService service;

    @BeforeEach
    void setUp() {
        // Usa o adaptador em memória — sem Spring, sem banco!
        repository = new TarefaInMemoryAdapter();
        // Notificador "espião" — só registra se foi chamado
        notificador = new NotificadorEspiao();
        service = new TarefaService(repository, notificador);
    }

    @Test
    @DisplayName("Deve criar uma tarefa com status PENDENTE")
    void deveCriarTarefaPendente() {
        var tarefa = service.executar("Estudar Java"); // CriarTarefaPort

        assertThat(tarefa.getId()).isNotNull();
        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.PENDENTE);
    }

    @Test
    @DisplayName("Deve concluir tarefa e notificar")
    void deveConcluirENotificar() {
        var criada = service.executar("Estudar hexagonal");

        var concluida = service.concluir(criada.getId());

        assertThat(concluida.getStatus()).isEqualTo(StatusTarefa.CONCLUIDA);
        // Verifica que o adaptador de notificação foi chamado!
        assertThat(((NotificadorEspiao) notificador).foiChamado()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção ao concluir tarefa inexistente")
    void deveLancarExcecaoParaTarefaInexistente() {
        assertThatThrownBy(() -> service.concluir(999L))
            .isInstanceOf(TarefaNaoEncontradaException.class);
    }
}

/**
 * "Espião" de teste: implementação fake do NotificadorPort que apenas
 * registra se foi chamado, sem enviar email/SMS de verdade.
 */
class NotificadorEspiao implements NotificadorPort {
    private boolean chamado = false;

    @Override
    public void notificarConclusao(Tarefa tarefa) {
        this.chamado = true;
    }

    public boolean foiChamado() { return chamado; }
}
```

## Testando o Adaptador REST isoladamente

```java
// @WebMvcTest sobe só a camada web — o resto é mockado
@WebMvcTest(TarefaRestController.class)
class TarefaRestControllerTest {

    @Autowired private MockMvc mockMvc;

    // Mocka as PORTAS, não o service inteiro!
    @MockBean private CriarTarefaPort criarTarefaPort;
    @MockBean private ConcluirTarefaPort concluirTarefaPort;
    @MockBean private ListarTarefasPort listarTarefasPort;

    @Test
    @DisplayName("POST /api/tarefas deve criar e retornar 201")
    void deveCriarTarefa() throws Exception {
        var tarefaCriada = new Tarefa(1L, "Nova tarefa", StatusTarefa.PENDENTE, LocalDateTime.now());
        when(criarTarefaPort.executar("Nova tarefa")).thenReturn(tarefaCriada);

        mockMvc.perform(post("/api/tarefas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\": \"Nova tarefa\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.titulo").value("Nova tarefa"));
    }
}
```

## Testando o Adaptador JPA isoladamente

```java
@DataJpaTest
class TarefaJpaAdapterTest {

    @Autowired private TarefaJpaRepository jpaRepository;
    private TarefaJpaAdapter adapter;

    @BeforeEach
    void setUp() { adapter = new TarefaJpaAdapter(jpaRepository); }

    @Test
    @DisplayName("Deve persistir e recuperar tarefa do banco")
    void devePersistirERecuperar() {
        var tarefa = new Tarefa("Tarefa de teste");

        var salva = adapter.salvar(tarefa);
        var encontrada = adapter.buscarPorId(salva.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTitulo()).isEqualTo("Tarefa de teste");
    }
}
```

# Testing Strategy — Business Reference

## Core Unit Tests (no Spring, no infrastructure)

```java
class TaskServiceTest {

    private TaskRepository repository;
    private NotificationPort notifications;
    private CachePort cache;
    private TaskService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
        notifications = mock(NotificationPort.class);
        cache = new NoOpCacheAdapter();
        service = new TaskService(repository, notifications, cache);
    }

    @Test
    void createTask_shouldPersistWithPendingStatus() {
        var task = service.execute(new CreateTaskCommand("Write tests"));

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(repository.findById(task.getId())).isPresent();
    }

    @Test
    void completeTask_shouldUpdateStatusAndNotify() {
        var created = service.execute(new CreateTaskCommand("Write tests"));

        var completed = service.execute(created.getId());

        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
        verify(notifications).notifyTaskCompleted(completed);
    }

    @Test
    void completeTask_shouldThrowWhenAlreadyCompleted() {
        var created = service.execute(new CreateTaskCommand("Write tests"));
        service.execute(created.getId());

        assertThatThrownBy(() -> service.execute(created.getId()))
            .isInstanceOf(TaskAlreadyCompletedException.class);
    }

    @Test
    void completeTask_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> service.execute(TaskId.generate()))
            .isInstanceOf(TaskNotFoundException.class);
    }
}
```

## Port Contract Tests — Run the Same Test Against Every Adapter

```java
abstract class TaskRepositoryContractTest {

    protected abstract TaskRepository repository();

    @Test
    void save_shouldAssignRetrievableId() {
        var task = Task.create("Contract test task");
        var saved = repository().save(task);

        assertThat(repository().findById(saved.getId())).isPresent();
    }

    @Test
    void findAll_shouldFilterByStatus() {
        var pending = Task.create("Pending task");
        var completed = Task.create("Completed task");
        completed.complete();

        repository().save(pending);
        repository().save(completed);

        var result = repository().findAll(new TaskFilter(TaskStatus.COMPLETED, null));

        assertThat(result).extracting(Task::getTitle).containsExactly("Completed task");
    }

    @Test
    void deleteById_shouldRemoveTask() {
        var saved = repository().save(Task.create("To be deleted"));
        repository().deleteById(saved.getId());

        assertThat(repository().findById(saved.getId())).isEmpty();
    }
}

class InMemoryTaskRepositoryTest extends TaskRepositoryContractTest {
    private final TaskRepository repository = new InMemoryTaskRepository();
    @Override protected TaskRepository repository() { return repository; }
}

@DataJpaTest
class TaskJpaAdapterTest extends TaskRepositoryContractTest {
    @Autowired private TaskJpaRepository jpaRepository;
    private TaskRepository repository;

    @BeforeEach void setUp() { repository = new TaskJpaAdapter(jpaRepository); }
    @Override protected TaskRepository repository() { return repository; }
}

@DataMongoTest
class TaskMongoAdapterTest extends TaskRepositoryContractTest {
    @Autowired private TaskMongoRepository mongoRepository;
    @Autowired private MongoTemplate mongoTemplate;
    private TaskRepository repository;

    @BeforeEach void setUp() { repository = new TaskMongoAdapter(mongoRepository, mongoTemplate); }
    @Override protected TaskRepository repository() { return repository; }
}
```

This pattern guarantees every adapter honors the same contract — swapping
implementations cannot silently break behavior the core depends on.

## ArchUnit — Enforce Core Purity

```java
@AnalyzeClasses(packages = "com.company.service")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule coreHasNoFrameworkDependencies =
        noClasses().that().resideInAPackage("..core..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.mongodb..",
                "org.apache.kafka..")
            .as("Core must remain framework-free");

    @ArchTest
    static final ArchRule adaptersDependOnCorePortsOnly =
        classes().that().resideInAPackage("..adapter..")
            .and().dependOnClassesThat().resideInAPackage("..core..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..core.port..", "..core.domain..")
            .as("Adapters must depend only on ports and domain, not service internals");

    @ArchTest
    static final ArchRule noCyclesBetweenAdapters =
        slices().matching("..adapter.(*)..")
            .should().beFreeOfCycles();
}
```

## Adapter Integration Tests — REST

```java
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CreateTaskUseCase createTask;
    @MockBean private CompleteTaskUseCase completeTask;
    @MockBean private QueryTasksUseCase queryTasks;

    @Test
    void create_shouldReturn201() throws Exception {
        var task = Task.create("New task");
        when(createTask.execute(any())).thenReturn(task);

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "New task"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New task"));
    }
}
```

## End-to-End — Swapping Adapters via Profiles

```java
@SpringBootTest
@ActiveProfiles("inmemory")
class TaskServiceInMemoryE2ETest {
    @Autowired private CreateTaskUseCase createTask;
    @Autowired private TaskRepository repository;

    @Test
    void shouldWorkEndToEndWithInMemoryAdapter() {
        var task = createTask.execute(new CreateTaskCommand("E2E task"));
        assertThat(repository.findById(task.getId())).isPresent();
    }
}

@SpringBootTest
@ActiveProfiles("jpa")
@Testcontainers
class TaskServiceJpaE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired private CreateTaskUseCase createTask;
    @Autowired private TaskRepository repository;

    @Test
    void shouldWorkEndToEndWithJpaAdapter() {
        var task = createTask.execute(new CreateTaskCommand("E2E JPA task"));
        assertThat(repository.findById(task.getId())).isPresent();
    }
}
```

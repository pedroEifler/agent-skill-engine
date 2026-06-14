---
name: hexagonal-business
description: >
  Use this skill whenever a professional developer requests production-grade Hexagonal Architecture
  (Ports & Adapters per Alistair Cockburn), pluggable infrastructure, multi-adapter systems, or any
  Java application designed for technology independence. Triggers include: "hexagonal architecture",
  "ports and adapters", "driving adapter", "driven adapter", "pluggable architecture", "swap database
  implementation", "framework-independent core", "multiple adapters same port", "in-memory adapter for
  tests", "technology agnostic domain", "adapter pattern enterprise", "polyglot persistence", "primary
  vs secondary adapter", "application core isolation". Produces production-ready English code with a
  framework-free application core, strictly typed ports, multiple interchangeable driving/driven adapters,
  and a testing strategy that exercises the core without infrastructure. ALWAYS use this skill for any
  professional Hexagonal Architecture task, even when only one adapter is initially requested — design
  for pluggability from the start.
---

# Skill: Hexagonal Architecture Business

Generates production-grade Java applications using Hexagonal Architecture (Ports & Adapters).
Framework-free application core, multiple interchangeable adapters, clean English code, no inline comments.

---

## 1. Architectural Overview

```
                    DRIVING ADAPTERS (Primary / Inbound)
        ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
        │  REST API    │  │  GraphQL     │  │  Kafka       │
        │  Controller  │  │  Resolver    │  │  Consumer    │
        └──────┬───────┘  └──────┬───────┘  └──────┬──────┘
               │                 │                 │
               ▼                 ▼                 ▼
        ┌──────────────── INPUT PORTS ───────────────────────┐
        │                                                      │
        │              ⬡  APPLICATION CORE  ⬡                 │
        │                                                      │
        │   Domain Model + Use Cases — zero framework deps    │
        │                                                      │
        └─────────────── OUTPUT PORTS ────────────────────────┘
               │                 │                 │
               ▼                 ▼                 ▼
        ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
        │ PostgreSQL   │  │ Redis Cache  │  │ In-Memory   │
        │ Adapter      │  │ Adapter      │  │ (testing)   │
        └─────────────┘  └─────────────┘  └─────────────┘
                    DRIVEN ADAPTERS (Secondary / Outbound)
```

**Core principle**: the application core depends only on interfaces (ports).
Adapters depend on the core, never the reverse. Multiple adapters can implement
the same port and be swapped via configuration without touching the core.

---

## 2. Project Structure

```
my-service/
├── src/main/java/com/company/service/
│   │
│   ├── core/                                ← ⬡ Framework-free
│   │   ├── domain/
│   │   │   ├── Task.java
│   │   │   ├── TaskId.java
│   │   │   └── TaskStatus.java
│   │   ├── port/
│   │   │   ├── in/                          ← Input ports (use cases)
│   │   │   │   ├── CreateTaskUseCase.java
│   │   │   │   ├── CompleteTaskUseCase.java
│   │   │   │   └── QueryTasksUseCase.java
│   │   │   └── out/                         ← Output ports
│   │   │       ├── TaskRepository.java
│   │   │       ├── NotificationPort.java
│   │   │       └── CachePort.java
│   │   └── service/
│   │       └── TaskService.java
│   │
│   ├── adapter/
│   │   ├── in/                              ← Driving adapters
│   │   │   ├── rest/
│   │   │   │   ├── TaskController.java
│   │   │   │   └── dto/
│   │   │   ├── graphql/
│   │   │   │   └── TaskResolver.java
│   │   │   ├── messaging/
│   │   │   │   └── TaskCommandConsumer.java
│   │   │   └── scheduler/
│   │   │       └── TaskCleanupJob.java
│   │   │
│   │   └── out/                             ← Driven adapters
│   │       ├── persistence/
│   │       │   ├── jpa/
│   │       │   │   ├── TaskJpaAdapter.java
│   │       │   │   └── TaskJpaEntity.java
│   │       │   ├── mongodb/
│   │       │   │   └── TaskMongoAdapter.java
│   │       │   └── inmemory/
│   │       │       └── InMemoryTaskRepository.java
│   │       ├── cache/
│   │       │   ├── RedisCacheAdapter.java
│   │       │   └── NoOpCacheAdapter.java
│   │       └── notification/
│   │           ├── EmailNotificationAdapter.java
│   │           ├── SmsNotificationAdapter.java
│   │           └── CompositeNotificationAdapter.java
│   │
│   └── config/
│       ├── PersistenceConfig.java
│       ├── NotificationConfig.java
│       └── CacheConfig.java
│
├── src/test/java/com/company/service/
│   ├── core/                                ← Pure unit tests, no Spring
│   ├── adapter/in/rest/                     ← @WebMvcTest
│   └── adapter/out/persistence/             ← @DataJpaTest, Testcontainers
└── pom.xml
```

---

## 3. Application Core — Domain Model

```java
public final class Task {

    private final TaskId id;
    private String title;
    private TaskStatus status;
    private final Instant createdAt;
    private Instant completedAt;

    private Task(TaskId id, String title, TaskStatus status, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.title = validateTitle(title);
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static Task create(String title) {
        return new Task(TaskId.generate(), title, TaskStatus.PENDING, Instant.now(), null);
    }

    public static Task reconstitute(TaskId id, String title, TaskStatus status,
                                     Instant createdAt, Instant completedAt) {
        return new Task(id, title, status, createdAt, completedAt);
    }

    public void complete() {
        if (status == TaskStatus.COMPLETED) {
            throw new TaskAlreadyCompletedException(id);
        }
        this.status = TaskStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void rename(String newTitle) {
        if (status == TaskStatus.COMPLETED) {
            throw new CannotModifyCompletedTaskException(id);
        }
        this.title = validateTitle(newTitle);
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskTitleException("Task title cannot be blank");
        }
        if (title.length() > 200) {
            throw new InvalidTaskTitleException("Task title cannot exceed 200 characters");
        }
        return title;
    }

    public TaskId getId() { return id; }
    public String getTitle() { return title; }
    public TaskStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}

public record TaskId(UUID value) {
    public TaskId { Objects.requireNonNull(value); }
    public static TaskId generate() { return new TaskId(UUID.randomUUID()); }
    public static TaskId of(String value) { return new TaskId(UUID.fromString(value)); }
}

public enum TaskStatus { PENDING, COMPLETED, ARCHIVED }
```

---

## 4. Input Ports (Use Cases)

```java
public interface CreateTaskUseCase {
    Task execute(CreateTaskCommand command);
}

public record CreateTaskCommand(String title) {}

public interface CompleteTaskUseCase {
    Task execute(TaskId id);
}

public interface QueryTasksUseCase {
    Optional<Task> findById(TaskId id);
    List<Task> findAll(TaskFilter filter);
}

public record TaskFilter(TaskStatus status, String searchTerm) {
    public static TaskFilter all() { return new TaskFilter(null, null); }
}
```

---

## 5. Output Ports

```java
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(TaskId id);
    List<Task> findAll(TaskFilter filter);
    void deleteById(TaskId id);
}

public interface NotificationPort {
    void notifyTaskCompleted(Task task);
}

public interface CachePort {
    Optional<Task> get(TaskId id);
    void put(Task task);
    void evict(TaskId id);
}
```

---

## 6. Core Service — Implements Input Ports

```java
@Service
public class TaskService implements CreateTaskUseCase, CompleteTaskUseCase, QueryTasksUseCase {

    private final TaskRepository repository;
    private final NotificationPort notifications;
    private final CachePort cache;

    public TaskService(TaskRepository repository, NotificationPort notifications, CachePort cache) {
        this.repository = repository;
        this.notifications = notifications;
        this.cache = cache;
    }

    @Override
    public Task execute(CreateTaskCommand command) {
        var task = Task.create(command.title());
        return repository.save(task);
    }

    @Override
    public Task execute(TaskId id) {
        var task = cache.get(id)
            .or(() -> repository.findById(id))
            .orElseThrow(() -> new TaskNotFoundException(id));

        task.complete();
        var saved = repository.save(task);
        cache.evict(id);
        notifications.notifyTaskCompleted(saved);

        return saved;
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return cache.get(id)
            .or(() -> repository.findById(id).map(task -> {
                cache.put(task);
                return task;
            }));
    }

    @Override
    public List<Task> findAll(TaskFilter filter) {
        return repository.findAll(filter);
    }
}
```

---

## 7. Driving Adapters — Multiple Entry Points

### REST Adapter
```java
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final CreateTaskUseCase createTask;
    private final CompleteTaskUseCase completeTask;
    private final QueryTasksUseCase queryTasks;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@RequestBody @Valid CreateTaskRequest request) {
        var task = createTask.execute(new CreateTaskCommand(request.title()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable String id) {
        return TaskResponse.from(completeTask.execute(TaskId.of(id)));
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) TaskStatus status,
                                    @RequestParam(required = false) String search) {
        return queryTasks.findAll(new TaskFilter(status, search))
            .stream().map(TaskResponse::from).collect(Collectors.toList());
    }
}
```

### GraphQL Adapter (same use cases, different protocol)
```java
@Controller
@RequiredArgsConstructor
public class TaskResolver {

    private final CreateTaskUseCase createTask;
    private final QueryTasksUseCase queryTasks;

    @QueryMapping
    public List<TaskResponse> tasks(@Argument TaskStatus status) {
        return queryTasks.findAll(new TaskFilter(status, null))
            .stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @MutationMapping
    public TaskResponse createTask(@Argument String title) {
        return TaskResponse.from(createTask.execute(new CreateTaskCommand(title)));
    }
}
```

### Message Consumer Adapter (same use case, async trigger)
```java
@Component
@RequiredArgsConstructor
public class TaskCommandConsumer {

    private final CreateTaskUseCase createTask;

    @KafkaListener(topics = "task.create.commands", groupId = "task-service")
    public void handle(CreateTaskCommandMessage message) {
        createTask.execute(new CreateTaskCommand(message.title()));
    }
}
```

---

## 8. Driven Adapters — Multiple Implementations of the Same Port

### JPA Adapter
```java
@Component
@Profile("jpa")
@RequiredArgsConstructor
public class TaskJpaAdapter implements TaskRepository {

    private final TaskJpaRepository jpaRepository;

    @Override
    public Task save(Task task) {
        return jpaRepository.save(TaskJpaEntity.from(task)).toDomain();
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return jpaRepository.findById(id.value()).map(TaskJpaEntity::toDomain);
    }

    @Override
    public List<Task> findAll(TaskFilter filter) {
        Specification<TaskJpaEntity> spec = TaskSpecifications.matching(filter);
        return jpaRepository.findAll(spec).stream()
            .map(TaskJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(TaskId id) {
        jpaRepository.deleteById(id.value());
    }
}
```

### In-Memory Adapter (production-grade test double, not just a stub)
```java
@Component
@Profile("inmemory")
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<TaskId, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findAll(TaskFilter filter) {
        return storage.values().stream()
            .filter(t -> filter.status() == null || t.getStatus() == filter.status())
            .filter(t -> filter.searchTerm() == null
                || t.getTitle().toLowerCase().contains(filter.searchTerm().toLowerCase()))
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(TaskId id) {
        storage.remove(id);
    }
}
```

### Cache Adapters — Redis vs No-Op
```java
@Component
@Profile("redis")
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

    private final RedisTemplate<String, Task> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);

    @Override
    public Optional<Task> get(TaskId id) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(id)));
    }

    @Override
    public void put(Task task) {
        redisTemplate.opsForValue().set(key(task.getId()), task, TTL);
    }

    @Override
    public void evict(TaskId id) {
        redisTemplate.delete(key(id));
    }

    private String key(TaskId id) { return "task:" + id.value(); }
}

@Component
@Profile("!redis")
public class NoOpCacheAdapter implements CachePort {
    @Override public Optional<Task> get(TaskId id) { return Optional.empty(); }
    @Override public void put(Task task) {}
    @Override public void evict(TaskId id) {}
}
```

### Composite Notification Adapter
```java
@Component
@Primary
public class CompositeNotificationAdapter implements NotificationPort {

    private final List<NotificationPort> delegates;

    public CompositeNotificationAdapter(List<NotificationPort> delegates) {
        this.delegates = delegates.stream()
            .filter(d -> !(d instanceof CompositeNotificationAdapter))
            .collect(Collectors.toList());
    }

    @Override
    public void notifyTaskCompleted(Task task) {
        delegates.forEach(d -> d.notifyTaskCompleted(task));
    }
}
```

---

## 9. Naming Conventions

```java
// Core domain
public final class Task {}                    // Entity/Aggregate: noun
public record TaskId(UUID value) {}            // Typed ID
public enum TaskStatus {}                      // Enum: noun + Status/Type

// Ports
public interface CreateTaskUseCase {}          // Input port: verb + UseCase
public interface TaskRepository {}             // Output port: noun + Repository/Port

// Service
public class TaskService {}                    // Core implementation: noun + Service

// Driving adapters
public class TaskController {}                 // REST: noun + Controller
public class TaskResolver {}                   // GraphQL: noun + Resolver
public class TaskCommandConsumer {}            // Messaging: noun + Consumer

// Driven adapters
public class TaskJpaAdapter {}                 // noun + TechnologyAdapter
public class InMemoryTaskRepository {}         // Technology + noun + Repository
public class RedisCacheAdapter {}              // Technology + noun + Adapter
```

---

## 10. Checklist

- [ ] Core package has zero imports from Spring, JPA, or any infrastructure library
- [ ] Every external dependency the core needs is an interface in `port/out`
- [ ] Every entry point to the core is an interface in `port/in`
- [ ] At least one in-memory adapter per output port (for fast tests)
- [ ] `@Profile` or `@Qualifier` used to select between adapter implementations
- [ ] Composite adapters considered where multiple implementations should run together
- [ ] Each driving adapter (REST/GraphQL/messaging) maps to the same use cases
- [ ] Unit tests for the core run without Spring context
- [ ] ArchUnit rule enforces core has no framework dependencies

---

## 11. Delivery Format

1. **Hexagon diagram** with labeled ports
2. **Project structure**
3. **Core**: domain model, input ports, output ports, service implementation
4. **Driving adapters**: at least two technologies (e.g. REST + messaging)
5. **Driven adapters**: at least two implementations per port (e.g. JPA + in-memory)
6. **Config**: profile-based wiring
7. **Test skeletons**: pure core tests, adapter integration tests

Load reference files:
- `references/multiple-adapters.md` — gRPC, scheduled jobs, polyglot persistence
- `references/configuration.md` — Profiles, conditional beans, composite adapters
- `references/tests.md` — Core unit tests, ArchUnit, contract tests for adapters

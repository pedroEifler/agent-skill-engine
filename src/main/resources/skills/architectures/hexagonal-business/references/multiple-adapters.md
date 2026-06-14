# Multiple Adapters — Business Reference

## gRPC Driving Adapter

```protobuf
// task.proto
syntax = "proto3";
package com.company.service.grpc;

service TaskGrpcService {
    rpc CreateTask(CreateTaskRequest) returns (TaskResponse);
    rpc CompleteTask(CompleteTaskRequest) returns (TaskResponse);
    rpc ListTasks(ListTasksRequest) returns (ListTasksResponse);
}

message CreateTaskRequest { string title = 1; }
message TaskResponse { string id = 1; string title = 2; string status = 3; }
```

```java
@GrpcService
@RequiredArgsConstructor
public class TaskGrpcAdapter extends TaskGrpcServiceGrpc.TaskGrpcServiceImplBase {

    private final CreateTaskUseCase createTask;
    private final CompleteTaskUseCase completeTask;

    @Override
    public void createTask(CreateTaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        var task = createTask.execute(new CreateTaskCommand(request.getTitle()));
        responseObserver.onNext(toProto(task));
        responseObserver.onCompleted();
    }

    private TaskResponse toProto(Task task) {
        return TaskResponse.newBuilder()
            .setId(task.getId().value().toString())
            .setTitle(task.getTitle())
            .setStatus(task.getStatus().name())
            .build();
    }
}
```

## Scheduled Job Driving Adapter

```java
@Component
@RequiredArgsConstructor
public class TaskCleanupJob {

    private final QueryTasksUseCase queryTasks;
    private final ArchiveTaskUseCase archiveTask;

    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    public void archiveCompletedTasks() {
        var completed = queryTasks.findAll(new TaskFilter(TaskStatus.COMPLETED, null));
        var cutoff = Instant.now().minus(Duration.ofDays(30));

        completed.stream()
            .filter(t -> t.getCompletedAt().isBefore(cutoff))
            .forEach(t -> archiveTask.execute(t.getId()));
    }
}
```

## Polyglot Persistence — MongoDB Adapter

```java
@Document(collection = "tasks")
public class TaskMongoDocument {
    @Id private String id;
    private String title;
    private String status;
    private Instant createdAt;
    private Instant completedAt;

    public static TaskMongoDocument from(Task task) {
        var doc = new TaskMongoDocument();
        doc.id = task.getId().value().toString();
        doc.title = task.getTitle();
        doc.status = task.getStatus().name();
        doc.createdAt = task.getCreatedAt();
        doc.completedAt = task.getCompletedAt();
        return doc;
    }

    public Task toDomain() {
        return Task.reconstitute(
            TaskId.of(id), title, TaskStatus.valueOf(status), createdAt, completedAt);
    }
}

@Component
@Profile("mongodb")
@RequiredArgsConstructor
public class TaskMongoAdapter implements TaskRepository {

    private final TaskMongoRepository mongoRepository;

    @Override
    public Task save(Task task) {
        return mongoRepository.save(TaskMongoDocument.from(task)).toDomain();
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return mongoRepository.findById(id.value().toString()).map(TaskMongoDocument::toDomain);
    }

    @Override
    public List<Task> findAll(TaskFilter filter) {
        Query query = new Query();
        if (filter.status() != null) {
            query.addCriteria(Criteria.where("status").is(filter.status().name()));
        }
        if (filter.searchTerm() != null) {
            query.addCriteria(Criteria.where("title").regex(filter.searchTerm(), "i"));
        }
        return mongoTemplate.find(query, TaskMongoDocument.class).stream()
            .map(TaskMongoDocument::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(TaskId id) {
        mongoRepository.deleteById(id.value().toString());
    }
}
```

## Choosing Adapters at Runtime — Feature Flags

```java
@Component
@RequiredArgsConstructor
public class FeatureFlagRepositoryRouter implements TaskRepository {

    private final TaskJpaAdapter jpaAdapter;
    private final TaskMongoAdapter mongoAdapter;
    private final FeatureFlagService featureFlags;

    @Override
    public Task save(Task task) {
        return activeRepository().save(task);
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return activeRepository().findById(id);
    }

    private TaskRepository activeRepository() {
        return featureFlags.isEnabled("use-mongodb-for-tasks") ? mongoAdapter : jpaAdapter;
    }

    // ... delegate remaining methods similarly
}
```

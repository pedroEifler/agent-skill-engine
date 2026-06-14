# Configuration & Adapter Wiring — Business Reference

## Profile-Based Configuration

```yaml
# application.yml
spring:
  profiles:
    active: ${ACTIVE_PROFILES:jpa,redis,email}

---
spring:
  config:
    activate:
      on-profile: test
app:
  adapters:
    repository: inmemory
    cache: noop
    notification: noop
```

## Explicit Bean Wiring (no @Component scanning ambiguity)

```java
@Configuration
public class PersistenceConfig {

    @Bean
    @Profile("jpa")
    public TaskRepository jpaTaskRepository(TaskJpaRepository jpaRepository) {
        return new TaskJpaAdapter(jpaRepository);
    }

    @Bean
    @Profile("mongodb")
    public TaskRepository mongoTaskRepository(TaskMongoRepository mongoRepository,
                                              MongoTemplate mongoTemplate) {
        return new TaskMongoAdapter(mongoRepository, mongoTemplate);
    }

    @Bean
    @Profile("inmemory")
    public TaskRepository inMemoryTaskRepository() {
        return new InMemoryTaskRepository();
    }
}

@Configuration
public class NotificationConfig {

    @Bean
    @ConditionalOnProperty(name = "app.notifications.email.enabled", havingValue = "true")
    public NotificationPort emailNotification(JavaMailSender mailSender) {
        return new EmailNotificationAdapter(mailSender);
    }

    @Bean
    @ConditionalOnProperty(name = "app.notifications.sms.enabled", havingValue = "true")
    public NotificationPort smsNotification(SmsClient smsClient) {
        return new SmsNotificationAdapter(smsClient);
    }

    @Bean
    @Primary
    public NotificationPort compositeNotification(List<NotificationPort> ports) {
        return new CompositeNotificationAdapter(ports);
    }
}
```

## Conditional Beans Based on Available Infrastructure

```java
@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CachePort redisCache(RedisTemplate<String, Task> redisTemplate) {
        return new RedisCacheAdapter(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(CachePort.class)
    public CachePort noOpCache() {
        return new NoOpCacheAdapter();
    }
}
```

## Hexagonal Module Boundaries with Java Modules (JPMS)

```java
// core/module-info.java — enforces zero infrastructure dependencies at compile time
module com.company.service.core {
    exports com.company.service.core.domain;
    exports com.company.service.core.port.in;
    exports com.company.service.core.port.out;
    exports com.company.service.core.service;
    // No requires to spring, jpa, etc.
}

// adapter.persistence.jpa/module-info.java
module com.company.service.adapter.persistence.jpa {
    requires com.company.service.core;
    requires spring.data.jpa;
    requires jakarta.persistence;
}
```

## Multi-Tenancy via Adapter Selection

```java
@Component
@RequiredArgsConstructor
public class TenantAwareTaskRepository implements TaskRepository {

    private final Map<String, TaskRepository> repositoriesByTenant;
    private final TenantContext tenantContext;

    @Override
    public Task save(Task task) {
        return resolveRepository().save(task);
    }

    private TaskRepository resolveRepository() {
        var tenantId = tenantContext.getCurrentTenant();
        return repositoriesByTenant.getOrDefault(tenantId, repositoriesByTenant.get("default"));
    }
}

@Configuration
public class MultiTenantConfig {

    @Bean
    public Map<String, TaskRepository> repositoriesByTenant(
            @Qualifier("tenantA") TaskRepository tenantA,
            @Qualifier("tenantB") TaskRepository tenantB,
            TaskRepository defaultRepository) {
        return Map.of("tenant-a", tenantA, "tenant-b", tenantB, "default", defaultRepository);
    }
}
```

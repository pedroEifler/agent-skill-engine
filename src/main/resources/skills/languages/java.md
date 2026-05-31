# Java - Skills & Best Practices

## Convenções de Código
- Utilize **camelCase** para variáveis e métodos, **PascalCase** para classes e interfaces.
- Pacotes em **lowercase** separados por domínio: `br.com.empresa.modulo.camada`.
- Constantes em **UPPER_SNAKE_CASE** com `static final`.
- Prefira `var` (Java 10+) apenas quando o tipo é óbvio no contexto local.

## Tipagem e Null Safety
- Nunca retorne `null` de métodos públicos — use `Optional<T>` para retornos que podem estar ausentes.
- Utilize `@NonNull` e `@Nullable` (Jakarta/Spring) para documentar contratos de nulidade.
- Prefira tipos primitivos (`int`, `long`, `boolean`) quando não houver necessidade de nulidade.

## Coleções e Streams
- Use a API de Streams para transformações declarativas, mas evite streams excessivamente aninhados.
- Prefira `List.of()`, `Map.of()`, `Set.of()` para coleções imutáveis.
- Utilize `Collectors.toUnmodifiableList()` ao coletar resultados de streams.

## Tratamento de Exceções
- Crie exceções de domínio específicas estendendo `RuntimeException`.
- Nunca capture `Exception` ou `Throwable` genericamente — seja específico.
- Use try-with-resources para qualquer `AutoCloseable`.
- Log a exceção original ao re-lançar: `throw new DomainException("msg", originalException)`.

## Orientação a Objetos
- Favoreça **composição sobre herança**.
- Classes devem ser `final` por padrão — abra para extensão apenas intencionalmente.
- Aplique o princípio da **responsabilidade única** (SRP) — uma classe, um motivo para mudar.
- Encapsule estado — campos devem ser `private final` sempre que possível.

## Records e DTOs (Java 16+)
- Use `record` para objetos de valor imutáveis (DTOs, Value Objects).
- Records são ideais para respostas de API e eventos de domínio.

## Concorrência
- Prefira `ExecutorService` e `CompletableFuture` sobre threads manuais.
- Use `Virtual Threads` (Java 21+) para I/O-bound tasks.
- Evite estado mutável compartilhado — quando necessário, use `ConcurrentHashMap` ou `AtomicReference`.

## Testes
- Nomeie testes com o padrão: `should_expectedBehavior_when_condition`.
- Use JUnit 5 com `@DisplayName` para legibilidade.
- Mocks com Mockito — nunca mocke o que você não possui (use fakes/stubs para dependências externas).
- Busque cobertura significativa (comportamentos, não linhas).

## Build e Dependências
- Gerencie dependências com Gradle (Kotlin DSL) ou Maven.
- Mantenha versões de dependências centralizadas (BOM ou version catalog).
- Evite dependências transitivas desnecessárias — use `implementation` ao invés de `api` quando possível.


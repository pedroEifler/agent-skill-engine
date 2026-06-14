---
name: singleton-pattern-student
description: >
  Use esta skill sempre que um estudante pedir para criar código usando o padrão Singleton, quiser
  entender como garantir que uma classe tenha apenas uma instância, ou quiser aprender sobre os
  diferentes modos de implementar o padrão. Triggers incluem: "singleton pattern", "padrão singleton",
  "uma única instância", "instância única", "como garantir uma única instância", "singleton thread safe",
  "enum singleton", "lazy singleton", "eager singleton", "design pattern singleton java",
  "singleton com spring", "por que evitar singleton", "problema do singleton", "singleton para estudar",
  "creational pattern singleton". Gera código em português com comentários explicativos sobre o "porquê"
  do padrão, as diferentes formas de implementar (Eager, Lazy, Double-Checked Locking, Enum, Spring),
  e os problemas/armadilhas de cada abordagem. SEMPRE use esta skill quando o estudante mencionar o
  padrão Singleton, mesmo que o pedido pareça simples.
---

# Skill: Design Pattern Singleton para Estudantes 🔒

Gera código Java demonstrando o padrão Singleton, explicando cada variação com comentários didáticos
em português, incluindo os problemas e armadilhas de cada abordagem.

---

## 1. O Problema que o Singleton Resolve

```java
// ❌ SEM o padrão Singleton — múltiplas instâncias do gerenciador de conexão!

// Em algum lugar do código:
var gerenciador1 = new GerenciadorConexao(); // abre um pool de conexões
gerenciador1.conectar();

// Em outro lugar do código:
var gerenciador2 = new GerenciadorConexao(); // OUTRO pool de conexões!!!
gerenciador2.conectar();

// Resultado: dois pools de conexão diferentes, desperdício de memória,
// possível inconsistência entre eles, e difícil de controlar o ciclo de vida.
```

```
SINGLETON resolve:
"Garanta que uma classe tenha UMA e somente UMA instância,
e forneça um ponto de acesso global a ela."
                                        — GoF (Gang of Four)

Casos de uso comuns:
✅ Pool de conexões com banco de dados
✅ Cache em memória compartilhado
✅ Gerenciador de configurações da aplicação
✅ Logger (registro de eventos)
✅ Gerenciador de threads (thread pool)
```

---

## 2. Implementação 1 — Eager Loading (carregamento antecipado)

```java
/**
 * Singleton com carregamento ANTECIPADO (Eager Loading).
 *
 * A instância é criada quando a CLASSE é carregada pela JVM,
 * mesmo que ninguém a use ainda.
 *
 * ✅ Simples e thread-safe (a JVM garante que a inicialização da classe é atômica)
 * ❌ Cria a instância mesmo se nunca for usada (desperdício se for pesada)
 */
public class GerenciadorConfiguracao {

    // A instância é criada NA DECLARAÇÃO — antes mesmo de qualquer chamada!
    // "static final" garante que será criada uma única vez e nunca substituída
    private static final GerenciadorConfiguracao INSTANCIA = new GerenciadorConfiguracao();

    // Construtor PRIVADO: impede que qualquer outro código faça "new GerenciadorConfiguracao()"!
    private GerenciadorConfiguracao() {
        System.out.println("GerenciadorConfiguracao criado!"); // só deve aparecer UMA VEZ
        // carrega as configurações do arquivo...
    }

    /**
     * Ponto de acesso global — a única forma de obter a instância.
     * @return a instância única e já inicializada
     */
    public static GerenciadorConfiguracao getInstancia() {
        return INSTANCIA; // sempre retorna a MESMA instância
    }

    public String buscarPropriedade(String chave) {
        return "valor_da_" + chave;
    }
}

// Uso:
var config1 = GerenciadorConfiguracao.getInstancia();
var config2 = GerenciadorConfiguracao.getInstancia();

System.out.println(config1 == config2); // true — são o MESMO objeto na memória!
System.out.println(config1.buscarPropriedade("db.url"));
```

---

## 3. Implementação 2 — Lazy Loading (carregamento sob demanda)

```java
/**
 * Singleton com carregamento TARDIO (Lazy Loading) — NÃO thread-safe!
 *
 * A instância só é criada quando alguém a pedir pela primeira vez.
 * PROBLEMA: em ambientes com múltiplas threads, pode criar mais de uma instância!
 *
 * ✅ Não cria a instância se nunca for usada (economiza memória)
 * ❌ NÃO É SEGURO em múltiplas threads — NÃO USE em produção assim!
 */
public class CacheResultados {

    // Começa como null — só vai ser criado quando alguém pedir
    private static CacheResultados instancia = null;

    private final Map<String, Object> cache = new HashMap<>();

    private CacheResultados() {
        System.out.println("Cache criado!");
    }

    public static CacheResultados getInstancia() {
        // ⚠️ PROBLEMA: se duas threads chegarem aqui ao mesmo tempo,
        // ambas veem "instancia == null" e criam instâncias diferentes!
        if (instancia == null) {
            instancia = new CacheResultados(); // ← perigoso com múltiplas threads!
        }
        return instancia;
    }

    public void guardar(String chave, Object valor) {
        cache.put(chave, valor);
    }

    public Object buscar(String chave) {
        return cache.get(chave);
    }
}
```

---

## 4. Implementação 3 — Double-Checked Locking (seguro para threads)

```java
/**
 * Singleton com Double-Checked Locking — thread-safe e eficiente.
 *
 * Verificamos DUAS VEZES se a instância é null:
 * 1ª verificação: sem lock (rápida, maioria dos casos)
 * 2ª verificação: dentro do lock (só nas primeiras chamadas concorrentes)
 *
 * ✅ Thread-safe
 * ✅ Lazy loading (cria só quando precisar)
 * ✅ Eficiente (o lock só é usado nas primeiras chamadas)
 * ⚠️ volatile é OBRIGATÓRIO para funcionar corretamente!
 */
public class PoolConexoes {

    /**
     * volatile garante que todos os threads vejam a versão mais recente desta variável.
     * Sem volatile, o Double-Checked Locking pode falhar de formas sutis!
     */
    private static volatile PoolConexoes instancia = null;

    private final int tamanhoPool;
    private final List<Object> conexoes = new ArrayList<>();

    private PoolConexoes() {
        this.tamanhoPool = 10;
        System.out.println("Pool de conexões criado com " + tamanhoPool + " conexões!");
        // inicializa as conexões...
    }

    public static PoolConexoes getInstancia() {
        // 1ª verificação: sem lock — rápida para a maioria das chamadas
        if (instancia == null) {

            // Adquire o lock — apenas se a instância ainda não foi criada
            synchronized (PoolConexoes.class) {

                // 2ª verificação: dentro do lock — evita criação dupla
                // se duas threads passaram pela 1ª verificação simultaneamente
                if (instancia == null) {
                    instancia = new PoolConexoes(); // criada UMA VEZ
                }
            }
        }
        return instancia;
    }

    public int getTamanhoPool() { return tamanhoPool; }
}
```

---

## 5. Implementação 4 — Enum Singleton (a melhor abordagem!)

```java
/**
 * Singleton com ENUM — a forma mais robusta e recomendada por Joshua Bloch
 * (autor de "Effective Java").
 *
 * ✅ Thread-safe (a JVM garante que enums são criados uma única vez)
 * ✅ Protegido contra serialização (outros métodos podem ser "quebrados" por serialização!)
 * ✅ Protegido contra Reflection (outros métodos podem ser contornados via Reflection!)
 * ✅ Mais simples de escrever
 * ❌ Não suporta herança (enum não pode herdar de outra classe)
 * ❌ Pode parecer estranho para quem não conhece o idioma
 */
public enum Logger {

    // INSTANCIA é a única instância — a JVM garante isso!
    INSTANCIA;

    // Atributos e métodos funcionam normalmente
    private final List<String> logs = new ArrayList<>();

    /**
     * Registra uma mensagem de log.
     * @param nivel o nível do log (INFO, WARN, ERROR)
     * @param mensagem a mensagem a registrar
     */
    public void log(String nivel, String mensagem) {
        var entrada = "[" + nivel + "] " + LocalDateTime.now() + " - " + mensagem;
        logs.add(entrada);
        System.out.println(entrada);
    }

    public List<String> getTodosLogs() {
        return Collections.unmodifiableList(logs);
    }
}

// Uso:
Logger.INSTANCIA.log("INFO", "Aplicação iniciada");
Logger.INSTANCIA.log("WARN", "Configuração padrão sendo usada");

// A instância é sempre a mesma:
System.out.println(Logger.INSTANCIA == Logger.INSTANCIA); // true
```

---

## 6. Implementação 5 — Holder Pattern (Lazy + Thread-safe sem volatile)

```java
/**
 * Singleton com Initialization-on-demand Holder (Bill Pugh Singleton).
 *
 * Usa uma classe interna estática para garantir inicialização lazy e thread-safe
 * sem precisar de synchronized ou volatile.
 *
 * ✅ Thread-safe (a JVM só inicializa a classe interna quando ela é acessada)
 * ✅ Lazy loading (a instância só é criada quando getInstancia() é chamado)
 * ✅ Sem overhead de sincronização após a criação
 * ✅ Sem volatile
 */
public class GerenciadorThread {

    private GerenciadorThread() {
        System.out.println("GerenciadorThread criado!");
    }

    /**
     * Classe interna estática — só é carregada pela JVM quando PRIMEIRO acessada.
     * A JVM garante que a inicialização de classes é thread-safe por padrão.
     */
    private static class Holder {
        // Esta linha só roda quando Holder.INSTANCIA é acessada pela primeira vez
        private static final GerenciadorThread INSTANCIA = new GerenciadorThread();
    }

    public static GerenciadorThread getInstancia() {
        // Ao acessar Holder.INSTANCIA, a JVM inicializa a classe Holder (se ainda não fez)
        return Holder.INSTANCIA;
    }

    public void executar(Runnable tarefa) {
        new Thread(tarefa).start();
    }
}
```

---

## 7. Singleton com Spring — A Forma Mais Comum na Prática

```java
/**
 * Com Spring Boot, o padrão Singleton é o COMPORTAMENTO PADRÃO de qualquer bean!
 * Você NÃO precisa implementar o padrão manualmente.
 *
 * Todo @Service, @Repository, @Component é singleton por padrão no Spring.
 */
@Service // ← isso JÁ faz o Spring criar apenas UMA instância desta classe!
public class ServicoDeNotificacao {

    // O Spring injeta automaticamente — sem precisar de getInstancia()!
    private final List<String> notificacoesEnviadas = new ArrayList<>();

    public void enviar(String mensagem) {
        notificacoesEnviadas.add(mensagem);
        System.out.println("Notificação enviada: " + mensagem);
    }

    public int totalEnviadas() {
        return notificacoesEnviadas.size();
    }
}

// Para "forçar" um bean com escopo de protótipo (uma instância por injeção):
// @Scope("prototype") — use quando você QUER múltiplas instâncias!
```

---

## 8. Comparação das Abordagens

```
┌──────────────────────┬──────────────┬──────────────┬───────────────────────────────┐
│ Abordagem            │ Thread-safe? │ Lazy?        │ Quando usar                   │
├──────────────────────┼──────────────┼──────────────┼───────────────────────────────┤
│ Eager Loading        │ ✅ Sim       │ ❌ Não       │ Instância leve, sempre usada  │
│ Lazy (simples)       │ ❌ Não       │ ✅ Sim       │ NUNCA em produção             │
│ Double-Check Locking │ ✅ Sim       │ ✅ Sim       │ Precisa de lazy + thread-safe │
│ Enum                 │ ✅ Sim       │ ❌ Não       │ Melhor opção na maioria!      │
│ Holder (Bill Pugh)   │ ✅ Sim       │ ✅ Sim       │ Lazy + sem sincronização      │
│ Spring @Service      │ ✅ Sim       │ Configurável │ Projetos Spring Boot          │
└──────────────────────┴──────────────┴──────────────┴───────────────────────────────┘
```

---

## 9. ⚠️ Cuidados e Anti-patterns

```java
// ❌ NÃO faça: estado mutável compartilhado sem sincronização!
public enum ContadorVisitas {
    INSTANCIA;
    private int total = 0; // ← PERIGO com múltiplas threads!

    public void incrementar() {
        total++; // não é atômico! pode perder incrementos
    }
}

// ✅ FAÇA: use tipos atômicos para contadores thread-safe
public enum ContadorVisitasSafe {
    INSTANCIA;
    private final AtomicInteger total = new AtomicInteger(0);

    public void incrementar() {
        total.incrementAndGet(); // atômico, thread-safe!
    }

    public int getTotal() { return total.get(); }
}
```

---

## 10. Checklist Antes de Gerar

- [ ] Identificar qual variação se aplica ao contexto (Eager, Enum, Holder, Spring)?
- [ ] Construtor privado em todas as variações manuais?
- [ ] `volatile` no Double-Checked Locking?
- [ ] Estado mutável protegido com `AtomicInteger`/`ConcurrentHashMap`/`synchronized`?
- [ ] Comentário explicando o problema que o Singleton resolve neste caso?
- [ ] Aviso quando a solução Spring é mais adequada?

---

## 11. Formato de Entrega

1. **O problema**: por que precisamos de uma única instância
2. **A variação indicada** e por que foi escolhida
3. **Código completo** com comentários em cada ponto crítico
4. **Exemplo de uso** mostrando que a instância é sempre a mesma
5. **Dica**: quando NÃO usar Singleton (prefira injeção de dependência!)

Consulte os arquivos de referência:
- `references/design-patterns/problemas-e-armadilhas.md` — Reflection, serialização e testes quebrados
- `references/design-patterns/singleton-vs-spring.md` — Quando usar @Service ao invés de Singleton manual
- `references/design-patterns/testes.md` — Como testar código que usa Singleton (e por que é difícil!)

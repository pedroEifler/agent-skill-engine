# Problemas e Armadilhas do Singleton — Referência para Estudantes

## Problema 1: Singleton "quebrado" por Reflection

```java
// ❌ Singleton tradicional pode ser QUEBRADO via Reflection!
public class SingletonQuebravel {
    private static final SingletonQuebravel INSTANCIA = new SingletonQuebravel();
    private SingletonQuebravel() {}
    public static SingletonQuebravel getInstancia() { return INSTANCIA; }
}

// Quebrando o Singleton via Reflection:
var construtorPrivado = SingletonQuebravel.class.getDeclaredConstructor();
construtorPrivado.setAccessible(true); // ← ignora o "private"!
var instanciaIlegal = construtorPrivado.newInstance(); // SEGUNDA instância!!!

System.out.println(SingletonQuebravel.getInstancia() == instanciaIlegal); // false 😱
```

```java
// ✅ Solução: lançar exceção no construtor se a instância já existir
public class SingletonProtegido {
    private static volatile boolean criado = false;

    private SingletonProtegido() {
        // Se alguém tentar criar via Reflection após a 1ª instância, lança exceção
        if (criado) {
            throw new IllegalStateException("Use getInstancia() — o Singleton já foi criado!");
        }
        criado = true;
    }

    private static final SingletonProtegido INSTANCIA = new SingletonProtegido();
    public static SingletonProtegido getInstancia() { return INSTANCIA; }
}

// ✅✅ Solução definitiva: use Enum — IMUNE a Reflection por design da linguagem!
public enum SingletonEnum {
    INSTANCIA; // Reflection não consegue criar uma segunda instância de enum!
}
```

## Problema 2: Singleton "quebrado" por Serialização

```java
// ❌ Singleton tradicional pode criar uma nova instância ao desserializar!
public class SingletonSerializavel implements Serializable {
    private static final SingletonSerializavel INSTANCIA = new SingletonSerializavel();
    private SingletonSerializavel() {}
    public static SingletonSerializavel getInstancia() { return INSTANCIA; }
}

// Ao salvar e carregar do disco, uma SEGUNDA instância é criada!
// (problema clássico com Serializable + Singleton)
```

```java
// ✅ Solução: implementar readResolve() para retornar a instância existente
public class SingletonSerializavelCorreto implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final SingletonSerializavelCorreto INSTANCIA = new SingletonSerializavelCorreto();

    private SingletonSerializavelCorreto() {}
    public static SingletonSerializavelCorreto getInstancia() { return INSTANCIA; }

    /**
     * Chamado automaticamente pela JVM durante a desserialização.
     * Em vez de criar um novo objeto, retorna a instância existente!
     */
    private Object readResolve() {
        return INSTANCIA; // garante que sempre é o mesmo objeto
    }
}

// ✅✅ Com Enum, este problema não existe — Enum já protege automaticamente!
```

## Problema 3: Singleton dificulta testes!

```java
// ❌ Código que usa Singleton diretamente é DIFÍCIL de testar!
public class ServicoPedido {
    public void processar(Pedido pedido) {
        // Chama o Singleton diretamente — não há como trocar por um fake nos testes!
        Logger.INSTANCIA.log("INFO", "Processando pedido: " + pedido.getId());
        // ... lógica de negócio
    }
}

// No teste, o log REAL sempre é chamado — não há como desativar ou verificar!
```

```java
// ✅ Solução: injetar a dependência, mesmo que seja um Singleton
public class ServicoPedidoTestavel {
    private final Logger logger; // dependência injetada

    // Recebe o logger via construtor — em produção, passa Logger.INSTANCIA
    // Em testes, passa um logger falso (mock)!
    public ServicoPedidoTestavel(Logger logger) {
        this.logger = logger;
    }

    public void processar(Pedido pedido) {
        logger.log("INFO", "Processando pedido: " + pedido.getId());
    }
}

// No teste:
var loggerFake = mock(Logger.class);
var servico = new ServicoPedidoTestavel(loggerFake);
servico.processar(new Pedido(1L));
verify(loggerFake).log("INFO", "Processando pedido: 1"); // verificável!
```

## Problema 4: Estado global compartilhado

```java
// ❌ Cuidado! Singletons são como variáveis globais — qualquer teste pode "sujar" o estado!
public enum Carrinho {
    INSTANCIA;
    private List<Item> itens = new ArrayList<>();
    public void adicionar(Item item) { itens.add(item); }
    public List<Item> getItens() { return itens; }
}

// Teste A adiciona um item... Teste B também vê esse item! Os testes se interferem.
// SEMPRE limpe o estado entre testes quando usar Singletons mutáveis!
```

```java
// ✅ Solução: forneça um método de reset para testes (package-private ou @VisibleForTesting)
public enum CarrinhoTestavel {
    INSTANCIA;
    private final List<Item> itens = new ArrayList<>();

    public void adicionar(Item item) { itens.add(item); }
    public List<Item> getItens() { return Collections.unmodifiableList(itens); }

    // Só usado em testes! Em produção, nunca deve ser chamado.
    void limparParaTeste() { itens.clear(); }
}

// No teste:
@AfterEach
void limparCarrinho() {
    CarrinhoTestavel.INSTANCIA.limparParaTeste(); // sempre limpa entre testes
}
```

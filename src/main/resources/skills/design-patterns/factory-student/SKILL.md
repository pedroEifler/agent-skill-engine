---
name: factory-pattern-student
description: >
  Use esta skill sempre que um estudante pedir para criar código usando o padrão Factory (Factory Method
  ou Abstract Factory), ou quiser entender como criar objetos sem usar "new" diretamente. Triggers incluem:
  "factory pattern", "padrão factory", "factory method", "abstract factory", "como criar objetos sem new",
  "padrão de criação", "design pattern factory", "fábrica de objetos", "static factory method",
  "como desacoplar a criação de objetos", "factory para estudar", "creational design pattern java",
  "exemplo de factory pattern". Gera código em português com comentários explicativos sobre o "porquê"
  do padrão, comparando código SEM o padrão (problema) e COM o padrão (solução), e mostrando os diferentes
  tipos de Factory (Simple Factory, Factory Method, Abstract Factory). SEMPRE use esta skill quando o
  estudante mencionar o padrão Factory, mesmo que o pedido pareça simples.
---

# Skill: Design Pattern Factory para Estudantes 🏭

Gera código Java demonstrando o padrão Factory, explicando o "porquê" com comparações entre código
SEM e COM o padrão, e comentários didáticos em português.

---

## 1. O Problema que o Factory Resolve

Sempre comece mostrando o problema:

```java
// ❌ SEM o padrão Factory — código "engessado" e difícil de estender

public class ProcessadorPagamento {

    public void processar(String tipoPagamento, double valor) {
        // Lógica de decisão espalhada pelo código — viola o Open/Closed Principle!
        if (tipoPagamento.equals("CARTAO")) {
            var pagamento = new PagamentoCartao();
            pagamento.processar(valor);
        } else if (tipoPagamento.equals("PIX")) {
            var pagamento = new PagamentoPix();
            pagamento.processar(valor);
        } else if (tipoPagamento.equals("BOLETO")) {
            var pagamento = new PagamentoBoleto();
            pagamento.processar(valor);
        }
        // Toda vez que surge um novo tipo de pagamento,
        // preciso EDITAR esta classe e adicionar mais um "else if"!
        // Isso é sinal de que a criação dos objetos precisa ser isolada.
    }
}
```

```
Problemas deste código:
1. Toda vez que surge um novo tipo, preciso modificar esta classe (viola Open/Closed)
2. A lógica de "qual objeto criar" está misturada com a lógica de negócio
3. Difícil de testar — não há como "trocar" a criação por um mock facilmente
4. Se a criação de PagamentoCartao() precisar de configuração extra, fica tudo aqui
```

---

## 2. O que é o Padrão Factory?

```
FACTORY PATTERN: centraliza a CRIAÇÃO de objetos em um lugar só.

Em vez de "new X()" espalhado pelo código, você pede para uma FÁBRICA
criar o objeto certo — a fábrica decide QUAL implementação retornar.

┌─────────────┐    "me dê um         ┌──────────────┐    decide e cria   ┌─────────────────┐
│   Cliente   │    pagamento PIX"    │   Factory    │ ─────────────────> │ PagamentoPix     │
│  (código)   │ ───────────────────> │ (a fábrica)  │                    │ (implementação) │
└─────────────┘                      └──────────────┘                    └─────────────────┘
```

---

## 3. Simple Factory — A Forma Mais Básica

```java
/**
 * Interface comum para todos os tipos de pagamento.
 * Esta é a "linguagem comum" que todas as implementações falam.
 */
public interface Pagamento {
    void processar(double valor);
}

// Implementações concretas — cada uma sabe processar seu próprio tipo
public class PagamentoCartao implements Pagamento {
    @Override
    public void processar(double valor) {
        System.out.println("Processando R$ " + valor + " via Cartão de Crédito");
    }
}

public class PagamentoPix implements Pagamento {
    @Override
    public void processar(double valor) {
        System.out.println("Processando R$ " + valor + " via PIX");
    }
}

public class PagamentoBoleto implements Pagamento {
    @Override
    public void processar(double valor) {
        System.out.println("Processando R$ " + valor + " via Boleto");
    }
}
```

```java
/**
 * SIMPLE FACTORY: uma classe com um método estático que CRIA o objeto certo.
 *
 * Não é um "padrão GoF oficial", mas é o ponto de partida mais simples
 * e muito usado na prática.
 */
public class PagamentoFactory {

    /**
     * Cria a implementação correta de Pagamento baseado no tipo informado.
     *
     * @param tipo o tipo de pagamento desejado (CARTAO, PIX, BOLETO)
     * @return a implementação correspondente de Pagamento
     * @throws IllegalArgumentException se o tipo não for reconhecido
     */
    public static Pagamento criar(TipoPagamento tipo) {
        // A decisão de QUAL classe instanciar fica isolada AQUI,
        // não espalhada pelo código que USA o pagamento!
        return switch (tipo) {
            case CARTAO -> new PagamentoCartao();
            case PIX -> new PagamentoPix();
            case BOLETO -> new PagamentoBoleto();
        };
    }
}

// Enum para os tipos — mais seguro que Strings (evita erros de digitação!)
public enum TipoPagamento {
    CARTAO, PIX, BOLETO
}
```

```java
// ✅ COM o padrão Factory — código limpo e desacoplado!
public class ProcessadorPagamento {

    public void processar(TipoPagamento tipo, double valor) {
        // Não sabemos (nem precisamos saber!) qual classe concreta será usada.
        // A factory decide isso para nós.
        Pagamento pagamento = PagamentoFactory.criar(tipo);
        pagamento.processar(valor);
    }
}

// Uso:
var processador = new ProcessadorPagamento();
processador.processar(TipoPagamento.PIX, 150.00);
// Saída: "Processando R$ 150.0 via PIX"
```

---

## 4. Factory Method — Cada Subclasse Decide o Que Criar

```
FACTORY METHOD: define um MÉTODO ABSTRATO de criação na classe base.
Cada SUBCLASSE decide qual objeto concreto será criado.

Diferença da Simple Factory:
- Simple Factory: UM lugar decide tudo com if/switch
- Factory Method: cada SUBCLASSE tem seu próprio método de criação
```

```java
/**
 * Classe abstrata que define o "esqueleto" do processo de notificação.
 * O método criarNotificador() é abstrato — cada subclasse decide qual criar.
 */
public abstract class ServicoNotificacao {

    /**
     * Template Method: define o FLUXO geral, mas delega a CRIAÇÃO
     * do notificador específico para as subclasses.
     */
    public void enviar(String mensagem, String destinatario) {
        // 1. Pede para a subclasse criar o notificador certo
        Notificador notificador = criarNotificador();

        // 2. Usa o notificador (não importa qual implementação é!)
        notificador.notificar(destinatario, mensagem);

        System.out.println("Notificação enviada via " + notificador.getClass().getSimpleName());
    }

    /**
     * FACTORY METHOD: método abstrato que cada subclasse implementa
     * para criar o tipo de notificador apropriado.
     */
    protected abstract Notificador criarNotificador();
}

// Interface comum dos notificadores
public interface Notificador {
    void notificar(String destinatario, String mensagem);
}

public class NotificadorEmail implements Notificador {
    @Override
    public void notificar(String destinatario, String mensagem) {
        System.out.println("📧 Email para " + destinatario + ": " + mensagem);
    }
}

public class NotificadorSms implements Notificador {
    @Override
    public void notificar(String destinatario, String mensagem) {
        System.out.println("📱 SMS para " + destinatario + ": " + mensagem);
    }
}
```

```java
/**
 * Cada subclasse de ServicoNotificacao implementa o Factory Method
 * para criar SEU tipo específico de notificador.
 */
public class ServicoNotificacaoEmail extends ServicoNotificacao {
    @Override
    protected Notificador criarNotificador() {
        return new NotificadorEmail();
    }
}

public class ServicoNotificacaoSms extends ServicoNotificacao {
    @Override
    protected Notificador criarNotificador() {
        return new NotificadorSms();
    }
}

// Uso:
ServicoNotificacao servico = new ServicoNotificacaoEmail();
servico.enviar("Sua tarefa foi concluída!", "usuario@exemplo.com");
// Saída:
// 📧 Email para usuario@exemplo.com: Sua tarefa foi concluída!
// Notificação enviada via NotificadorEmail
```

---

## 5. Abstract Factory — Famílias de Objetos Relacionados

```
ABSTRACT FACTORY: cria FAMÍLIAS de objetos relacionados que devem
"combinar" entre si — sem especificar as classes concretas.

Exemplo: um sistema de UI que pode ter tema CLARO ou ESCURO.
Botões, campos de texto e janelas devem combinar com o tema escolhido!

┌────────────────────┐         ┌────────────────────┐
│  Fábrica Tema Claro │         │ Fábrica Tema Escuro │
│  ┌───────────────┐  │         │  ┌───────────────┐  │
│  │ Botão Claro   │  │         │  │ Botão Escuro  │  │
│  │ Campo Claro   │  │         │  │ Campo Escuro  │  │
│  │ Janela Clara  │  │         │  │ Janela Escura │  │
│  └───────────────┘  │         │  └───────────────┘  │
└────────────────────┘         └────────────────────┘
```

```java
// Interfaces dos produtos da família
public interface Botao {
    void renderizar();
}

public interface CampoTexto {
    void renderizar();
}

// Família CLARA
public class BotaoClaro implements Botao {
    @Override
    public void renderizar() { System.out.println("🔲 Renderizando botão CLARO"); }
}

public class CampoTextoClaro implements CampoTexto {
    @Override
    public void renderizar() { System.out.println("📝 Renderizando campo de texto CLARO"); }
}

// Família ESCURA
public class BotaoEscuro implements Botao {
    @Override
    public void renderizar() { System.out.println("⬛ Renderizando botão ESCURO"); }
}

public class CampoTextoEscuro implements CampoTexto {
    @Override
    public void renderizar() { System.out.println("📝 Renderizando campo de texto ESCURO"); }
}
```

```java
/**
 * ABSTRACT FACTORY: interface que declara métodos para criar
 * CADA produto da família (botão, campo de texto, etc.)
 */
public interface FabricaUI {
    Botao criarBotao();
    CampoTexto criarCampoTexto();
}

/**
 * Fábrica concreta: cria TODA a família de componentes do tema CLARO.
 * Garante que botão, campo, janela... tudo combine!
 */
public class FabricaUIClara implements FabricaUI {
    @Override
    public Botao criarBotao() { return new BotaoClaro(); }

    @Override
    public CampoTexto criarCampoTexto() { return new CampoTextoClaro(); }
}

/**
 * Fábrica concreta: cria TODA a família de componentes do tema ESCURO.
 */
public class FabricaUIEscura implements FabricaUI {
    @Override
    public Botao criarBotao() { return new BotaoEscuro(); }

    @Override
    public CampoTexto criarCampoTexto() { return new CampoTextoEscuro(); }
}
```

```java
/**
 * O código cliente trabalha SOMENTE com a interface FabricaUI.
 * Ele não sabe (e não precisa saber) se está usando tema claro ou escuro!
 */
public class Aplicacao {

    private final Botao botao;
    private final CampoTexto campoTexto;

    // Recebe a fábrica via construtor — não decide qual usar aqui!
    public Aplicacao(FabricaUI fabrica) {
        this.botao = fabrica.criarBotao();
        this.campoTexto = fabrica.criarCampoTexto();
    }

    public void renderizar() {
        botao.renderizar();
        campoTexto.renderizar();
    }
}

// Uso:
var temaEscolhido = "ESCURO"; // poderia vir de uma configuração do usuário

FabricaUI fabrica = temaEscolhido.equals("ESCURO")
    ? new FabricaUIEscura()
    : new FabricaUIClara();

var app = new Aplicacao(fabrica);
app.renderizar();
// Saída (tema escuro):
// ⬛ Renderizando botão ESCURO
// 📝 Renderizando campo de texto ESCURO
```

---

## 6. Comparação dos Três Tipos

```
┌─────────────────┬──────────────────────────────────────────────────┐
│ Simple Factory   │ Um método (geralmente estático) com if/switch     │
│                  │ que decide qual classe instanciar.                │
│                  │ Uso: poucos tipos, lógica de criação simples.     │
├─────────────────┼──────────────────────────────────────────────────┤
│ Factory Method   │ Método abstrato em uma classe base; cada          │
│                  │ subclasse decide o que criar.                     │
│                  │ Uso: framework onde subclasses customizam         │
│                  │ uma etapa do processo.                            │
├─────────────────┼──────────────────────────────────────────────────┤
│ Abstract Factory │ Fábrica que cria FAMÍLIAS de objetos relacionados │
│                  │ que devem combinar entre si.                      │
│                  │ Uso: múltiplas variantes (temas, plataformas,     │
│                  │ bancos de dados) que precisam de objetos          │
│                  │ consistentes entre si.                            │
└─────────────────┴──────────────────────────────────────────────────┘
```

---

## 7. Checklist Antes de Gerar

- [ ] Mostrar o problema SEM o padrão primeiro (if/else espalhado)?
- [ ] Identificar qual tipo de Factory melhor se aplica ao caso?
- [ ] Usar enum em vez de String para os tipos (mais seguro)?
- [ ] Interface comum para os produtos criados?
- [ ] Comentários explicando "quem decide o que criar" em cada variação?
- [ ] Exemplo de uso (`main` ou teste) mostrando o resultado?

---

## 8. Formato de Entrega

1. **O problema**: código sem o padrão, explicando as dores
2. **A solução**: qual tipo de Factory resolve melhor e por quê
3. **Código completo**: interfaces, implementações, factory, exemplo de uso
4. **Comparação**: antes vs depois
5. **Dica**: quando NÃO usar Factory (over-engineering em casos simples)

Consulte os arquivos de referência:
- `references/factory-com-spring.md` — Factory integrado com Spring (Strategy + Factory)
- `references/static-factory-methods.md` — Métodos de fábrica estáticos (Effective Java)
- `references/testes.md` — Testando código que usa Factory

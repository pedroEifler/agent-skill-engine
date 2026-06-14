---
name: java-estudante
description: >
  Use esta skill sempre que um estudante de Java pedir para criar um projeto, classe, estrutura de pacotes,
  ou qualquer código Java com boas práticas de aprendizado. Triggers incluem: "crie um projeto Java",
  "como estruturar meu projeto Java", "projeto Java para estudar", "template Java", "exemplo Java com boas práticas",
  "código Java com comentários", qualquer pedido de projeto Java em português. Esta skill gera código em português,
  com comentários explicativos nos pontos principais, JavaDoc, nomenclatura correta, tratamento de exceções e
  padrões adequados para a versão do Java informada (Java 8 até a versão estável mais recente).
  SEMPRE use esta skill quando o contexto for estudante + Java, mesmo que o pedido seja simples.
---

# Skill: Java para Estudantes 🎓

Gera projetos e código Java com boas práticas de aprendizado, comentários explicativos em português,
JavaDoc, e padrões corretos para a versão Java informada (8+).

---

## 1. Verificação da Versão do Java

**SEMPRE** comece verificando ou perguntando a versão do Java do projeto.

### Como identificar a versão:

Se o usuário não informar, pergunte: _"Qual versão do Java você está usando? (ex: 8, 11, 17, 21)"_

Se houver um `pom.xml` ou `build.gradle`, extraia a versão de lá:

```xml
<!-- Maven - pom.xml -->
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

```groovy
// Gradle - build.gradle
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

### Tabela de versões estáveis (LTS em destaque):

| Versão | Tipo | Principais recursos disponíveis |
|--------|------|----------------------------------|
| **8**  | LTS  | Lambdas, Streams, Optional, interface default |
| **11** | LTS  | `var` (local), HttpClient, String methods novos |
| **17** | LTS  | Records, Sealed classes, Pattern Matching instanceof |
| **21** | LTS  | Virtual Threads, Sequenced Collections, Pattern Matching switch |
| 22, 23, 24 | Non-LTS | Preview features, refinamentos |

> ⚠️ Nunca use recursos de versões superiores à versão informada pelo estudante.

---

## 2. Estrutura de Projeto Padrão

```
meu-projeto/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/projeto/
│   │   │       ├── Main.java               ← Ponto de entrada
│   │   │       ├── model/                  ← Entidades/POJOs
│   │   │       ├── service/                ← Regras de negócio
│   │   │       ├── repository/             ← Acesso a dados
│   │   │       ├── controller/             ← Controladores (se web)
│   │   │       ├── exception/              ← Exceções customizadas
│   │   │       └── util/                   ← Classes utilitárias
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── br/com/projeto/
├── pom.xml (Maven) ou build.gradle (Gradle)
└── README.md
```

---

## 3. Convenções de Nomenclatura (PascalCase e camelCase)

Explique sempre as convenções ao estudante com exemplos:

```java
// ✅ CORRETO

// Classes e Interfaces → PascalCase
public class ContaBancaria { }
public interface ServicoDeEmail { }
public enum StatusPedido { ATIVO, INATIVO, CANCELADO }

// Métodos e variáveis → camelCase
String nomeDoCliente;
int quantidadeDeItens;
public void calcularDesconto() { }

// Constantes → SCREAMING_SNAKE_CASE
public static final double TAXA_JUROS = 0.05;
public static final int LIMITE_MAXIMO = 1000;

// Pacotes → tudo minúsculo, sem underline
package br.com.meuprojeto.service;

// ❌ ERRADO - exemplos do que evitar:
// public class conta_bancaria { }  ← underline em classe
// String NomeDoCliente;            ← começa com maiúsculo
// public void Calcular() { }       ← método começa com maiúsculo
```

---

## 4. JavaDoc — Documentação do Código

Sempre gere JavaDoc para classes e métodos públicos. Explique o que cada tag faz:

```java
/**
 * Representa uma conta bancária com operações básicas de depósito e saque.
 *
 * <p>Esta classe é responsável por manter o saldo atualizado e registrar
 * todas as transações realizadas pelo titular.</p>
 *
 * @author NomeDoEstudante
 * @version 1.0
 * @since Java 11
 */
public class ContaBancaria {

    /**
     * Realiza um depósito na conta.
     *
     * @param valor O valor a ser depositado. Deve ser maior que zero.
     * @throws IllegalArgumentException Se o valor for negativo ou zero.
     */
    public void depositar(double valor) {
        // Valida que o valor é positivo antes de depositar
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo. Recebido: " + valor);
        }
        this.saldo += valor;
    }
}
```

---

## 5. Tratamento de Exceções

Ensine o padrão correto com comentários explicativos:

```java
// ✅ Exceção customizada para erros de negócio
/**
 * Exceção lançada quando uma operação bancária inválida é tentada.
 */
public class OperacaoBancariaException extends RuntimeException {

    // Construtor com apenas mensagem
    public OperacaaoBancariaException(String mensagem) {
        super(mensagem);
    }

    // Construtor com mensagem e causa original (para encadear exceções)
    public OperacaoBancariaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

// ✅ Como usar try-catch corretamente
public void realizarSaque(double valor) {
    try {
        // Tenta realizar o saque
        conta.sacar(valor);

    } catch (SaldoInsuficienteException e) {
        // Captura especificamente saldo insuficiente
        System.err.println("Saldo insuficiente: " + e.getMessage());

    } catch (IllegalArgumentException e) {
        // Captura argumentos inválidos (valor negativo, etc.)
        System.err.println("Valor inválido: " + e.getMessage());

    } finally {
        // Este bloco SEMPRE executa, com ou sem exceção
        // Bom para fechar recursos, registrar logs, etc.
        registrarTentativaDeSaque();
    }
}
```

---

## 6. Templates por Versão Java

Ao gerar código, adapte os recursos conforme a versão. Leia o arquivo de referência correto:

- Java 8  → `references/languages/java8.md`
- Java 11 → `references/languages/java11.md`
- Java 17 → `references/languages/java17.md`
- Java 21 → `references/languages/java21.md`

---

## 7. Checklist Antes de Gerar o Código

Antes de gerar qualquer código para o estudante, confirme:

- [ ] Versão do Java identificada?
- [ ] Estrutura de pacotes definida?
- [ ] Nomes em PascalCase/camelCase corretos?
- [ ] JavaDoc nas classes e métodos públicos?
- [ ] Exceções customizadas quando necessário?
- [ ] Comentários nos pontos principais do código?
- [ ] `pom.xml` ou `build.gradle` com a versão correta?

---

## 8. Formato de Entrega

Sempre entregue nesta ordem:
1. **Explicação breve** do que será criado e por quê (aprendizado)
2. **Estrutura de pastas** (se projeto completo)
3. **Arquivo de build** (`pom.xml` ou `build.gradle`) com versão correta
4. **Código Java** com comentários em português
5. **Dica de aprendizado** ao final: o que estudar a seguir

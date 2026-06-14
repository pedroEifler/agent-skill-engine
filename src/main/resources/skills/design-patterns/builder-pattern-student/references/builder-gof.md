# Builder GoF Original — Referência para Estudantes

## A Versão "Clássica" do Design Patterns (GoF)

O Builder do livro original "Design Patterns" (GoF) é diferente do Builder fluente que usamos hoje.
Ele separa a construção em um DIRETOR que coordena os passos do Builder.

```
Director (coordena)         Builder (interface dos passos)
┌──────────────────┐        ┌────────────────────────────┐
│  construirRelat  │ ──────> │  adicionarCabecalho()      │
│  orio(builder)   │        │  adicionarConteudo()        │
│                  │        │  adicionarRodape()          │
└──────────────────┘        └────────────────────────────┘
                                        ▲               ▲
                            ┌───────────┘               └───────────┐
                    RelatorioHtmlBuilder              RelatorioTxtBuilder
                    (saída em HTML)                  (saída em TXT)
```

```java
// 1. Interface do Builder — define os passos da construção
public interface RelatorioBuilder {
    RelatorioBuilder adicionarCabecalho(String titulo);
    RelatorioBuilder adicionarSecao(String nome, String conteudo);
    RelatorioBuilder adicionarRodape(String texto);
    String construir(); // retorna o resultado final
}
```

```java
// 2. Builder concreto #1: gera HTML
public class RelatorioHtmlBuilder implements RelatorioBuilder {

    private final StringBuilder html = new StringBuilder();

    @Override
    public RelatorioBuilder adicionarCabecalho(String titulo) {
        html.append("<html><head><title>").append(titulo).append("</title></head><body>");
        html.append("<h1>").append(titulo).append("</h1>");
        return this;
    }

    @Override
    public RelatorioBuilder adicionarSecao(String nome, String conteudo) {
        html.append("<h2>").append(nome).append("</h2>");
        html.append("<p>").append(conteudo).append("</p>");
        return this;
    }

    @Override
    public RelatorioBuilder adicionarRodape(String texto) {
        html.append("<footer>").append(texto).append("</footer></body></html>");
        return this;
    }

    @Override
    public String construir() { return html.toString(); }
}

// 3. Builder concreto #2: gera texto puro
public class RelatorioTxtBuilder implements RelatorioBuilder {

    private final StringBuilder txt = new StringBuilder();

    @Override
    public RelatorioBuilder adicionarCabecalho(String titulo) {
        txt.append("=".repeat(50)).append("\n");
        txt.append(titulo.toUpperCase()).append("\n");
        txt.append("=".repeat(50)).append("\n");
        return this;
    }

    @Override
    public RelatorioBuilder adicionarSecao(String nome, String conteudo) {
        txt.append("\n## ").append(nome).append("\n");
        txt.append(conteudo).append("\n");
        return this;
    }

    @Override
    public RelatorioBuilder adicionarRodape(String texto) {
        txt.append("\n").append("-".repeat(50)).append("\n");
        txt.append(texto).append("\n");
        return this;
    }

    @Override
    public String construir() { return txt.toString(); }
}
```

```java
/**
 * DIRECTOR: sabe a ORDEM dos passos, mas não sabe COMO cada passo é feito.
 * Recebe qualquer Builder que implemente RelatorioBuilder e o coordena.
 */
public class RelatorioDirector {

    /**
     * Constrói um relatório mensal usando o builder fornecido.
     * O mesmo método pode gerar HTML OU TXT — depende do builder passado!
     */
    public String construirRelatorioMensal(RelatorioBuilder builder, String mes) {
        return builder
            .adicionarCabecalho("Relatório Mensal — " + mes)
            .adicionarSecao("Resumo", "Total de vendas: R$ 15.000,00")
            .adicionarSecao("Detalhes", "Produtos mais vendidos: Notebook, Teclado")
            .adicionarRodape("Gerado em " + LocalDate.now())
            .construir();
    }
}

// Uso: o mesmo Director, diferentes Builders = diferentes formatos!
var director = new RelatorioDirector();

var html = director.construirRelatorioMensal(new RelatorioHtmlBuilder(), "Junho/2024");
System.out.println("=== HTML ===\n" + html);

var txt = director.construirRelatorioMensal(new RelatorioTxtBuilder(), "Junho/2024");
System.out.println("=== TXT ===\n" + txt);
```

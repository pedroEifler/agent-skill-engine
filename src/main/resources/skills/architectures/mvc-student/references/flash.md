# Mensagens Flash — Referência para Estudantes

## O que são mensagens flash?

Mensagens flash são mensagens temporárias que aparecem UMA vez após um redirecionamento.
Exemplo: "Produto salvo com sucesso!" que aparece na lista após salvar o formulário.

```
POST /produtos/salvar → salva → redirect:/produtos → GET /produtos → exibe mensagem
                                                                        ↑
                                                              a mensagem viaja aqui!
```

## No Controller — enviando mensagens flash

```java
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@PostMapping
public String salvar(@ModelAttribute @Valid Produto produto,
                     BindingResult bindingResult,
                     RedirectAttributes redirectAttributes) { // ← injeta o RedirectAttributes

    if (bindingResult.hasErrors()) {
        return "produto/formulario";
    }

    produtoService.salvar(produto);

    // addFlashAttribute → a mensagem vai junto no redirecionamento e some depois
    redirectAttributes.addFlashAttribute("mensagemSucesso",
        "Produto '" + produto.getNome() + "' salvo com sucesso!");

    return "redirect:/produtos";
}

@PostMapping("/{id}/deletar")
public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    var produto = produtoService.buscarPorId(id);
    produtoService.deletar(id);

    redirectAttributes.addFlashAttribute("mensagemSucesso",
        "Produto '" + produto.getNome() + "' removido.");

    return "redirect:/produtos";
}
```

## Na View — exibindo mensagens flash

```html
<!-- templates/produto/lista.html -->
<!-- Coloque isto no topo do conteúdo, antes da tabela -->

<!-- Mensagem de sucesso (verde) -->
<div th:if="${mensagemSucesso}"
     class="alert alert-success alert-dismissible fade show" role="alert">
    <!-- th:text insere a mensagem vinda do controller -->
    <span th:text="${mensagemSucesso}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>

<!-- Mensagem de erro (vermelha) -->
<div th:if="${mensagemErro}"
     class="alert alert-danger alert-dismissible fade show" role="alert">
    <span th:text="${mensagemErro}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>

<!-- Mensagem de aviso (amarela) -->
<div th:if="${mensagemAviso}"
     class="alert alert-warning" role="alert">
    <span th:text="${mensagemAviso}"></span>
</div>
```

## Usando um fragmento reutilizável para mensagens

```html
<!-- templates/layout/fragmentos.html — adicione este fragmento -->
<div th:fragment="mensagens">
    <div th:if="${mensagemSucesso}" class="alert alert-success" th:text="${mensagemSucesso}"></div>
    <div th:if="${mensagemErro}"    class="alert alert-danger"  th:text="${mensagemErro}"></div>
    <div th:if="${mensagemAviso}"   class="alert alert-warning" th:text="${mensagemAviso}"></div>
</div>
```

```html
<!-- Em qualquer página, use: -->
<div th:replace="~{layout/fragmentos :: mensagens}"></div>
```

# Thymeleaf — Referência para Estudantes

## Expressões mais usadas

```html
<!-- th:text → insere texto (escapa HTML automaticamente, seguro!) -->
<p th:text="${produto.nome}">nome aqui</p>

<!-- th:utext → insere HTML sem escapar (cuidado com XSS!) -->
<p th:utext="${produto.descricaoHtml}">html aqui</p>

<!-- th:href → gera URLs dinamicamente -->
<a th:href="@{/produtos}">Lista</a>
<a th:href="@{/produtos/{id}(id=${produto.id})}">Ver produto</a>

<!-- th:src → para imagens -->
<img th:src="@{/static/logo.png}">

<!-- th:if e th:unless → condicionais -->
<p th:if="${produto.emEstoque}">Disponível!</p>
<p th:unless="${produto.emEstoque}">Fora de estoque</p>

<!-- th:each → laço de repetição -->
<tr th:each="item, status : ${lista}">
    <!-- status.index → índice (0, 1, 2...)  -->
    <!-- status.count → contagem (1, 2, 3...) -->
    <!-- status.first / status.last → primeiro ou último da lista -->
    <td th:text="${status.count}"></td>
    <td th:text="${item.nome}"></td>
</tr>

<!-- th:class e th:classappend → classes CSS dinâmicas -->
<tr th:classappend="${status.even} ? 'table-light'">...</tr>
<input th:classappend="${#fields.hasErrors('nome')} ? 'is-invalid'">

<!-- th:switch / th:case → switch/case no HTML -->
<span th:switch="${pedido.status}">
    <span th:case="'PENDENTE'" class="badge bg-warning">Pendente</span>
    <span th:case="'CONFIRMADO'" class="badge bg-success">Confirmado</span>
    <span th:case="*" class="badge bg-secondary">Desconhecido</span>
</span>
```

## Formatação de valores

```html
<!-- Datas -->
<td th:text="${#temporals.format(produto.criadoEm, 'dd/MM/yyyy HH:mm')}"></td>

<!-- Números decimais (moeda) -->
<td th:text="${#numbers.formatDecimal(produto.preco, 1, 'POINT', 2, 'COMMA')}"></td>
<!-- Resultado: 1.234,56 -->

<!-- String utilities -->
<p th:text="${#strings.toUpperCase(produto.nome)}"></p>
<p th:if="${#strings.isEmpty(produto.descricao)}">Sem descrição</p>
```

## Fragmentos — reutilizando partes do HTML

```html
<!-- templates/layout/fragmentos.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

    <!-- Define um fragmento chamado "navbar" -->
    <nav th:fragment="navbar" class="navbar navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" th:href="@{/}">Minha App</a>
            <ul class="navbar-nav">
                <li><a class="nav-link" th:href="@{/produtos}">Produtos</a></li>
            </ul>
        </div>
    </nav>

    <!-- Define um fragmento de rodapé -->
    <footer th:fragment="rodape" class="footer mt-5 py-3 bg-light">
        <div class="container text-center">
            <span class="text-muted">© 2024 Minha Aplicação</span>
        </div>
    </footer>

</body>
</html>
```

```html
<!-- Usando o fragmento em outra página -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Produtos</title></head>
<body>
    <!-- th:replace insere o fragmento "navbar" do arquivo "fragmentos" -->
    <div th:replace="~{layout/fragmentos :: navbar}"></div>

    <div class="container mt-4">
        <!-- conteúdo da página -->
    </div>

    <div th:replace="~{layout/fragmentos :: rodape}"></div>
</body>
</html>
```

## Layout com Thymeleaf Layout Dialect

```html
<!-- templates/layout/base.html — template base -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="UTF-8">
    <!-- th:block substitui pelo título de cada página filha -->
    <title layout:title-pattern="$CONTENT_TITLE - Minha App">Minha App</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-dark bg-dark mb-4">
        <div class="container">
            <a class="navbar-brand" th:href="@{/}">Minha App</a>
        </div>
    </nav>

    <!-- Cada página filha preenche este bloco -->
    <div class="container" layout:fragment="conteudo">
        <!-- conteúdo da página vai aqui -->
    </div>

    <footer class="mt-5 py-3 bg-light text-center">
        <span class="text-muted">© 2024</span>
    </footer>
</body>
</html>
```

```html
<!-- templates/produto/lista.html — página filha -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title>Lista de Produtos</title>
</head>
<body>
    <!-- Preenche o bloco "conteudo" do layout base -->
    <div layout:fragment="conteudo">
        <h1>Produtos</h1>
        <!-- restante do conteúdo -->
    </div>
</body>
</html>
```

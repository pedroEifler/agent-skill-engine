# Thymeleaf — Business Reference

## Reusable Fragments

```html
<!-- templates/layout/fragments.html -->
<div th:fragment="pagination(page, baseUrl, searchParam)">
    <nav th:if="${page.totalPages > 1}" aria-label="Page navigation">
        <ul class="pagination justify-content-center">
            <li class="page-item" th:classappend="${page.first} ? 'disabled'">
                <a class="page-link"
                   th:href="${baseUrl + '?page=' + (page.number - 1) + (searchParam != null ? '&search=' + searchParam : '')}">
                    &laquo;
                </a>
            </li>
            <li class="page-item" th:each="i : ${#numbers.sequence(0, page.totalPages - 1)}"
                th:classappend="${i == page.number} ? 'active'">
                <a class="page-link" th:text="${i + 1}"
                   th:href="${baseUrl + '?page=' + i + (searchParam != null ? '&search=' + searchParam : '')}">
                </a>
            </li>
            <li class="page-item" th:classappend="${page.last} ? 'disabled'">
                <a class="page-link"
                   th:href="${baseUrl + '?page=' + (page.number + 1) + (searchParam != null ? '&search=' + searchParam : '')}">
                    &raquo;
                </a>
            </li>
        </ul>
        <p class="text-center text-muted small">
            Page <span th:text="${page.number + 1}"></span> of <span th:text="${page.totalPages}"></span>
            (<span th:text="${page.totalElements}"></span> total)
        </p>
    </nav>
</div>

<div th:fragment="alerts">
    <div th:if="${successMessage}" class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle me-2"></i>
        <span th:text="${successMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-2"></i>
        <span th:text="${errorMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</div>
```

## Internationalization (i18n)

```java
// config/WebMvcConfig.java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new CookieLocaleResolver("lang");
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        var interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

```properties
# resources/messages.properties (default - English)
product.list.title=Products
product.form.name=Name
product.form.price=Price
product.saved=Product saved successfully.
product.deleted=Product deleted.
validation.name.required=Name is required.
```

```properties
# resources/messages_pt_BR.properties
product.list.title=Produtos
product.form.name=Nome
product.form.price=Preço
product.saved=Produto salvo com sucesso.
product.deleted=Produto removido.
validation.name.required=O nome é obrigatório.
```

```html
<!-- Using messages in templates -->
<h1 th:text="#{product.list.title}"></h1>
<label th:text="#{product.form.name}"></label>

<!-- Language switcher -->
<a th:href="@{''(lang=en)}">English</a>
<a th:href="@{''(lang=pt_BR)}">Português</a>
```

## Conditional CSS Classes

```html
<!-- Status badge with dynamic color -->
<span class="badge"
      th:classappend="${product.active} ? 'bg-success' : 'bg-secondary'"
      th:text="${product.active} ? 'Active' : 'Inactive'">
</span>

<!-- Table row highlight -->
<tr th:each="product, stat : ${products.content}"
    th:classappend="${stat.odd} ? 'table-light'">
```

## Custom Dialect Utility — Formatting

```html
<!-- Dates -->
<td th:text="${#temporals.format(product.createdAt, 'MMM dd, yyyy')}"></td>
<td th:text="${#temporals.format(product.createdAt, 'dd/MM/yyyy HH:mm')}"></td>

<!-- Currency -->
<td th:text="${#numbers.formatDecimal(product.price, 1, 'POINT', 2, 'COMMA')}"></td>

<!-- Truncate long text -->
<p th:text="${#strings.abbreviate(product.description, 100)}"></p>

<!-- Null-safe -->
<span th:text="${product.description ?: 'No description'}"></span>
```

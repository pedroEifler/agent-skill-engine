---
name: mvc-business
description: >
  Use this skill whenever a professional developer requests production-grade Spring MVC project scaffolding,
  server-side rendered web applications, Thymeleaf templates, form handling, Spring Security login flows,
  pagination, file upload, or any full-stack server-rendered Java web application. Triggers include:
  "Spring MVC", "Thymeleaf project", "server-side rendering", "SSR Java", "Spring MVC best practices",
  "Spring MVC with security", "Thymeleaf layout", "form validation Spring MVC", "Spring MVC pagination",
  "Spring MVC file upload", "full stack Spring Boot", "admin panel Spring", "Spring MVC production setup",
  "Spring MVC CRUD professional". Produces clean, production-ready English code without inline comments,
  with proper layered architecture, reusable Thymeleaf fragments, layout dialect, CSRF protection,
  flash messages, pagination, and testable controllers. ALWAYS use this skill for professional Spring MVC tasks.
---

# Skill: MVC Business

Generates production-ready Spring MVC applications with Thymeleaf. Clean English code, no inline comments,
layered architecture, reusable layout system, Spring Security, pagination, and full test coverage.

---

## 1. Version Detection

Identify Spring Boot and Java version before generating.

| Spring Boot | Java | Namespace     | Status          |
|-------------|------|---------------|-----------------|
| 2.7.x       | 8+   | `javax.*`     | Legacy          |
| **3.3.x**   | 17+  | `jakarta.*`   | **Recommended** |

---

## 2. Standard Project Layout

```
my-app/
├── src/main/java/com/company/app/
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── AuthController.java
│   │   └── advice/
│   │       └── GlobalControllerAdvice.java
│   ├── service/
│   │   ├── ProductService.java
│   │   └── impl/
│   │       └── ProductServiceImpl.java
│   ├── repository/
│   │   └── ProductRepository.java
│   ├── model/
│   │   ├── entity/
│   │   │   └── Product.java
│   │   └── form/
│   │       └── ProductForm.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   └── BusinessException.java
│   └── config/
│       ├── SecurityConfig.java
│       └── WebMvcConfig.java
├── src/main/resources/
│   ├── templates/
│   │   ├── layout/
│   │   │   ├── base.html
│   │   │   └── fragments.html
│   │   ├── product/
│   │   │   ├── list.html
│   │   │   ├── form.html
│   │   │   └── detail.html
│   │   ├── auth/
│   │   │   └── login.html
│   │   └── error/
│   │       ├── 403.html
│   │       └── 404.html
│   ├── static/
│   │   ├── css/app.css
│   │   └── js/app.js
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
└── pom.xml
```

---

## 3. Model Layer

```java
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    // getters and setters
}
```

### Form Object (separates HTTP concerns from the entity)

```java
public class ProductForm {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must be at most 150 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @Size(max = 1000)
    private String description;

    public static ProductForm from(Product product) {
        var form = new ProductForm();
        form.id = product.getId();
        form.name = product.getName();
        form.price = product.getPrice();
        form.description = product.getDescription();
        return form;
    }

    public Product toEntity() {
        var product = new Product();
        product.setId(this.id);
        product.setName(this.name);
        product.setPrice(this.price);
        product.setDescription(this.description);
        return product;
    }

    // getters and setters
}
```

---

## 4. Controller Layer

```java
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String search) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var products = search != null && !search.isBlank()
            ? productService.search(search, pageable)
            : productService.findAll(pageable);
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        return "product/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ProductForm());
        return "product/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", ProductForm.from(productService.findById(id)));
        return "product/form";
    }

    @PostMapping
    public String save(@ModelAttribute("form") @Valid ProductForm form,
                       BindingResult binding,
                       RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) return "product/form";
        productService.save(form.toEntity());
        redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted.");
        return "redirect:/products";
    }
}
```

### Global Controller Advice

```java
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Model model) {
        return "error/403";
    }

    @ModelAttribute("appName")
    public String appName() {
        return "My Application";
    }
}
```

---

## 5. Thymeleaf Layout

### Base Layout

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title layout:title-pattern="$CONTENT_TITLE - $DECORATOR_TITLE">My App</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/app.css}" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" th:href="@{/}">My App</a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" th:href="@{/products}" sec:authorize="isAuthenticated()">Products</a>
                <span class="nav-link text-light" sec:authentication="name" sec:authorize="isAuthenticated()"></span>
                <a class="nav-link" th:href="@{/logout}" sec:authorize="isAuthenticated()">Logout</a>
                <a class="nav-link" th:href="@{/login}" sec:authorize="isAnonymous()">Login</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div th:if="${successMessage}" class="alert alert-success alert-dismissible fade show">
            <span th:text="${successMessage}"></span>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible fade show">
            <span th:text="${errorMessage}"></span>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>

        <div layout:fragment="content">
            <!-- page content goes here -->
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script th:src="@{/js/app.js}"></script>
</body>
</html>
```

### List Page with Pagination

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head><title>Products</title></head>
<body>
<div layout:fragment="content">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>Products</h1>
        <a th:href="@{/products/new}" class="btn btn-primary">New Product</a>
    </div>

    <form th:action="@{/products}" method="get" class="row g-2 mb-3">
        <div class="col-auto">
            <input type="text" name="search" th:value="${search}"
                   class="form-control" placeholder="Search...">
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-secondary">Search</button>
        </div>
    </form>

    <table class="table table-hover">
        <thead>
            <tr><th>Name</th><th>Price</th><th>Actions</th></tr>
        </thead>
        <tbody>
            <tr th:each="product : ${products.content}">
                <td th:text="${product.name}"></td>
                <td th:text="${#numbers.formatCurrency(product.price)}"></td>
                <td>
                    <a th:href="@{/products/{id}/edit(id=${product.id})}"
                       class="btn btn-sm btn-warning">Edit</a>
                    <form th:action="@{/products/{id}/delete(id=${product.id})}"
                          method="post" class="d-inline"
                          onsubmit="return confirm('Delete this product?')">
                        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
                        <button class="btn btn-sm btn-danger">Delete</button>
                    </form>
                </td>
            </tr>
        </tbody>
    </table>

    <nav th:if="${products.totalPages > 1}">
        <ul class="pagination">
            <li class="page-item" th:classappend="${products.first} ? 'disabled'">
                <a class="page-link" th:href="@{/products(page=${products.number - 1}, search=${search})}">Previous</a>
            </li>
            <li class="page-item" th:each="i : ${#numbers.sequence(0, products.totalPages - 1)}"
                th:classappend="${i == products.number} ? 'active'">
                <a class="page-link" th:text="${i + 1}"
                   th:href="@{/products(page=${i}, search=${search})}"></a>
            </li>
            <li class="page-item" th:classappend="${products.last} ? 'disabled'">
                <a class="page-link" th:href="@{/products(page=${products.number + 1}, search=${search})}">Next</a>
            </li>
        </ul>
    </nav>
</div>
</body>
</html>
```

---

## 6. application.yml

```yaml
spring:
  application:
    name: my-app
  datasource:
    url: ${DB_URL:jdbc:h2:mem:mydb}
    username: ${DB_USER:sa}
    password: ${DB_PASSWORD:}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  thymeleaf:
    cache: false
  security:
    remember-me:
      key: ${REMEMBER_ME_KEY:change-me-in-production}

server:
  port: ${PORT:8080}
  error:
    whitelabel:
      enabled: false
```

---

## 7. Naming Conventions

```java
// Controllers → noun + Controller
@Controller public class ProductController {}
@Controller public class AuthController {}

// Services → interface + Impl
public interface ProductService {}
@Service public class ProductServiceImpl implements ProductService {}

// Repositories → noun + Repository
public interface ProductRepository extends JpaRepository<Product, Long> {}

// Entities → PascalCase singular
@Entity public class Product {}

// Form objects → noun + Form
public class ProductForm {}

// Thymeleaf templates → lowercase, hyphened
// product/list.html, product/form.html, auth/login.html
```

---

## 8. Checklist

- [ ] `@Controller` (not `@RestController`) on all MVC controllers
- [ ] Form objects separate from JPA entities
- [ ] `@Valid` + `BindingResult` on every POST handler
- [ ] PRG pattern on all mutating operations (Post/Redirect/Get)
- [ ] Flash attributes via `RedirectAttributes` for user feedback
- [ ] CSRF token included in all non-GET forms
- [ ] Base layout with `layout:decorate` on all pages
- [ ] Pagination with `Pageable` on list endpoints
- [ ] `spring.thymeleaf.cache=false` in dev profile
- [ ] `GlobalControllerAdvice` for error pages and common model attributes

---

## 9. Delivery Format

1. **Project structure**
2. **pom.xml** with Spring MVC, Thymeleaf, Security, Validation
3. **application.yml** (dev + prod profiles)
4. **Model** entity + form object
5. **Service** interface + implementation
6. **Controller** with full CRUD + pagination
7. **Templates**: base layout, list, form, login, error pages
8. **Test skeletons**: `@WebMvcTest` + `@SpringBootTest` stubs

Load reference files:
- `references/architectures/thymeleaf.md` — Advanced Thymeleaf expressions, fragments, i18n
- `references/architectures/security.md` — Spring Security with Thymeleaf, role-based views
- `references/architectures/pagination.md` — Pageable, search filters, sort params
- `references/architectures/tests.md` — MockMvc, `@WebMvcTest`, security test patterns

# Spring Security com MVC — Referência para Estudantes

## Dependências no pom.xml

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Integração Thymeleaf + Spring Security (th:sec:*) -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

## Configuração de Segurança

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Define quais páginas precisam de login
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Configura a página de login personalizada
            .formLogin(form -> form
                .loginPage("/login")           // nossa página de login
                .loginProcessingUrl("/login")  // URL que processa o formulário
                .defaultSuccessUrl("/produtos", true)  // redireciona após login
                .failureUrl("/login?erro=true")        // redireciona se erro
                .permitAll()
            )
            // Configura o logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?saiu=true")
                .permitAll()
            )
            .build();
    }

    // Usuários em memória — só para estudar! Em produção use banco de dados.
    @Bean
    public UserDetailsService userDetailsService() {
        var admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("admin123")
            .roles("ADMIN", "USER")
            .build();

        var usuario = User.withDefaultPasswordEncoder()
            .username("usuario")
            .password("user123")
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin, usuario);
    }
}
```

## Página de Login

```java
// Controller para a página de login
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }
}
```

```html
<!-- templates/login.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<div class="container d-flex justify-content-center align-items-center" style="min-height:100vh">
    <div class="card p-4" style="width: 350px">
        <h3 class="text-center mb-4">Entrar</h3>

        <!-- Mensagem de erro de credenciais -->
        <div th:if="${param.erro}" class="alert alert-danger">
            Usuário ou senha incorretos.
        </div>

        <!-- Mensagem após logout -->
        <div th:if="${param.saiu}" class="alert alert-info">
            Você saiu com sucesso.
        </div>

        <!--
          action="/login" → URL que o Spring Security intercepta
          method="post"   → envio seguro das credenciais
        -->
        <form action="/login" method="post">
            <!-- Token CSRF obrigatório com Spring Security! -->
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">

            <div class="mb-3">
                <label class="form-label">Usuário</label>
                <!-- name="username" → nome padrão que o Spring Security espera -->
                <input type="text" name="username" class="form-control" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Senha</label>
                <!-- name="password" → nome padrão que o Spring Security espera -->
                <input type="password" name="password" class="form-control" required>
            </div>

            <button type="submit" class="btn btn-primary w-100">Entrar</button>
        </form>
    </div>
</div>
</body>
</html>
```

## Usando Segurança nos Templates Thymeleaf

```html
<!-- Adicione o namespace de segurança no <html> -->
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<!-- Mostra o nome do usuário logado -->
<span sec:authentication="name">usuário</span>

<!-- Exibe conteúdo apenas para usuários autenticados -->
<div sec:authorize="isAuthenticated()">
    <a th:href="@{/logout}">Sair</a>
</div>

<!-- Exibe apenas para a ROLE_ADMIN -->
<div sec:authorize="hasRole('ADMIN')">
    <a th:href="@{/admin/usuarios}">Gerenciar Usuários</a>
</div>

<!-- Exibe para quem NÃO está logado -->
<div sec:authorize="isAnonymous()">
    <a th:href="@{/login}">Entrar</a>
</div>
```

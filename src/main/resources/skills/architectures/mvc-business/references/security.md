# Spring Security — Business Reference

## Full Security Config with Database Users

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("${security.remember-me.key}")
                .tokenValiditySeconds(604800) // 7 days
                .userDetailsService(userDetailsService)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## UserDetailsService with JPA

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .map(user -> User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .accountExpired(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
```

## Method-Level Security

```java
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String list(Model model) {
        // ...
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') and #id != authentication.principal.id")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        // ...
    }
}
```

## Role-Based Thymeleaf Views

```html
<html xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<!-- Show only to admins -->
<li sec:authorize="hasRole('ADMIN')">
    <a th:href="@{/admin/users}">User Management</a>
</li>

<!-- Show authenticated user's name and email -->
<span sec:authentication="principal.username"></span>

<!-- Conditional button visibility -->
<button th:if="${#authorization.expression('hasRole(''ADMIN'')')}"
        class="btn btn-danger">Delete</button>

<!-- CSRF in AJAX -->
<meta name="_csrf" th:content="${_csrf.token}">
<meta name="_csrf_header" th:content="${_csrf.headerName}">
```

## Login Page

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container d-flex justify-content-center align-items-center vh-100">
    <div class="card shadow p-4" style="width:380px">
        <h4 class="text-center mb-4">Sign In</h4>

        <div th:if="${param.error}" class="alert alert-danger py-2">
            Invalid email or password.
        </div>
        <div th:if="${param.logout}" class="alert alert-info py-2">
            You have been signed out.
        </div>
        <div th:if="${param.expired}" class="alert alert-warning py-2">
            Your session has expired. Please sign in again.
        </div>

        <form th:action="@{/login}" method="post">
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">

            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control" autofocus required>
            </div>
            <div class="mb-3">
                <label class="form-label">Password</label>
                <input type="password" name="password" class="form-control" required>
            </div>
            <div class="mb-3 form-check">
                <input type="checkbox" name="remember-me" class="form-check-input" id="rememberMe">
                <label class="form-check-label" for="rememberMe">Remember me</label>
            </div>
            <button type="submit" class="btn btn-primary w-100">Sign In</button>
        </form>
    </div>
</div>
</body>
</html>
```

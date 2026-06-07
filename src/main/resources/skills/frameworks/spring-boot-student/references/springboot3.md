# Spring Boot 3.x — Referência para Estudantes

> Use quando o estudante estiver no Java 17+ com Spring Boot 3.2.x ou 3.3.x

## pom.xml para Spring Boot 3.3.x + Java 21
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <groupId>br.com.meuprojeto</groupId>
    <artifactId>meu-projeto</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Entidade com Jakarta (3.x)
```java
// Spring Boot 3.x migrou de javax para jakarta — atenção na importação!
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank → não pode ser nulo nem vazio
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    // @Positive → deve ser maior que zero
    @Positive(message = "O preço deve ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
}
```

## SecurityFilterChain (3.x — substitui WebSecurityConfigurerAdapter)
```java
// Spring Boot 3.x: usa SecurityFilterChain como bean (sem herança!)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // @Bean → o Spring gerencia este objeto e o injeta onde precisar
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF para APIs REST stateless
            .csrf(csrf -> csrf.disable())
            // Define quais rotas precisam de autenticação
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/publico/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            // Permite exibir o console H2 no iframe
            .headers(headers -> headers.frameOptions(f -> f.disable()))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Define usuários em memória (apenas para estudos!)
    @Bean
    public UserDetailsService userDetailsService() {
        var usuario = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("123456")
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(usuario);
    }
}
```

## Records como DTOs (Java 17+)
```java
// Records são perfeitos para DTOs — imutáveis e sem boilerplate!

// DTO de entrada (dados que chegam do cliente)
public record ProdutoRequestDTO(
    @NotBlank(message = "Nome é obrigatório") String nome,
    @Positive(message = "Preço deve ser positivo") BigDecimal preco
) { }

// DTO de saída (dados que enviamos ao cliente)
public record ProdutoResponseDTO(Long id, String nome, BigDecimal preco) {

    // Método estático para converter Entidade → DTO
    public static ProdutoResponseDTO de(Produto produto) {
        return new ProdutoResponseDTO(produto.getId(), produto.getNome(), produto.getPreco());
    }
}
```

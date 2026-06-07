# Spring Boot 2.x — Referência para Estudantes

> Use quando o estudante estiver no Java 8, 11 ou 17 com Spring Boot 2.7.x

## Diferenças principais em relação ao 3.x

```java
// Spring Boot 2.x usa javax.* (não jakarta!)
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

// Spring Boot 3.x usa jakarta.*
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
```

## pom.xml para Spring Boot 2.7.x + Java 17
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- O parent do Spring Boot gerencia versões de todas as dependências -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>br.com.meuprojeto</groupId>
    <artifactId>meu-projeto</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- API REST -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- JPA + Hibernate (acesso ao banco de dados) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Banco H2 em memória (ótimo para estudar!) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Validação de dados (@NotNull, @Size, etc.) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Testes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin que empacota o projeto como JAR executável -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## WebSecurityConfigurerAdapter (apenas 2.x — removido no 3.x!)
```java
// No Spring Boot 2.x, a configuração de segurança herda de WebSecurityConfigurerAdapter
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()               // Desabilita CSRF para APIs REST
            .authorizeRequests()
                .antMatchers("/api/publico/**").permitAll()  // Rotas públicas
                .anyRequest().authenticated()               // Resto precisa de autenticação
            .and()
            .httpBasic();                   // Autenticação básica (usuário/senha)
    }
}
```

# Spring Security com JWT — Referência para Estudantes

## Como funciona a autenticação JWT

```
1. Cliente envia: POST /auth/login { "email": "...", "senha": "..." }
2. Servidor valida credenciais e gera um Token JWT
3. Cliente guarda o token e envia em toda requisição:
   Header: Authorization: Bearer <token>
4. Servidor valida o token e libera ou bloqueia o acesso
```

## Dependência no pom.xml
```xml
<!-- Biblioteca para gerar e validar tokens JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

## Serviço de JWT com comentários
```java
/**
 * Responsável por gerar e validar tokens JWT.
 */
@Service
public class TokenService {

    // Chave secreta — em produção, use variável de ambiente!
    @Value("${jwt.secret}")
    private String segredo;

    /**
     * Gera um token JWT para o usuário autenticado.
     *
     * @param usuario o usuário autenticado
     * @return o token JWT como String
     */
    public String gerarToken(Usuario usuario) {
        return Jwts.builder()
            .subject(usuario.getEmail())       // "dono" do token
            .issuedAt(new Date())              // quando foi criado
            .expiration(new Date(System.currentTimeMillis() + 86400000)) // expira em 24h
            .signWith(getChave())              // assina com a chave secreta
            .compact();
    }

    /**
     * Extrai o email do usuário a partir do token.
     */
    public String extrairEmail(String token) {
        return Jwts.parser()
            .verifyWith(getChave())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    // Converte a String do segredo em uma chave criptográfica
    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }
}
```

## Configuração no application.properties
```properties
# Segredo do JWT — use algo longo e aleatório em produção!
jwt.secret=meu-segredo-super-secreto-que-ninguem-vai-adivinhar-123456

# Configurações de segurança
spring.security.user.name=admin
spring.security.user.password=admin123
```

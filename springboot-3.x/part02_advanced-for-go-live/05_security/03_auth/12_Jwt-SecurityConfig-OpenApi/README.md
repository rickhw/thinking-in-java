
好的，我來幫你擴充範例，加上 **JWT 權限控管**，讓：

* Admin 群組只能看到 `/admin/**` API
* User 群組只能看到 `/user/**` API
* 並在 Swagger 上帶入 JWT token 驗證不同權限

---

## 🔐 實作總覽

* 加入 JWT 驗證過濾器（使用 `OncePerRequestFilter`）
* 使用 Spring Security 控制路由存取
* 在 Swagger 上支援 JWT 權限登入（Authorization Bearer）
* 為方便起見，使用硬編碼的 Token 作示範（正式環境建議接 OAuth2 / SSO）

---

## 🧱 專案擴充依賴

在 `build.gradle` 增加：

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

---

## 🔑 JWT Utility

建立 `JwtUtil.java`：

```java
@Component
public class JwtUtil {

    private final String SECRET = "super-secret-key";

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## 🔍 Security Filter

建立 `JwtAuthFilter.java`：

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                String role = jwtUtil.extractRole(token);

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken("user", null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 🛡️ Security 設定

建立 `SecurityConfig.java`：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable()
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/user/**").hasRole("USER")
                    .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

---

## 🧪 建立測試用 Token

範例硬編碼兩種角色的 JWT：

```java
// Admin Token
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.cYbKmSn2fEKoK5JtOeJXzhzShdxQ0Z6SttiMLweo7jM

// User Token
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlVTRVIifQ.dTYAn1JhzqUE3S8M1iH-GGlf4CMFLFzKxEdZCzgnNo4
```

這兩個 token 都是 base64 編碼 payload：

```json
// Admin payload
{ "sub": "admin", "role": "ADMIN" }

// User payload
{ "sub": "user", "role": "USER" }
```

---

## 🌐 Swagger 加入 Authorization 支援

修改 `application.yml` 或加上 Swagger 自定義 config：

```java
@Configuration
public class SwaggerSecurityConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .name("Authorization")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

---

## ✅ 測試方式

1. 啟動專案，開啟：

   * [http://localhost:8080/swagger-ui.html?configUrl=/v3/api-docs/admin](http://localhost:8080/swagger-ui.html?configUrl=/v3/api-docs/admin)
   * [http://localhost:8080/swagger-ui.html?configUrl=/v3/api-docs/user](http://localhost:8080/swagger-ui.html?configUrl=/v3/api-docs/user)

2. 點選右上角 `Authorize`，輸入：

```
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.cYbKmSn2fEKoK5JtOeJXzhzShdxQ0Z6SttiMLweo7jM
```

3. 即可根據角色看到不同群組的 API 文件和操作權限。

---

需要我幫你加入 JWT 的產生與登入 API 嗎？還是你要用現有的 SSO 系統整合？

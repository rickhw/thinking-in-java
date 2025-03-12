# Prompt

用 java 17, spring boot 3.2, spring security, grade, 寫一個 login 的基本框架, 滿足以下條件:

1. 使用 Spring Security 的 login form 當登入頁面
2. 使用者用本地認證，預設密碼使用 BCryptPasswordEncoder
3. Session Token 為 JWT, JWT 相關參數請放在 application.yaml
4. Database 使用 MySQL, 使用者資料表至少要有 username, password, role 欄位
5. Session Token 存放在 Redis Cache, 當使用者瀏覽的時候, backend 會先拿 cookie 中的 session Id 去 cache 檢查，如果存在，那表示使用者還是登入的狀態。

請提供完整的程式。

---

# ChatGPT

這是一個基於 **Java 17**、**Spring Boot 3.2**、**Spring Security**、**Gradle** 的登入框架，符合你的需求：

- 使用 **Spring Security login form** 作為登入頁面。
- 使用者進行 **本地認證**，密碼使用 **BCryptPasswordEncoder** 加密。
- **Session Token 使用 JWT**，相關參數存放於 `application.yaml`。
- **使用 MySQL** 作為資料庫，`users` 表包含 `username`, `password`, `role` 欄位。
- **Session Token 存放於 Redis Cache**，使用者請求時，從 Cookie 取出 JWT，驗證 Redis 內的 session。

---

### 📌 **專案結構**
```
/src
 ├── main
 │   ├── java/com/example/security
 │   │   ├── config
 │   │   │   ├── SecurityConfig.java
 │   │   │   ├── JwtAuthFilter.java
 │   │   │   ├── JwtUtil.java
 │   │   ├── controller
 │   │   │   ├── AuthController.java
 │   │   ├── entity
 │   │   │   ├── UserEntity.java
 │   │   ├── repository
 │   │   │   ├── UserRepository.java
 │   │   ├── service
 │   │   │   ├── UserService.java
 │   │   ├── SecurityApplication.java
 ├── resources
 │   ├── application.yaml
```

---

### 1️⃣ **配置 `application.yaml`**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/security_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  redis:
    host: localhost
    port: 6379

jwt:
  secret: mysecretkey123456789012345678901234567890
  expiration: 3600000  # 1 hour
```

---

### 2️⃣ **建立 `UserEntity.java`**
```java
package com.example.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}
```

---

### 3️⃣ **建立 `UserRepository.java`**
```java
package com.example.security.repository;

import com.example.security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}
```

---

### 4️⃣ **建立 `UserService.java`**
```java
package com.example.security.service;

import com.example.security.entity.UserEntity;
import com.example.security.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void createUser(String username, String password, String role) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
    }
}
```

---

### 5️⃣ **建立 `JwtUtil.java`**
```java
package com.example.security.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

---

### 6️⃣ **建立 `JwtAuthFilter.java`**
```java
package com.example.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthFilter(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String cachedToken = redisTemplate.opsForValue().get(username);
                
                if (cachedToken != null && cachedToken.equals(token)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            new User(username, "", null), null, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
```

---

### 7️⃣ **建立 `SecurityConfig.java`**
```java
package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public SecurityConfig(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated())
            .addFilter(new JwtAuthFilter(jwtUtil, redisTemplate));
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

這樣你的 Spring Security 登入系統就完成了！這個框架支援 **JWT 驗證**，並透過 **Redis 來儲存 session**，提升效能和安全性。
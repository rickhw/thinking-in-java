



---
# ChatGPT

這裡是一個 **Spring Boot 3.2 + Spring Security + Java 17** 的基本 **登入框架**，滿足 **本地帳密認證**，並使用 **JWT** 進行身份驗證。  

## 🔹 **功能需求**
1. **用戶登入**（帳號密碼驗證，回傳 JWT）
2. **用戶註冊**（密碼加密存儲）
3. **JWT 驗證**（攔截未授權請求）
4. **權限管理**（支援 `USER` 角色）

---

## **📂 專案結構**
```
src
 ├── main
 │   ├── java/com/example/security
 │   │   ├── config
 │   │   │   ├── SecurityConfig.java      # Spring Security 配置
 │   │   │   ├── JwtAuthenticationFilter.java  # JWT 過濾器
 │   │   │   ├── JwtUtil.java              # JWT 工具類
 │   │   ├── controller
 │   │   │   ├── AuthController.java       # 登入 & 註冊 API
 │   │   ├── model
 │   │   │   ├── User.java                 # 用戶實體
 │   │   │   ├── Role.java                 # 角色枚舉
 │   │   ├── repository
 │   │   │   ├── UserRepository.java       # JPA Repository
 │   │   ├── service
 │   │   │   ├── UserService.java          # 用戶業務邏輯
 │   │   ├── SecurityApplication.java      # Spring Boot 啟動類
 ├── resources
 │   ├── application.yml
```

---

## **🔹 實作代碼**

### **1️⃣ application.yml**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/security_db
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: "your_jwt_secret_key"
  expiration: 86400000 # 1 天
```

---

### **2️⃣ User.java（用戶實體）**
```java
package com.example.security.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
```

---

### **3️⃣ Role.java（角色枚舉）**
```java
package com.example.security.model;

public enum Role {
    USER,
    ADMIN
}
```

---

### **4️⃣ UserRepository.java（JPA Repository）**
```java
package com.example.security.repository;

import com.example.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

---

### **5️⃣ JwtUtil.java（JWT 工具類）**
```java
package com.example.security.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    private static final String SECRET_KEY = "your_jwt_secret_key";
    private static final long EXPIRATION_TIME = 86400000; // 1 天

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
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
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

### **6️⃣ JwtAuthenticationFilter.java（JWT 過濾器）**
```java
package com.example.security.config;

import com.example.security.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
```

---

### **7️⃣ SecurityConfig.java（Spring Security 配置）**
```java
package com.example.security.config;

import com.example.security.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain() {
        return request -> request
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
```

---

### **8️⃣ AuthController.java（認證 API）**
```java
package com.example.security.controller;

import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import com.example.security.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered";
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> loginData) {
        User user = userRepository.findByUsername(loginData.get("username")).orElseThrow();
        if (passwordEncoder.matches(loginData.get("password"), user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername());
        }
        return "Invalid credentials";
    }
}
```

這樣就完成了一個完整的 **Spring Security + JWT** 登入框架！ 🚀
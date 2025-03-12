
我想增加 cache with Redis, 用來存放 session token. 當使用者瀏覽的時候, backend 會先拿 cookie 中的 session Id 去 cache 檢查，如果存在，那表示使用者還是登入的狀態。

---
# ChatGPT

這裡我們會 **整合 Redis 作為 Session Token 快取**，並修改驗證邏輯，使後端先檢查 Redis，若 **session 存在則認定使用者已登入**，若不存在則拒絕請求。  

---

## **🔹 更新後的功能**
1. **用戶登入**
   - 成功登入後，產生 `sessionId` 並存入 Redis（設定過期時間）。
   - 這個 `sessionId` 會回傳給前端，並存放在 `HttpOnly Cookie`。
   
2. **用戶請求驗證**
   - 每次請求，後端會從 Cookie 取得 `sessionId`，然後去 Redis 查詢：
     - **存在**：允許請求並刷新 TTL（過期時間）。
     - **不存在**：拒絕請求，要求重新登入。
   
3. **用戶登出**
   - 從 Redis 刪除 `sessionId`，使其失效。

---

## **📂 更新後的專案結構**
```
src
 ├── main
 │   ├── java/com/example/security
 │   │   ├── config
 │   │   │   ├── SecurityConfig.java      # Spring Security 配置
 │   │   │   ├── RedisConfig.java         # Redis 設定
 │   │   ├── controller
 │   │   │   ├── AuthController.java       # 登入 & 登出 API
 │   │   ├── model
 │   │   │   ├── User.java                 # 用戶實體
 │   │   ├── repository
 │   │   │   ├── UserRepository.java       # JPA Repository
 │   │   ├── service
 │   │   │   ├── AuthService.java          # 認證邏輯
 │   │   │   ├── RedisService.java         # Redis 操作
 │   │   ├── filter
 │   │   │   ├── SessionAuthenticationFilter.java  # Cookie 驗證
 │   │   ├── SecurityApplication.java      # Spring Boot 啟動類
 ├── resources
 │   ├── application.yml
```

---

## **1️⃣ application.yml（Redis 設定）**
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

  redis:
    host: localhost
    port: 6379
    timeout: 6000ms
    password: ""

session:
  expiration: 1800 # 30 分鐘
```

---

## **2️⃣ RedisConfig.java（Redis 設定）**
```java
package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
```

---

## **3️⃣ RedisService.java（Redis 操作）**
```java
package com.example.security.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionId, String username, long expiration) {
        redisTemplate.opsForValue().set(sessionId, username, Duration.ofSeconds(expiration));
    }

    public String getUsernameFromSession(String sessionId) {
        return redisTemplate.opsForValue().get(sessionId);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}
```

---

## **4️⃣ AuthService.java（登入 & 登出邏輯）**
```java
package com.example.security.service;

import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RedisService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            String sessionId = UUID.randomUUID().toString();
            redisService.saveSession(sessionId, username, 1800); // 30 分鐘
            return sessionId;
        }
        throw new RuntimeException("Invalid credentials");
    }

    public void logout(String sessionId) {
        redisService.deleteSession(sessionId);
    }
}
```

---

## **5️⃣ AuthController.java（登入 & 登出 API）**
```java
package com.example.security.controller;

import com.example.security.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> loginData, HttpServletResponse response) {
        String sessionId = authService.login(loginData.get("username"), loginData.get("password"));

        Cookie cookie = new Cookie("sessionId", sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(1800);
        response.addCookie(cookie);

        return "Login successful";
    }

    @PostMapping("/logout")
    public String logout(@CookieValue(name = "sessionId", required = false) String sessionId, HttpServletResponse response) {
        if (sessionId != null) {
            authService.logout(sessionId);

            Cookie cookie = new Cookie("sessionId", "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "Logged out";
    }
}
```

---

## **6️⃣ SessionAuthenticationFilter.java（Cookie 驗證）**
```java
package com.example.security.filter;

import com.example.security.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final RedisService redisService;

    public SessionAuthenticationFilter(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String sessionId = Arrays.stream(cookies)
                    .filter(cookie -> "sessionId".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (sessionId != null && redisService.getUsernameFromSession(sessionId) != null) {
                chain.doFilter(request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
```

---

## **🔹 總結**
這樣就完成了一個 **Spring Boot + Redis Session-based Authentication**！  
1. **登入** -> 產生 `sessionId` 存入 Redis + 設置 Cookie  
2. **請求時驗證** -> 從 Cookie 讀取 `sessionId`，查詢 Redis  
3. **登出** -> 刪除 Redis session + 清除 Cookie  

這樣後端就能安全地驗證用戶，確保 Session 仍然有效！🚀

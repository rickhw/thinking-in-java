# Prompt

如果我想把 sessionId 內容，改成 jwt，jwt 則填入目前 SecurityContextHolder 的資訊，應該怎麼做？請提供範例程式

---

# ChatGPT

如果你想將 **Session ID 改成 JWT**，並且讓 JWT 內含 `SecurityContextHolder` 的使用者資訊，可以採用 **Spring Security + JWT** 的方式，取代傳統的 HTTP Session。這樣，每次請求時，客戶端會帶上 JWT，而後端可以解析 JWT 取得 `SecurityContextHolder` 的內容。

---

## **解決方案**
1. **使用 JWT 取代 Session**
   - 使用者登入時，產生 JWT，並將 `SecurityContextHolder` 內的資訊存入 JWT Payload。
   - 前端存儲 JWT (通常在 `Authorization: Bearer <token>` Header)。
   - 每次請求時，後端從 JWT 解析使用者資訊，填入 `SecurityContextHolder`。

2. **實作 JWT 過濾器**
   - 攔截請求並解析 JWT，將資訊填入 `SecurityContextHolder`。

3. **關閉 Session**
   - `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`。

---

## **完整程式範例**
### **1. 產生 JWT**
**`JwtUtil.java`**
```java
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtUtil {

    private static final String SECRET_KEY = "YourSuperSecretKeyThatShouldBeLongEnough";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 小時

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public static String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles) // 存放角色資訊
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

### **2. 攔截請求並解析 JWT**
**`JwtAuthenticationFilter.java`**
```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = JwtUtil.parseToken(token);
            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = 
                List.of(claims.get("roles").toString().split(","))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

            UserDetails userDetails = new User(username, "", authorities);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
```

---

### **3. 設定 SecurityConfig**
**`SecurityConfig.java`**
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 無狀態模式
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
```

---

### **4. 登入時發送 JWT**
**`AuthController.java`**
```java
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        String token = JwtUtil.generateToken(authentication);
        return ResponseEntity.ok(token);
    }
}
```

---

## **測試流程**
### **1. 登入取得 JWT**
```bash
curl -X POST "http://localhost:8080/auth/login" -d "username=user&password=pass"
```
回應：
```json
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### **2. 帶上 JWT 訪問受保護資源**
```bash
curl -H "Authorization: Bearer eyJhbGciOi..." http://localhost:8080/protected-resource
```

---

## **總結**
| **功能** | **方法** |
|----------|----------|
| **使用 JWT 取代 Session** | `JwtUtil.generateToken(authentication)` |
| **從 JWT 解析使用者資訊** | `JwtUtil.parseToken(token)` |
| **過濾請求並驗證 JWT** | `JwtAuthenticationFilter` |
| **關閉 Session 機制** | `sessionCreationPolicy(SessionCreationPolicy.STATELESS)` |
| **動態填充 SecurityContext** | `SecurityContextHolder.getContext().setAuthentication(auth)` |

這樣就能讓 **Session ID 改為 JWT**，並且動態填充 `SecurityContextHolder`，讓你的應用程式變得更輕量且無狀態 (Stateless)。🚀
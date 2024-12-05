我將為您提供一個完整的 Spring Boot 範例，展示 JWT 身份驗證的實作。我會將程式碼拆分成多個 artifacts 以便於理解。



1. 首先，我們來建立 Gradle 配置文件：

```groovy
plugins {
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT 相關依賴
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // Lombok 簡化代碼
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

repositories {
    mavenCentral()
}

test {
    useJUnitPlatform()
}

```

2. JWT 工具類別：

```java
package com.example.demo.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("classpath:jwt-private.key")
    private Resource privateKeyResource;

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(privateKeyResource.getFile().toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public String generateToken(String email, String name) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("name", name);

            return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24小時有效
                .signWith(loadPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public PublicKey getPublicKey() {
        try {
            PrivateKey privateKey = loadPrivateKey();
            return Keys.rsaPublicKeyFrom(privateKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Public key extraction failed", e);
        }
    }
}

```

3. 安全配置：

```java
package com.example.demo.config;

import com.example.demo.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**", "/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

```

4. JWT 驗證過濾器：

```java
package com.example.demo.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final PublicKey publicKey;

    public JwtAuthFilter(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

                String email = claims.getSubject();
                UserDetails userDetails = User
                    .withUsername(email)
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/public/") || path.startsWith("/auth/");
    }
}

```

5. 控制器：

```java
package com.example.demo.controller;

import com.example.demo.util.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@RestController
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // 公開 public key 的 API
    @GetMapping("/public/key")
    public ResponseEntity<String> getPublicKey() {
        PublicKey publicKey = jwtUtils.getPublicKey();
        return ResponseEntity.ok(
            Base64.getEncoder().encodeToString(publicKey.getEncoded())
        );
    }

    // 生成 token 的 API
    @PostMapping("/auth/token")
    public ResponseEntity<Map<String, String>> createToken(
        @RequestBody Map<String, String> payload
    ) {
        String email = payload.get("email");
        String name = payload.get("name");

        if (email == null || name == null) {
            return ResponseEntity.badRequest().build();
        }

        String token = jwtUtils.generateToken(email, name);
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    // 需要授權的 API
    @GetMapping("/protected/hello")
    public ResponseEntity<String> protectedHello() {
        return ResponseEntity.ok("Hello, Authenticated User!");
    }
}

```

6. 主應用程式：

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.security.PublicKey;

@SpringBootApplication
public class DemoApplication {

    private final JwtUtils jwtUtils;

    public DemoApplication(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public PublicKey publicKey() {
        return jwtUtils.getPublicKey();
    }
}

```

使用步驟和說明：

1. 在 `resources` 目錄下放置 `jwt-private.key`（RSA 私鑰）

2. 測試 API：
   - 獲取公鑰: `GET /public/key`
   - 生成 Token: `POST /auth/token`
     ```json
     {
       "email": "user@example.com",
       "name": "John Doe"
     }
     ```
   - 訪問受保護資源: `GET /protected/hello`，需要在 Header 加入 `Authorization: Bearer <token>`

3. 配置說明：
   - 安全配置允許 `/public/**` 和 `/auth/**` 路徑不需要驗證
   - JWT Filter 會檢查 Bearer Token 的有效性
   - Token 包含 email 和 name 聲明
   - Token 有效期為 24 小時

注意事項：
- 在生產環境中，應使用更安全的金鑰管理方式
- 建議使用環境變數或更安全的方法來管理金鑰
- 實際應用中需要更複雜的用戶管理和驗證邏輯

需要我進一步解釋任何部分嗎？


---

您遇到的問題是因為 `Keys.rsaPublicKeyFrom()` 方法不可用。我們需要修改 `getPublicKey()` 方法來正確地從私鑰提取公鑰。以下是修正後的 `JwtUtils` 類別：

```java
package com.example.demo.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("classpath:jwt-private.key")
    private Resource privateKeyResource;

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(privateKeyResource.getFile().toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public String generateToken(String email, String name) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("name", name);

            return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24小時有效
                .signWith(loadPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public PublicKey getPublicKey() {
        try {
            PrivateKey privateKey = loadPrivateKey();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(
                new X509EncodedKeySpec(privateKey.getEncoded())
            );
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException("Public key extraction failed", e);
        }
    }
}

```

主要变更：

1. 使用 `KeyFactory` 和 `X509EncodedKeySpec` 从私钥生成公钥
2. 移除了 `Keys.rsaPublicKeyFrom()` 方法的调用
3. 添加了更标准的公钥提取方法

这个修改应该可以解决您编译时遇到的错误。请替换原来的 `JwtUtils` 类，然后重新构建项目。

如果您想生成一个 RSA 密钥对用于测试，可以使用以下 OpenSSL 命令：

```bash
# 生成私钥
openssl genrsa -out src/main/resources/jwt-private.key 2048

# 可选：从私钥导出公钥
openssl rsa -in src/main/resources/jwt-private.key -pubout -out src/main/resources/jwt-public.key
```

建议在实际生产环境中使用更安全的密钥管理方式。

需要我帮您进一步解决任何问题吗？


---

openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private_key.pem -out private_key_pkcs8.pem

以下是一個使用 **Java 17**, **Spring Boot 3.2**, **Gradle**, 並整合 **OAuth2 授權（透過 scope 控制存取權限）** 的簡單 API 範例。

---

### ✅ 功能說明

* 提供一個 `/api/hello` endpoint
* 使用 Spring Security + OAuth2 Resource Server
* 根據 JWT token 中的 `scope` 權限判斷是否可存取該 API
* 使用 `Bearer Token` 驗證

---

## 📁 專案目錄結構

```
oauth2-scope-demo/
├── build.gradle
├── settings.gradle
└── src/
    └── main/
        ├── java/com/example/demo/
        │   ├── DemoApplication.java
        │   ├── controller/HelloController.java
        │   └── config/SecurityConfig.java
        └── resources/
            ├── application.yml
```

---

## 🔧 `build.gradle`

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

---

## 📘 `application.yml`

```yaml
server:
  port: 8080

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://YOUR_ISSUER_URI  # 例如 Okta, Auth0, Keycloak 等
```

> 🔐 測試可用 mock JWT 請參考文末補充。

---

## 🔐 `SecurityConfig.java`

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/hello").hasAuthority("SCOPE_hello.read")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
}
```

---

## 📦 `HelloController.java`

```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello, secured world!";
    }
}
```

---

## 🚀 `DemoApplication.java`

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## 🧪 測試 JWT token 範例（使用 Mock JWT）

你可以產生以下 JWT payload，並加上 HS256 簽名（或改用 Keycloak 等產生）：

```json
{
  "sub": "test-user",
  "scope": "hello.read",
  "iss": "https://your-issuer.com",
  "aud": "your-api"
}
```

Authorization Header：

```http
Authorization: Bearer eyJhbGciOi...
```

---

## ✅ 測試方式（用 curl）

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8080/api/hello
```

---

## 🧩 補充說明

* `SCOPE_hello.read` 是 Spring Security 的慣例：會自動將 `scope` 欄位中的 `hello.read` 對應成 `SCOPE_hello.read` 權限。
* 可以搭配 Keycloak / Auth0 / Okta 等 OAuth Provider 發 token。
* 若你要測試本地 JWT（不連接外部 provider），可使用 [https://jwt.io](https://jwt.io) 產生 token + 設定 `spring.security.oauth2.resourceserver.jwt.secret` 來驗證 HS256。

---

需要我補充：🔧如何整合 Keycloak 產出 Token + scope？還是要加入 Spring Authorization Server 做成全套 OAuth2 server？

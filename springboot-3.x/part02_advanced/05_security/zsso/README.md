我會幫你建立一個使用 Spring Boot 3.x 和 Google OAuth2 的 SSO 範例程式。



```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

```

```properties
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope:
              - email
              - profile

server:
  port: 8080

```

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login**", "/error**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        
        return http.build();
    }
}

```

```java
package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("name"));
            model.addAttribute("email", principal.getAttribute("email"));
        }
        return "dashboard";
    }
}

```

```html
<!-- templates/index.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Welcome</title>
</head>
<body>
    <h1>Welcome to SSO Demo</h1>
    <a href="/login">Login</a>
</body>
</html>

<!-- templates/login.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login</title>
</head>
<body>
    <h1>Login</h1>
    <a href="/oauth2/authorization/google">Login with Google</a>
</body>
</html>

<!-- templates/dashboard.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Dashboard</title>
</head>
<body>
    <h1>Dashboard</h1>
    <p>Welcome <span th:text="${name}">User</span>!</p>
    <p>Email: <span th:text="${email}">email@example.com</span></p>
    <form th:action="@{/logout}" method="post">
        <input type="submit" value="Logout"/>
    </form>
</body>
</html>

```

要使用這個範例，你需要先做以下設定：

1. 在 Google Cloud Console 設定 OAuth 2.0 憑證：
   - 前往 https://console.cloud.google.com
   - 建立一個新專案或選擇既有專案
   - 啟用 Google+ API
   - 在憑證頁面建立 OAuth 2.0 客戶端 ID
   - 設定授權的重新導向 URI 為：`http://localhost:8080/login/oauth2/code/google`

2. 更新 `application.yml` 中的設定：
   - 將 YOUR_GOOGLE_CLIENT_ID 替換為你的 Google Client ID
   - 將 YOUR_GOOGLE_CLIENT_SECRET 替換為你的 Google Client Secret

3. 執行應用程式：
```bash
./gradlew bootRun
```

使用 curl 測試登入流程：

1. 獲取登入頁面：
```bash
curl http://localhost:8080/login
```

2. 由於 OAuth2 需要瀏覽器互動，你需要在瀏覽器中打開以下 URL：
```
http://localhost:8080/oauth2/authorization/google
```

3. 測試受保護的資源（需要先登入）：
```bash
curl -c cookies.txt -b cookies.txt http://localhost:8080/dashboard
```

4. 登出：
```bash
curl -c cookies.txt -b cookies.txt -X POST http://localhost:8080/logout
```

登入流程說明：
1. 用戶訪問 `/login` 頁面
2. 點擊 "Login with Google" 連結
3. 重定向到 Google 登入頁面
4. 用戶授權後，Google 將用戶重定向回應用程式的回調 URL
5. Spring Security 處理 OAuth2 回調，驗證令牌
6. 登入成功後重定向到 `/dashboard`

主要功能：
- 使用 Spring Security 的 OAuth2 客戶端支援
- Google SSO 整合
- 基本的用戶資訊顯示
- 安全的登出功能

需要注意的是，由於 OAuth2 流程需要瀏覽器互動，無法完全使用 curl 測試。建議使用瀏覽器測試完整的登入流程。

你想要我解釋程式碼的哪個部分，或是需要其他功能的補充嗎？
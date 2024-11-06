反過來，用 java17 + springboot 3.2 寫一個 http server，可以指定 api 的 rate limit，當超過 rate limit 的時候回傳 HTTP 429。Rate Limit 可以指定單位為秒、分，以及次數，例如每秒 10 個，稱為 RPS.

請幫我提供完整的程式碼架構，包含 gradle 的部分

---
## ChatGPT

以下是完整的程式碼架構，包括 `build.gradle` 配置和必要的 Java 類別，讓這個 Spring Boot 應用程式可以設置每秒 10 次的 API 請求頻率限制，並在超過此限制時返回 HTTP 429 錯誤。

### 專案結構

```
rate-limit-spring-boot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── ratelimit/
│   │   │               ├── RateLimitApplication.java
│   │   │               ├── config/
│   │   │               │   └── AppConfig.java
│   │   │               ├── filter/
│   │   │               │   └── RateLimitFilter.java
│   │   │               └── controller/
│   │   │                   └── RateLimitController.java
│   │   └── resources/
│   │       └── application.properties
├── build.gradle
└── settings.gradle
```

### 1. `build.gradle`

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.3'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:8.0.0'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 2. `settings.gradle`

```gradle
rootProject.name = 'rate-limit-spring-boot'
```

### 3. 主應用程式入口 `RateLimitApplication.java`

```java
package com.example.ratelimit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimitApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimitApplication.class, args);
    }
}
```

### 4. 配置類別 `AppConfig.java`

在這個配置類別中，我們註冊了 `RateLimitFilter`。

```java
package com.example.ratelimit.config;

import com.example.ratelimit.filter.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.FilterRegistrationBean;

@Configuration
public class AppConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/*"); // 設定需要限流的 API 路徑
        registrationBean.setOrder(1); // 設置過濾器順序
        return registrationBean;
    }
}
```

### 5. Rate Limit 過濾器 `RateLimitFilter.java`

這個 `Filter` 會在每個請求進入前檢查是否超過 Rate Limit，並在超過時回應 HTTP 429。

```java
package com.example.ratelimit.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Bucket bucket;

    public RateLimitFilter() {
        // 設置每秒 10 次請求的速率限制 (RPS)
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (bucket.tryConsume(1)) {
            // 若可消耗，則繼續處理請求
            filterChain.doFilter(request, response);
        } else {
            // 若達到限制，則返回 HTTP 429 錯誤
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too Many Requests - Rate limit exceeded");
        }
    }
}
```

### 6. 控制器 `RateLimitController.java`

這是一個簡單的 API，當被呼叫時會回傳成功訊息，用來測試 Rate Limit 機制。

```java
package com.example.ratelimit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RateLimitController {

    @GetMapping("/test")
    public String test() {
        return "Request successful!";
    }
}
```

### 7. 配置檔案 `application.properties`

這裡不需要額外的設定，文件保持空白即可：

```properties
# application.properties
```

### 執行應用程式

使用以下指令啟動 Spring Boot 應用程式：

```bash
./gradlew bootRun
```

### 測試 Rate Limiting

1. 啟動應用程式後，可以用 `curl` 或任何 HTTP 客戶端工具來測試：
    ```bash
    curl -X GET http://localhost:8080/api/test
    ```
2. 在短時間內多次執行這個指令（例如 10 次以上），你應該會看到部分請求返回 `Request successful!`，但超出 Rate Limit 後將會回應 `HTTP 429 Too Many Requests` 及訊息 `Too Many Requests - Rate limit exceeded`。

### 說明

- **Rate Limit**：這裡的 `Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1)))` 設置了每秒最多 10 次的請求限制。
- **可調整性**：可以根據需求修改 `Bandwidth` 的配置，以實現每分鐘或其他自定義頻率的限流。

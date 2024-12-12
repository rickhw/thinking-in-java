
用 java 17, springboot 3.2, gradle, 幫我寫一個 內部系統之間溝通的 認證方法, 滿足內部系統相互 調用 REST API 的時候，可以做基本驗證，相關條件如下：

1. 當其他人來 call 我的 API 的時候，用 `X-API-KEY` 當作 header，如果不在我設定好的清單中，就回傳 401
2. 驗證 `X-API-KEY` 的流程，請使用 springboot 相關 component 實作, 客戶端的 API Key 先用一個 hashmap 存。
3. 請用 http client 寫一個 client call API 的範例
4. 以 `/internal` 開頭, 請提供一個範例 API 

請提供完整的 java 程式給我。


---
## Claude

我會幫你建立一個完整的內部系統API認證方案。



```java
// build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// src/main/java/com/example/config/ApiKeyAuthFilter.java
package com.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyStore apiKeyStore;

    public ApiKeyAuthFilter(ApiKeyStore apiKeyStore) {
        this.apiKeyStore = apiKeyStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        // 只檢查 /internal 開頭的路徑
        if (path.startsWith("/internal")) {
            String apiKey = request.getHeader("X-API-KEY");
            
            if (apiKey == null || !apiKeyStore.isValidApiKey(apiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

// src/main/java/com/example/config/ApiKeyStore.java
package com.example.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiKeyStore {
    private final Map<String, String> apiKeys = new ConcurrentHashMap<>();

    public ApiKeyStore() {
        // 初始化一些測試用的 API Keys
        apiKeys.put("service1-api-key", "Service 1");
        apiKeys.put("service2-api-key", "Service 2");
    }

    public boolean isValidApiKey(String apiKey) {
        return apiKeys.containsKey(apiKey);
    }

    public void addApiKey(String apiKey, String serviceName) {
        apiKeys.put(apiKey, serviceName);
    }
}

// src/main/java/com/example/controller/InternalApiController.java
package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalApiController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Service is healthy!";
    }
}

// src/main/java/com/example/client/ApiClient.java
package com.example.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ApiClient {
    private final WebClient webClient;

    public ApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("X-API-KEY", "service1-api-key")
                .build();
    }

    public String callHealthCheck() {
        return webClient.get()
                .uri("/internal/health")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

// src/main/java/com/example/InternalApiApplication.java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InternalApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(InternalApiApplication.class, args);
    }
}

// src/test/java/com/example/ApiClientTest.java
package com.example;

import com.example.client.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ApiClientTest {

    @Autowired
    private ApiClient apiClient;

    @Test
    void testHealthCheck() {
        String response = apiClient.callHealthCheck();
        assertEquals("Service is healthy!", response);
    }
}

```

這個實作包含以下主要組件：

1. **ApiKeyAuthFilter**: 實作了一個過濾器，用於驗證所有以 `/internal` 開頭的請求中的 `X-API-KEY` header。

2. **ApiKeyStore**: 管理API Keys的組件，使用 ConcurrentHashMap 儲存有效的 API Keys。

3. **InternalApiController**: 提供了一個範例的內部 API endpoint `/internal/health`。

4. **ApiClient**: 使用 WebClient 實作的客戶端，展示如何呼叫內部 API。

使用方式：

1. 啟動應用程式：
```bash
./gradlew bootRun
```

2. 呼叫 API 的方式：
```bash
# 使用有效的 API Key
curl -H "X-API-KEY: service1-api-key" http://localhost:8080/internal/health

# 使用無效的 API Key
curl -H "X-API-KEY: invalid-key" http://localhost:8080/internal/health
```

你可以根據需求修改以下部分：

1. 在 `ApiKeyStore` 中新增更多的 API Keys
2. 在 `InternalApiController` 中新增更多的內部 API endpoints
3. 修改 `ApiKeyAuthFilter` 的驗證邏輯
4. 調整 `ApiClient` 的設定，例如超時時間、重試機制等

需要注意的是，這個實作是基本的範例，在實際生產環境中，你可能還需要考慮：

1. API Keys 的加密儲存
2. 資料庫儲存而不是 HashMap
3. API Keys 的過期機制
4. 更完整的錯誤處理
5. 請求速率限制
6. 監控和日誌記錄

要新增這些功能，我可以提供更詳細的實作範例。你需要哪些額外的功能嗎？
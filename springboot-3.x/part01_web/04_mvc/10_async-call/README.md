
https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html

以下是使用 **Java 17**、**Spring Boot 3.2** 和 **Gradle** 建立一個基於 Spring MVC 的異步請求完整範例。這個範例參考了 [Spring 官方文檔](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)，演示了如何使用 `@Async` 處理長時間運行的操作。

---

### 項目結構
```
async-mvc-example/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/asyncmvc/
│   │   │       ├── AsyncController.java
│   │   │       ├── AsyncService.java
│   │   │       └── AsyncMvcExampleApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
```

---

### `build.gradle`
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
java {
    sourceCompatibility = '17'
    targetCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-log4j2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

### `AsyncMvcExampleApplication.java`
主應用程序入口：
```java
package com.example.asyncmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AsyncMvcExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsyncMvcExampleApplication.class, args);
    }
}
```

---

### `AsyncController.java`
控制器處理異步請求：
```java
package com.example.asyncmvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

@RestController
public class AsyncController {

    private static final Logger logger = LoggerFactory.getLogger(AsyncController.class);

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/async-process")
    public CompletableFuture<String> asyncProcess() {
        logger.info("Controller: Received request");
        return asyncService.performAsyncTask()
                .thenApply(result -> {
                    logger.info("Controller: Sending response");
                    return result;
                });
    }
}
```

---

### `AsyncService.java`
執行耗時操作的服務：
```java
package com.example.asyncmvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Async
    public CompletableFuture<String> performAsyncTask() {
        logger.info("Service: Starting async task");
        try {
            Thread.sleep(5000); // 模擬耗時操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("Service: Completed async task");
        return CompletableFuture.completedFuture("Async process complete!");
    }
}
```

---

### `application.yml`
配置文件：
```yaml
spring:
  main:
    allow-bean-definition-overriding: true
logging:
  level:
    root: INFO
```

---

### `logback-spring.xml`
可選的日誌配置：
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

---

### 本地運行
1. **啟動應用程序**：
   ```bash
   ./gradlew bootRun
   ```

2. **測試異步接口**：
   在瀏覽器或 Postman 中訪問以下 URL：
   ```
   http://localhost:8080/async-process
   ```
   頁面會在 5 秒後返回：
   ```
   Async process complete!
   ```

3. **查看日誌輸出**：
   您可以在控制台中看到類似以下的日誌順序，顯示控制器和服務是異步執行的：
   ```
   2024-12-06 10:00:00 [http-nio-8080-exec-1] INFO  c.e.a.AsyncController - Controller: Received request
   2024-12-06 10:00:00 [Async-1] INFO  c.e.a.AsyncService - Service: Starting async task
   2024-12-06 10:00:05 [Async-1] INFO  c.e.a.AsyncService - Service: Completed async task
   2024-12-06 10:00:05 [http-nio-8080-exec-1] INFO  c.e.a.AsyncController - Controller: Sending response
   ```

---

此範例展示了如何用 Spring MVC 實現異步處理，同時利用 `@Async` 提升應用程序的性能和響應速度。
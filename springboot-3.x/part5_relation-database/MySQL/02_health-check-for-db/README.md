## Prompt

幫我改進這個 springboot application, 新增兩個 api, 第一個是確認 springboot 自身是 health 的，第二個 api 是可檢測 database 是否正常運作
---

## Claude
我會為您的 Spring Boot 應用程式添加健康檢查 API。我會使用 Spring Actuator 和自定義的健康檢查器來實現這個功能。

```groovy
// build.gradle
dependencies {
    // 之前的依賴項
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

```

```properties
# src/main/resources/application.properties
# 之前的配置...

# Actuator 配置
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always

# 自定義健康檢查
management.health.custom.enabled=true

```

```java
package com.example.myapp.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            // 執行一個簡單的查詢來檢查資料庫連線
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                .withDetail("database", "MySQL is ready")
                .withDetail("users_count", getUsersCount())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Cannot connect to database")
                .withDetail("message", e.getMessage())
                .build();
        }
    }

    private int getUsersCount() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        } catch (Exception e) {
            return -1;
        }
    }
}

```

```java
package com.example.myapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "Spring Boot Application");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
}

```

我对原有项目做了以下改进：

1. 添加 Spring Actuator 依赖：
   - 提供开箱即用的健康检查端点
   - 可以通过 `/actuator/health` 访问系统健康状态

2. 自定义数据库健康检查器 `DatabaseHealthIndicator`：
   - 检查数据库连接是否正常
   - 返回用户总数
   - 如果数据库连接失败，会返回错误详情

3. 添加自定义 API 端点 `HealthCheckController`：
   - `/api/health` 端点返回应用基本健康信息

4. 配置文件更新：
   - 暴露 Actuator 健康检查端点
   - 显示详细的健康检查信息

现在您可以通过以下端点检查系统健康：

1. Spring Actuator 健康检查：
   - `http://localhost:8181/actuator/health`
   - 显示整体系统健康状态，包括数据库连接情况

2. 自定义应用健康检查：
   - `http://localhost:8181/api/health`
   - 返回应用基本信息

建议：
- 在生产环境中，考虑限制 Actuator 端点的访问
- 可以根据需要添加更多自定义健康检查
- 考虑添加安全机制来保护这些端点

希望这个升级满足您的需求！如果您需要更复杂的健康检查或有其他specific要求，我可以进一步调整。
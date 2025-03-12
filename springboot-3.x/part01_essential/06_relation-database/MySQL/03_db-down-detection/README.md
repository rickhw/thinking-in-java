## Prompt


~$ curl localhost:8181/actuator/health
{"status":"UP"}                                                                                                                               
~$ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"UP"}

❯ docker ps
CONTAINER ID   IMAGE                        COMMAND                  CREATED              STATUS              PORTS                                                   NAMES
35974c8e634e   phpmyadmin/phpmyadmin        "/docker-entrypoint.…"   About a minute ago   Up About a minute   0.0.0.0:8080->80/tcp, [::]:8080->80/tcp                 phpmyadmin
923eab47418b   02_health-check-for-db-app   "java -jar /app/app.…"   About a minute ago   Up About a minute   8181/tcp, 0.0.0.0:8181->8080/tcp, [::]:8181->8080/tcp   springboot-app
ad8513a29255   mysql:8.0                    "docker-entrypoint.s…"   About a minute ago   Up About a minute   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp    mysqldb

❯ docker stop mysqldb
mysqldb

❯ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"UP"}%                                                                       

❯ curl localhost:8181/actuator/health
{"status":"DOWN"}%                                                                                                                               



---

## Claude

您的觀察很好。我們可以通過以下幾種方式改進健康檢查的響應時間和行為：

```properties
# src/main/resources/application.properties
# 資料庫連線超時設定
spring.datasource.hikari.connection-timeout=3000
spring.datasource.hikari.validation-timeout=3000

# Actuator 健康檢查策略
management.endpoint.health.show-details=always
management.health.defaults.enabled=true
management.health.db.enabled=true

```

```java
package com.example.myapp.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService executorService;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public Health health() {
        try {
            // 使用有限時間的檢查
            Future<Health> healthFuture = executorService.submit(this::checkDatabaseHealth);
            Health health = healthFuture.get(3, TimeUnit.SECONDS);
            return health;
        } catch (TimeoutException e) {
            return Health.down()
                .withDetail("error", "Database health check timed out")
                .withDetail("timeout", "3 seconds")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Database connection failed")
                .withDetail("message", e.getMessage())
                .build();
        }
    }

    private Callable<Health> checkDatabaseHealth() {
        return () -> {
            try {
                // 快速檢查連線的查詢
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                
                // 額外檢查使用者資料表
                Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
                
                return Health.up()
                    .withDetail("database", "MySQL is ready")
                    .withDetail("users_count", userCount)
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("error", "Cannot connect to database")
                    .withDetail("message", e.getMessage())
                    .build();
            }
        };
    }
}

```

```java
package com.example.myapp.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    private final HealthEndpoint healthEndpoint;

    public HealthCheckController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Health systemHealth = healthEndpoint.health();
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", systemHealth.getStatus().getCode());
        health.put("application", "Spring Boot Application");
        health.put("version", "1.0.0");
        
        // 如果有額外的健康檢查詳情，也一併回傳
        if (!systemHealth.getDetails().isEmpty()) {
            health.put("details", systemHealth.getDetails());
        }
        
        return ResponseEntity.ok(health);
    }
}

```

主要改進點：

1. 資料庫連線超時設定
   - 設定 `connection-timeout` 和 `validation-timeout` 為 3 秒
   - 快速失敗，避免長時間等待

2. 健康檢查改進
   - 使用 `ExecutorService` 限制檢查時間
   - 3 秒內未完成，直接返回 DOWN 狀態
   - 增加更多錯誤訊息和詳細資訊

3. 健康檢查控制器
   - 整合 Actuator 的健康檢查
   - 在自定義 API 中返回系統整體健康狀態
   - 攜帶額外的健康檢查詳情

預期效果：
- `/api/health` 會迅速返回
- `/actuator/health` 在資料庫無法連線時也會快速失敗
- 提供更多有用的錯誤資訊

測試建議：
```bash
# 停止 MySQL 容器
docker stop mysqldb

# 快速檢查
curl localhost:8181/api/health
curl localhost:8181/actuator/health
```

這些修改應該能顯著改善健康檢查的響應時間和可用性。您可以根據實際需求微調超時時間和檢查策略。


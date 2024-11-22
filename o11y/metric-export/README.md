## Prompt 

我想監控一個 springboot (3.4), java 17 的應用程式，透過 grafana 做可視化，包含以下：

1. Host machine CPU / Memory 使用狀況
2. JVM 的使用狀況

請幫我寫一個完整的程式，包含 docker-compose, 用 spring-boot-starter-actuator, 或者其他必要的 libraries.

我可以透過 docker-compose up 之後，直接打開 grafana 看到前述的 metric


## Claude

我會幫您建立一個完整的監控方案，使用 Prometheus 來收集指標，並用 Grafana 來視覺化。這個方案會包含以下組件：

1. Spring Boot 應用程式 (with Actuator & Micrometer)
2. Prometheus (用於收集和儲存指標)
3. Grafana (用於視覺化)
4. Node Exporter (用於收集主機指標)



```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    networks:
      - monitoring

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - monitoring
    depends_on:
      - app
      - node-exporter

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
    networks:
      - monitoring
    depends_on:
      - prometheus

  node-exporter:
    image: prom/node-exporter:latest
    ports:
      - "9100:9100"
    networks:
      - monitoring
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    command:
      - '--path.procfs=/host/proc'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'

networks:
  monitoring:
    driver: bridge

```

```java
// build.gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}

// application.properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=always

```

```yaml
# prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']

```

```yaml
# grafana/provisioning/datasources/datasource.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true

# grafana/provisioning/dashboards/dashboard.yml
apiVersion: 1

providers:
  - name: 'Default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    editable: true
    options:
      path: /var/lib/grafana/dashboards

# grafana/dashboards/jvm-dashboard.json
{
  "annotations": {
    "list": []
  },
  "editable": true,
  "fiscalYearStartMonth": 0,
  "graphTooltip": 0,
  "id": 1,
  "links": [],
  "liveNow": false,
  "panels": [
    {
      "datasource": {
        "type": "prometheus",
        "uid": "default"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 0,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          }
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 0
      },
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "default"
          },
          "editorMode": "code",
          "expr": "rate(process_cpu_usage[5m])",
          "legendFormat": "CPU Usage",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "CPU Usage",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "default"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 0,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "bytes"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 0
      },
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "default"
          },
          "editorMode": "code",
          "expr": "jvm_memory_used_bytes{area=\"heap\"}",
          "legendFormat": "Heap Memory Used",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "JVM Heap Memory Usage",
      "type": "timeseries"
    }
  ],
  "refresh": "5s",
  "schemaVersion": 38,
  "style": "dark",
  "tags": [],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-1h",
    "to": "now"
  },
  "timepicker": {},
  "timezone": "",
  "title": "JVM Metrics",
  "version": 1,
  "weekStart": ""
}

```

```dockerfile
# don't use openjdk:17- 
# FROM openjdk:17-jdk-slim

FROM eclipse-temurin:17-jdk-jammy
# FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY build/libs/*.jar app.jar

# 修正 ENTRYPOINT 的語法
ENTRYPOINT ["java", "-jar", "app.jar"]

```

請按照以下步驟設置和運行：

1. 創建專案結構：
```
your-project/
  ├── src/
  ├── build.gradle
  ├── Dockerfile
  ├── docker-compose.yml
  ├── prometheus/
  │   └── prometheus.yml
  └── grafana/
      ├── provlisioning/
      │   ├── datasources/
      │   │   └── datasource.yml
      │   └── dashboards/
      │       └── dashboard.yml
      └── dashboards/
          └── jvm-dashboard.json
```

2. 複製上述所有配置文件到對應目錄

3. 構建和運行：
```bash
./gradlew build
docker-compose up
```

4. 訪問各服務：
- Grafana: http://localhost:3000 (用戶名/密碼: admin/admin)
- Spring Boot 應用: http://localhost:8080
- Prometheus: http://localhost:9090

這個設置提供：
1. Host 機器的監控（通過 Node Exporter）：
   - CPU 使用率
   - 記憶體使用率
   - 磁碟使用率
   - 網路流量

2. JVM 監控：
   - Heap 記憶體使用率
   - GC 統計
   - 線程統計
   - CPU 使用率

需要其他指標或自定義 dashboard 嗎？我可以幫您調整。




---

我來幫您建立一個簡單的 REST API 範例，這樣您可以更好地測試監控效果。



```java
// src/main/java/com/example/demo/DemoApplication.java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

// src/main/java/com/example/demo/model/Task.java
package com.example.demo.model;

public class Task {
    private Long id;
    private String title;
    private String description;
    private String status;

    // Constructor
    public Task(Long id, String title, String description, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// src/main/java/com/example/demo/service/TaskService.java
package com.example.demo.service;

import com.example.demo.model.Task;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final Counter tasksCreated;
    private final Counter tasksCompleted;

    public TaskService(MeterRegistry registry) {
        this.tasksCreated = Counter.builder("tasks.created")
                .description("Total number of tasks created")
                .register(registry);
        
        this.tasksCompleted = Counter.builder("tasks.completed")
                .description("Total number of tasks completed")
                .register(registry);
    }

    public Task createTask(String title, String description) {
        Long id = idGenerator.incrementAndGet();
        Task task = new Task(id, title, description, "PENDING");
        tasks.put(id, task);
        tasksCreated.increment();
        return task;
    }

    public Task getTask(Long id) {
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task updateTaskStatus(Long id, String status) {
        Task task = tasks.get(id);
        if (task != null) {
            task.setStatus(status);
            if ("COMPLETED".equals(status)) {
                tasksCompleted.increment();
            }
            tasks.put(id, task);
        }
        return task;
    }

    public void deleteTask(Long id) {
        tasks.remove(id);
    }
}

// src/main/java/com/example/demo/controller/TaskController.java
package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Timed(value = "tasks.creation.time", description = "Time taken to create task")
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task.getTitle(), task.getDescription());
    }

    @GetMapping("/{id}")
    @Timed(value = "tasks.fetch.time", description = "Time taken to fetch task")
    public Task getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @GetMapping
    @Timed(value = "tasks.fetch.all.time", description = "Time taken to fetch all tasks")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/{id}/status")
    @Timed(value = "tasks.status.update.time", description = "Time taken to update task status")
    public Task updateTaskStatus(@PathVariable Long id, @RequestBody String status) {
        return taskService.updateTaskStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "tasks.deletion.time", description = "Time taken to delete task")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}

```

```properties
# src/main/resources/application.properties
spring.application.name=demo-app
server.port=8080

# Actuator Configuration
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.tags.application=${spring.application.name}

# Custom Metrics Configuration
management.metrics.enable.all=true

```

現在您可以使用以下 curl 命令來測試 API：

```bash
# 創建任務
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","description":"This is a test task"}'

# 獲取所有任務
curl http://localhost:8080/api/tasks

# 獲取特定任務
curl http://localhost:8080/api/tasks/1

# 更新任務狀態
curl -X PUT http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '"COMPLETED"'

# 刪除任務
curl -X DELETE http://localhost:8080/api/tasks/1
```

這個範例提供了以下功能：

1. 基本的 CRUD API 操作
2. 自定義業務指標監控：
   - 任務創建數量 (tasks.created)
   - 任務完成數量 (tasks.completed)
3. API 性能指標監控：
   - API 響應時間 (通過 @Timed 註解)
   - 請求計數
   - 錯誤率

在 Grafana 中，您可以看到：
1. JVM 相關指標
2. API 調用指標
3. 業務指標（任務創建和完成數量）
4. 系統資源使用情況

您可以根據需要在 Grafana 中創建新的 dashboard 來展示這些指標。需要我幫您調整任何部分嗎？
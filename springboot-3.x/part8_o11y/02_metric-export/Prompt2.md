
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
curl -X PUT http://localhost:8080/api/tasks/3/status \
  -H "Content-Type: application/json" \
  -d '"COMPLETED"'

# 刪除任務
curl -X DELETE http://localhost:8080/api/tasks/3
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
用 java17, springboot 3.2, gradle, 我想設計一個類似於 springboot 的 starter-web，可以透過 application.yaml 配置這個 starter-web 的設定，像是 uri 的配置與功能開關。請用一個預設的 RESTful API : task 的 CRUD 為例，提供一個完整的範例給我。

---

下面是一個完整的範例，設計了一個名為 `starter-web` 的 Spring Boot Starter，支持通過 `application.yaml` 配置設定，並提供一個 RESTful API (`task` 的 CRUD) 作為示例。

### 專案結構

```
starter-web/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.starterweb/
│   │   │       ├── StarterWebAutoConfiguration.java
│   │   │       ├── TaskController.java
│   │   │       ├── Task.java
│   │   │       ├── TaskService.java
│   │   │       └── StarterWebProperties.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── spring.factories
│   └── test/
│       └── java/
│           └── com.example.starterweb/
│               └── StarterWebTest.java
```

---

### `build.gradle`

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.3'
}

group = 'com.example'
version = '1.0.0-SNAPSHOT'
sourceCompatibility = '17'

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

### `StarterWebAutoConfiguration.java`

```java
package com.example.starterweb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "starter.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StarterWebAutoConfiguration {

    @Bean
    public TaskService taskService() {
        return new TaskService();
    }
}
```

---

### `StarterWebProperties.java`

```java
package com.example.starterweb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "starter.web")
public class StarterWebProperties {

    private boolean enabled = true;
    private String baseUri = "/api/tasks";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}
```

---

### `TaskController.java`

```java
package com.example.starterweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${starter.web.base-uri:/api/tasks}")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable int id, @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable int id) {
        taskService.deleteTask(id);
    }
}
```

---

### `TaskService.java`

```java
package com.example.starterweb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskService {

    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public List<Task> getAllTasks() {
        return tasks;
    }

    public Task createTask(Task task) {
        task.setId(nextId++);
        tasks.add(task);
        return task;
    }

    public Task updateTask(int id, Task updatedTask) {
        Optional<Task> existingTask = tasks.stream().filter(t -> t.getId() == id).findFirst();
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            task.setName(updatedTask.getName());
            task.setDescription(updatedTask.getDescription());
            return task;
        }
        throw new IllegalArgumentException("Task not found");
    }

    public void deleteTask(int id) {
        tasks.removeIf(task -> task.getId() == id);
    }
}
```

---

### `Task.java`

```java
package com.example.starterweb;

public class Task {
    private int id;
    private String name;
    private String description;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
```

---

### `spring.factories`

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.example.starterweb.StarterWebAutoConfiguration
```

---

### `application.yaml` 配置示例

```yaml
starter:
  web:
    enabled: true
    base-uri: /custom/tasks
```

---

### 測試配置

- 啟動應用程式後，訪問 `http://localhost:8080/custom/tasks` 可執行 CRUD 操作。
- 可以通過 `application.yaml` 動態調整配置，例如更改 `base-uri` 或關閉 Starter (`enabled: false`)。

此設計範例展示了如何開發自定義 Starter，並允許使用者通過配置檔進行靈活配置。
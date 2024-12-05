以下是滿足需求的完整範例專案，包含 Java Spring Boot 應用程式的設定、Docker Compose 配置，以及生成日誌並發送至 Elasticsearch 的腳本。  

---

### **1. 建立 Spring Boot API 專案**

#### 目錄結構
```plaintext
api-app/
├── src/main/java/com/example/api
│   ├── ApiAppApplication.java
│   ├── controller/LogController.java
│   ├── config/LoggingConfig.java
│   └── model/RequestLog.java
├── src/main/resources
│   ├── application.yml
│   └── logback-spring.xml
├── build.gradle
├── docker-compose.yml
├── filebeat.yml
└── log-client/
    ├── log-client.py
```

---

#### **Spring Boot 應用程式代碼**

1. **`ApiAppApplication.java`**
```java
package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiAppApplication.class, args);
    }
}
```

2. **`LogController.java`**
```java
package com.example.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @PostMapping
    public String logEvent(@RequestBody Map<String, String> payload) {
        logger.info("Received log event: {}", payload);
        return "Log recorded";
    }
}
```

3. **`LoggingConfig.java`**
自動設定 Logback，包含 Elasticsearch 需要的欄位。  
使用 MDC（Mapped Diagnostic Context）來添加欄位。

```java
package com.example.api.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Configuration
public class LoggingConfig {
    @PostConstruct
    public void setup() {
        // Add default fields to logs
        MDC.put("application", "api-app");
        MDC.put("environment", "development");
        MDC.put("traceId", UUID.randomUUID().toString());
    }
}
```

4. **`logback-spring.xml`**
設定 Logback，實現檔案輸出與日誌滾動。

```xml
<configuration>
    <property name="LOG_FILE" value="logs/api-app.log" />

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeBasedRollingPolicy">
            <maxFileSize>10MB</maxFileSize>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

5. **`application.yml`**
```yaml
server:
  port: 8080
spring:
  application:
    name: api-app
logging:
  level:
    root: INFO
```

---

### **2. Docker Compose 配置**

**`docker-compose.yml`**
```yaml
version: '3.7'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.10.0
    container_name: kibana
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch

  grafana:
    image: grafana/grafana:10.1.0
    container_name: grafana
    ports:
      - "3000:3000"

  filebeat:
    image: docker.elastic.co/beats/filebeat:8.10.0
    container_name: filebeat
    volumes:
      - ./filebeat.yml:/usr/share/filebeat/filebeat.yml
      - ./logs:/logs
    depends_on:
      - elasticsearch
```

---

### **3. Filebeat 配置**

**`filebeat.yml`**
```yaml
filebeat.inputs:
  - type: log
    paths:
      - /logs/*.log
    fields:
      app_name: api-app
    fields_under_root: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  protocol: "http"
setup.kibana:
  host: "kibana:5601"
```

---

### **4. Log Client 腳本**

**`log-client/log-client.py`**
```python
import requests
import random
import time

url = "http://localhost:8080/api/logs"
events = [
    {"event": "login", "user": "user1"},
    {"event": "logout", "user": "user2"},
    {"event": "purchase", "user": "user3", "amount": "30"},
]

while True:
    payload = random.choice(events)
    response = requests.post(url, json=payload)
    print(f"Sent: {payload}, Response: {response.text}")
    time.sleep(random.randint(1, 3))
```

---

### **啟動步驟**
1. 啟動 Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. 啟動 Spring Boot 應用程式:
   ```bash
   ./gradlew bootRun
   ```

3. 運行 Client 腳本:
   ```bash
   python log-client/log-client.py
   ```

4. 開啟 Kibana:
   - 瀏覽至 `http://localhost:5601`
   - 搜索日誌索引（`filebeat-*`）並檢視記錄。

---

完成後，應能在 Kibana 中看到應用程式的日誌資料，包括 Elasticsearch 所需的欄位。
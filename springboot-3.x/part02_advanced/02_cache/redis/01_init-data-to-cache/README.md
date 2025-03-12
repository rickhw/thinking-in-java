我想在 application 初始的時候，有一些 資料從 repository 讀取，放在特定的 bean 裡，幫我寫一個範例程式。

這個程式使用 java 17, springboot 3.4, gradle.

- repository 先用從 resources 目錄底下讀取一個 json 檔案.
- 如果檔案有更新的時候，則會自動重新讀取。
- 讀取的資料放在 redis 裡，所以要先有 redis server.
- 檔案的資料可以透過 api 更新，更新的時候，要同時更新 redis 裡的資料。

請提供我完整的範例程式碼。


---

我會幫你建立一個完整的範例，展示如何在應用程式啟動時從 JSON 檔案讀取資料，並與 Redis 整合，同時實現檔案監控和 API 更新功能。



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
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'commons-io:commons-io:2.15.1'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// src/main/resources/config.json
{
    "settings": {
        "maxUsers": 100,
        "maxConnections": 50,
        "timeoutSeconds": 30
    }
}

// src/main/java/com/example/config/AppConfig.java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class AppConfig {
    
    @Bean
    public RedisTemplate<String, Settings> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Settings> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Settings.class));
        return template;
    }
}

// src/main/java/com/example/config/Settings.java
package com.example.config;

public class Settings {
    private int maxUsers;
    private int maxConnections;
    private int timeoutSeconds;

    // Getters and setters
    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}

// src/main/java/com/example/config/ConfigurationLoader.java
package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigurationLoader {

    private static final String REDIS_KEY = "app:settings";
    private static final String CONFIG_FILE = "config.json";
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Settings> redisTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ConfigurationLoader(RedisTemplate<String, Settings> redisTemplate, 
                             ResourceLoader resourceLoader) {
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() throws Exception {
        // 初始加載配置
        loadConfiguration();
        
        // 設置檔案監控
        setupFileMonitoring();
    }

    public void loadConfiguration() throws Exception {
        InputStream inputStream = resourceLoader.getResource("classpath:" + CONFIG_FILE)
                                             .getInputStream();
        
        Settings settings = objectMapper.readValue(inputStream, Settings.class);
        redisTemplate.opsForValue().set(REDIS_KEY, settings);
    }

    private void setupFileMonitoring() throws Exception {
        String path = resourceLoader.getResource("classpath:" + CONFIG_FILE)
                                  .getFile()
                                  .getParent();
        
        FileAlterationObserver observer = new FileAlterationObserver(path);
        observer.addListener(new FileAlterationListener() {
            @Override
            public void onFileChange(File file) {
                if (file.getName().equals(CONFIG_FILE)) {
                    try {
                        loadConfiguration();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Other required methods with empty implementations
            public void onStart(FileAlterationObserver observer) {}
            public void onDirectoryCreate(File directory) {}
            public void onDirectoryChange(File directory) {}
            public void onDirectoryDelete(File directory) {}
            public void onFileCreate(File file) {}
            public void onFileDelete(File file) {}
            public void onStop(FileAlterationObserver observer) {}
        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(5));
        monitor.addObserver(observer);
        monitor.start();
    }

    public Settings getCurrentSettings() {
        return redisTemplate.opsForValue().get(REDIS_KEY);
    }

    public void updateSettings(Settings newSettings) {
        redisTemplate.opsForValue().set(REDIS_KEY, newSettings);
    }
}

// src/main/java/com/example/controller/SettingsController.java
package com.example.controller;

import com.example.config.ConfigurationLoader;
import com.example.config.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ConfigurationLoader configurationLoader;

    @Autowired
    public SettingsController(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    @GetMapping
    public ResponseEntity<Settings> getSettings() {
        return ResponseEntity.ok(configurationLoader.getCurrentSettings());
    }

    @PutMapping
    public ResponseEntity<Settings> updateSettings(@RequestBody Settings settings) {
        configurationLoader.updateSettings(settings);
        return ResponseEntity.ok(settings);
    }
}

// src/main/resources/application.yml
spring:
  redis:
    host: localhost
    port: 6379

```

這個範例程式實現了以下功能：

1. **檔案讀取**:
   - 從 `resources/config.json` 讀取配置檔案
   - 使用 Jackson 進行 JSON 解析
   - 在應用程式啟動時自動載入

2. **檔案監控**:
   - 使用 Apache Commons IO 的 FileAlterationMonitor
   - 每 5 秒檢查一次檔案變更
   - 檔案更新時自動重新載入

3. **Redis 整合**:
   - 配置 RedisTemplate 用於資料存取
   - 使用 Jackson 序列化器處理 JSON 轉換
   - 所有設定都存儲在 Redis 中

4. **API 端點**:
   - GET `/api/settings`: 獲取當前設定
   - PUT `/api/settings`: 更新設定

使用方式：

1. 確保有 Redis 服務器運行在本機的 6379 端口
2. 啟動應用程式
3. 可以通過 API 進行設定的讀取和更新
4. 也可以直接修改 `config.json` 檔案，系統會自動重新載入

測試 API：

```bash
# 獲取設定
curl http://localhost:8080/api/settings

# 更新設定
curl -X PUT http://localhost:8080/api/settings \
  -H "Content-Type: application/json" \
  -d '{"maxUsers":200,"maxConnections":100,"timeoutSeconds":60}'
```

你需要任何部分的詳細說明嗎？或是想了解特定功能的實現細節？
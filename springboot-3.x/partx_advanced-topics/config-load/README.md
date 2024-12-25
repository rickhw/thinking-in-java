
用 java 17, springboot 3.2, gradle, 幫我完成以下需求:

寫一個 bean as singletone, 主要內容是提供 application config
這個 app config 的內容依序從 class static field -> application.yaml -> database (JPA), 讀取規則是後蓋前. 如果 database 有值就會蓋掉 application.yaml
config 主要結構是 key / value, 例如 default.quota=10
database 請使用 mariadb, 存取則使用 JPA, 請提供完整的程式碼給我.

---

我會幫你建立一個完整的應用程式配置管理系統。



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
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.mariadb.jdbc:mariadb-java-client'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// src/main/resources/application.yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/configdb
    username: root
    password: root
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

app:
  config:
    default.quota: "20"
    max.connections: "100"

// src/main/java/com/example/config/StaticConfig.java
package com.example.config;

import java.util.HashMap;
import java.util.Map;

public class StaticConfig {
    private static final Map<String, String> DEFAULT_CONFIG = new HashMap<>();
    
    static {
        DEFAULT_CONFIG.put("default.quota", "5");
        DEFAULT_CONFIG.put("max.connections", "50");
    }
    
    public static Map<String, String> getDefaultConfig() {
        return new HashMap<>(DEFAULT_CONFIG);
    }
}

// src/main/java/com/example/config/ConfigProperties.java
package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.config")
public class ConfigProperties {
    private final Map<String, String> properties = new HashMap<>();

    public Map<String, String> getProperties() {
        return properties;
    }
}

// src/main/java/com/example/entity/ConfigEntity.java
package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_config")
@Data
public class ConfigEntity {
    @Id
    @Column(length = 255)
    private String configKey;
    
    @Column(length = 1000)
    private String configValue;
}

// src/main/java/com/example/repository/ConfigRepository.java
package com.example.repository;

import com.example.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, String> {
}

// src/main/java/com/example/config/ApplicationConfig.java
package com.example.config;

import com.example.entity.ConfigEntity;
import com.example.repository.ConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationConfig {
    private final Map<String, String> configMap = new HashMap<>();
    
    @Autowired
    private ConfigProperties yamlConfig;
    
    @Autowired
    private ConfigRepository configRepository;
    
    @PostConstruct
    public void init() {
        // 1. Load static config
        configMap.putAll(StaticConfig.getDefaultConfig());
        
        // 2. Override with YAML config
        configMap.putAll(yamlConfig.getProperties());
        
        // 3. Override with database config
        configRepository.findAll().forEach(config -> 
            configMap.put(config.getConfigKey(), config.getConfigValue())
        );
    }
    
    public String getConfig(String key) {
        return configMap.get(key);
    }
    
    public Map<String, String> getAllConfig() {
        return new HashMap<>(configMap);
    }
    
    public void updateConfig(String key, String value) {
        ConfigEntity config = new ConfigEntity();
        config.setConfigKey(key);
        config.setConfigValue(value);
        configRepository.save(config);
        configMap.put(key, value);
    }
}

```

這個實現提供了以下功能：

1. 單例配置 Bean：`ApplicationConfig` 使用 Spring 的 `@Component` 註解，確保單例模式。

2. 三層配置優先順序：
   - 首先載入 `StaticConfig` 中的靜態配置
   - 然後用 `application.yaml` 中的配置覆蓋
   - 最後用資料庫中的配置覆蓋

3. 資料庫支援：
   - 使用 MariaDB 作為資料庫
   - 通過 JPA 進行資料存取
   - 配置表包含 key-value 對

主要組件說明：

1. `StaticConfig`：包含靜態預設配置
2. `ConfigProperties`：讀取 YAML 配置
3. `ConfigEntity`：資料庫實體類
4. `ConfigRepository`：資料庫訪問接口
5. `ApplicationConfig`：主配置類，整合所有配置來源

使用方式：

```java
@Autowired
private ApplicationConfig appConfig;

public void example() {
    // 讀取配置
    String quota = appConfig.getConfig("default.quota");
    
    // 更新配置（會保存到資料庫）
    appConfig.updateConfig("default.quota", "15");
}
```

要使用這個系統，你需要：

1. 設置 MariaDB 資料庫
2. 確保 `application.yaml` 中的資料庫連接配置正確
3. 運行應用程式，表結構會自動創建

需要補充或修改的部分嗎？比如：
- 添加配置刷新機制
- 添加配置驗證
- 擴展更多功能？
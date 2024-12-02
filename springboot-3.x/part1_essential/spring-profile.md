

Spring Profile 是 Spring Boot 提供的一個強大特性，允許您根據不同的環境（如開發、測試、生產）配置不同的應用程式行為。讓我詳細介紹一下：

### Spring Profile 的基本概念

1. 定義 Profile
您可以在 `application.yaml` 或 `application.properties` 中定義不同的 Profile：

```yaml
# application.yaml
---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:devdb

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://production-server/mydb
```

2. 啟動方式

有多種方式啟動特定 Profile：

```bash
# 命令行啟動
java -jar myapp.jar --spring.profiles.active=dev

# 環境變數
export SPRING_PROFILES_ACTIVE=prod

# JVM 參數
java -Dspring.profiles.active=test -jar myapp.jar
```

3. 程式碼中使用 Profile

```java
@Configuration
@Profile("dev")
public class DevConfig {
    // 僅在開發環境載入的配置
}

@Component
@Profile("!prod")  // 非生產環境
public class LocalCacheService {
    // 開發和測試環境使用的快取服務
}
```

### 結合 Region 配置的進階用法

我們可以為不同的 Profile 準備不同的 `regions.yaml`：

```yaml
# regions-dev.yaml
regions:
  - name: DEV_REGION_1
    displayName: "Development Region 1"
    endpoint: "dev-endpoint-1"

# regions-prod.yaml
regions:
  - name: PROD_REGION_1
    displayName: "Production Region 1"
    endpoint: "prod-endpoint-1"
```

修改配置載入邏輯：

```java
@Configuration
public class RegionConfigLoader {
    @Value("${spring.config.activate.on-profile}")
    private String activeProfile;

    @Bean
    public RegionProperties regionProperties() throws IOException {
        String filename = "regions-" + activeProfile + ".yaml";
        Resource resource = new ClassPathResource(filename);
        
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(resource.getInputStream(), RegionProperties.class);
    }
}
```

### Profile 的最佳實踐

1. 預設 Profile
```yaml
spring:
  profiles:
    default: dev  # 如果沒有指定，使用 dev
```

2. 多重 Profile
```bash
# 同時啟用多個 Profile
java -jar myapp.jar --spring.profiles.active=dev,swagger
```

3. Profile 條件注入
```java
@Component
@Profile("${app.feature.cache.enabled:false}")
public class OptionalCacheService {
    // 可通過配置開關的服務
}
```

### 實用技巧

1. Profile 特定屬性覆蓋
```yaml
# application.yaml
app:
  feature:
    rate-limit: 100  # 預設值

# application-prod.yaml
app:
  feature:
    rate-limit: 1000  # 生產環境覆蓋
```

2. 使用 Maven/Gradle Profile
可以在建置時直接綁定 Spring Profile：
```gradle
springBoot {
    buildInfo {
        properties {
            artifact = 'myapp'
            version = version
            group = group
        }
    }
}

test {
    useJUnitPlatform()
    systemProperty 'spring.profiles.active', 'test'
}
```

### 注意事項

- Profile 名稱建議使用小寫
- 避免在 Profile 間放置敏感資訊
- 使用 `@Profile` 時要謹慎，不要過度使用
- 考慮使用 Spring Cloud Config 進行更複雜的配置管理

這樣的設計可以幫助您：
- 靈活管理不同環境的配置
- 實現環境隔離
- 簡化部署流程
- 提高應用程式的可配置性

您對 Spring Profile 還有什麼疑問嗎？
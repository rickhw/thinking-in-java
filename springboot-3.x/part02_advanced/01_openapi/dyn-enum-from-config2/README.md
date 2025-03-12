我會提供一個更加完整、結合 Spring Profile 的實現方案。

1. 首先，我們定義一個更強大的 Region Enum

```java
public enum Region {
    // 列舉保持基本結構，具體內容將由配置決定
    UNKNOWN;

    private String displayName;
    private String endpoint;

    // Getter 方法
    public String getDisplayName() {
        return displayName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    // 在載入後可以由外部設置
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
```

2. Region 配置類別

```java
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class RegionProperties {
    private List<RegionConfig> regions;

    @Data
    public static class RegionConfig {
        private String name;
        private String displayName;
        private String endpoint;
    }

    // 將配置轉換為 Map，方便後續處理
    public Map<String, RegionConfig> toMap() {
        return regions.stream()
            .collect(Collectors.toMap(
                RegionConfig::getName, 
                config -> config
            ));
    }
}

```

3. Region 配置服務

```java
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Arrays;
import java.util.Map;

@Configuration
public class RegionConfigService implements InitializingBean {

    @Autowired
    private RegionProperties regionProperties;

    @Autowired
    private Environment env;

    // 動態更新 Enum 值
    @Override
    public void afterPropertiesSet() {
        Map<String, RegionProperties.RegionConfig> configMap = 
            regionProperties.toMap();

        // 動態更新 Enum
        Arrays.stream(Region.values())
            .filter(r -> r != Region.UNKNOWN)
            .forEach(region -> {
                RegionProperties.RegionConfig config = 
                    configMap.get(region.name());
                
                if (config != null) {
                    region.setDisplayName(config.getDisplayName());
                    region.setEndpoint(config.getEndpoint());
                }
            });
    }

    // 額外的輔助方法
    public RegionProperties.RegionConfig getRegionConfig(String regionName) {
        return regionProperties.toMap().get(regionName);
    }
}

```

4. Region 配置載入器（支援多 Profile）

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;

@Configuration
public class RegionConfigLoader {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public RegionProperties regionProperties() throws IOException {
        // 根據 Profile 動態選擇 YAML 檔案
        String filename = "regions-" + activeProfile + ".yaml";
        
        // 如果特定 Profile 的檔案不存在，則使用預設檔案
        Resource resource = new ClassPathResource(filename);
        if (!resource.exists()) {
            resource = new ClassPathResource("regions-default.yaml");
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(resource.getInputStream(), RegionProperties.class);
    }
}

```

5. 配置檔案範例

`regions-default.yaml`:
```yaml
regions:
  - name: US_EAST_1
    displayName: "Default US East"
    endpoint: "default-ec2.us-east-1.amazonaws.com"
```

`regions-dev.yaml`:
```yaml
regions:
  - name: US_EAST_1
    displayName: "Development US East"
    endpoint: "dev-ec2.us-east-1.amazonaws.com"
```

`regions-prod.yaml`:
```yaml
regions:
  - name: US_EAST_1
    displayName: "Production US East"
    endpoint: "prod-ec2.us-east-1.amazonaws.com"
```

6. Controller 範例

```java
@RestController
@RequestMapping("/api/regions")
public class RegionController {
    @Autowired
    private RegionConfigService regionConfigService;

    @GetMapping("/{region}/ec2/instances")
    public ResponseEntity<?> getEC2Instances(@PathVariable String region) {
        RegionProperties.RegionConfig regionConfig = 
            regionConfigService.getRegionConfig(region);
        
        if (regionConfig == null) {
            return ResponseEntity.badRequest().body("Invalid region");
        }

        return ResponseEntity.ok(regionConfig);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listRegions() {
        return ResponseEntity.ok(
            Arrays.stream(Region.values())
                .filter(r -> r != Region.UNKNOWN)
                .map(r -> Map.of(
                    "name", r.name(),
                    "displayName", r.getDisplayName(),
                    "endpoint", r.getEndpoint()
                ))
                .collect(Collectors.toList())
        );
    }
}
```

7. Gradle 依賴

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-configuration-processor'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

啟動方式：
```bash
# 開發環境
java -jar myapp.jar --spring.profiles.active=dev

# 生產環境
java -jar myapp.jar --spring.profiles.active=prod
```

這個設計的優點：
1. 完全動態載入 Region 配置
2. 支持多環境配置
3. 可以輕鬆擴展 Region
4. 型別安全
5. 預設值處理
6. 靈活的 Profile 切換

您對這個實現有什麼想法或需要調整的地方嗎？



---


在 Gradle 中，有幾種方式可以設定 Spring Profile：

1. 直接在 Gradle 任務中設定系統屬性

```bash
./gradlew bootRun -Dspring.profiles.active=dev
```

2. 使用 `applicationDefaultJvmArgs`

在 `build.gradle` 中配置：

```gradle
bootRun {
    systemProperty 'spring.profiles.active', 'dev'
}
```

3. 環境變數方式

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

4. 更複雜的配置：

```gradle
bootRun {
    // 可以根據不同環境動態設定
    doFirst {
        def activeProfile = project.hasProperty('profile') ? project.property('profile') : 'default'
        systemProperty 'spring.profiles.active', activeProfile
    }
}
```

然後可以這樣執行：

```bash
# 使用特定 profile
./gradlew bootRun -Pprofile=dev

# 預設 profile
./gradlew bootRun
```

5. 打包時設定 Profile

```bash
./gradlew bootJar -Dspring.profiles.active=prod
```

推薦的做法是第4種，因為它提供了最大的靈活性。您可以根據自己的需求選擇最適合的方式。
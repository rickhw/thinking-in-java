
在 Java 17 和 Spring Boot 3.2 的環境中，可以透過 Logback 來設定 log 寫入的配置，並實現檔案位置、命名、格式、以及日誌滾動規則。以下是完整的 `logback-spring.xml` 配置範例，可以滿足您指定的需求。

### `logback-spring.xml` 配置檔案

首先，在您的 `src/main/resources` 資料夾下新增一個 `logback-spring.xml` 文件，並寫入以下配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- 設定 log 檔案儲存路徑 -->
    <property name="LOG_PATH" value="/var/log/appName" />
    <property name="LOG_NAME_PATTERN" value="%d{yyyy-MM-dd}.log" />
    
    <!-- 設定 log 格式 -->
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>

    <!-- 定義 ROOT logger 並輸出到檔案 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- log 檔案位置與命名 -->
        <file>${LOG_PATH}/${LOG_NAME_PATTERN}</file>
        
        <!-- 設定日誌滾動策略，設定檔案名稱和滾動規則 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 保留半年內的 log -->
            <fileNamePattern>${LOG_PATH}/%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>180</maxHistory>
            <!-- 可選：設定單檔案大小限制 -->
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 根 logger 設定 -->
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 配置說明

- **LOG_PATH**：指定日誌檔案儲存的位置。這裡設為 `/var/log/appName`。
- **LOG_NAME_PATTERN**：日誌檔案的命名格式（例：`2024-11-20.log`）。
- **FileAppender**：設置日誌輸出的文件位置。
- **RollingPolicy**：使用 `TimeBasedRollingPolicy` 滾動策略，每天產生新的日誌檔案並依據日期命名。
  - `fileNamePattern`：滾動後的日誌檔案的命名規則。
  - `maxHistory`：保留半年的日誌檔案（180 天）。
  - `totalSizeCap`：可選，設置總日誌檔案大小限制為 5GB。

### 說明

1. **Log 格式**：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n` 這種格式適合 Elasticsearch 或 Meilisearch 之類的 log shipper 工具直接解析。
2. **日誌文件保留與輪替**：這裡使用 `TimeBasedRollingPolicy`，並透過 `maxHistory` 控制保留時間達半年（180 天）。

### Spring Boot 應用程式啟動前的權限

記得要確保 `/var/log/appName` 資料夾對您的應用程式有寫入權限。
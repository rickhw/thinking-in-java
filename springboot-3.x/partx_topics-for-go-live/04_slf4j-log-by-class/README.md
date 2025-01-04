我想依照 package 或者 class 寫出不一樣的 log format (JSON Line), 每個 package 的 log 會根據 domain 需求, 寫入不同欄位的資訊, 最後會送給 elasticsearch 做分析使用.

logback 配置如下，我想針對 package com.gtcafe.asimov.platform.capacity 寫出 log file, 然後 log format 裡, 會根據請求狀況會寫入 capacity remaining, capacity consumed 等資訊.

我的環境是 java17, springboot 3.2, gradle. 請提供我可以調整 logback-spring.xml 的方式與範例, 或者告訴我其他需要配的地方.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    
    <appender name="ConsoleJsonLine" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- <pattern>{ "timestamp":"%d{yyyy-MM-dd'T'HH:mm:ss.SSS}", "scheme": "%X{Protocol}", "method": "%X{Method}", "uri": "%X{RequestURI}", "clientIp": "%X{ClientIP}", "level":"%level", "message": "%message", "thread":"%thread", "class":"%logger{36}", "requestId": "%X{R-Request-Id}" }%n</pattern> -->
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %highlight(%-5level) %green([%thread]) %logger{36}.%M\(%line\) %message%n</pattern>
        </encoder>
    </appender>

    <!-- 定義 ROOT logger 並輸出到檔案 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 初始 log 檔案位置，僅用於當前日誌 -->
        <!-- <file>${LOG_PATH}/current.log</file> -->
        <file>current.log</file>
        
        <!-- 設定日誌滾動策略，設定檔案名稱和滾動規則 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 每天創建新檔案，依據日期命名 -->
            <!-- <fileNamePattern>${LOG_PATH}/%d{yyyy-MM-dd}.log</fileNamePattern> -->
            <fileNamePattern>%d{yyyy-MM-dd}.log</fileNamePattern>
            <!-- 保留半年內的 log -->
            <maxHistory>180</maxHistory>
            <!-- 可選：設定單檔案大小限制 -->
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %highlight(%-5level) %green([%thread]) %logger{36}.%M\(%line\) %message%n</pattern>
        </encoder>
    </appender>

    <!-- 根 logger 設定 -->
    <root level="INFO">
        <appender-ref ref="ConsoleJsonLine" />
        <appender-ref ref="FILE" />
    </root>
</configuration>

```

---

# ChatGPT

為了讓 Logback 根據 package (`com.gtcafe.asimov.platform.capacity`) 寫入不同的 Log 格式與欄位，你可以採用自訂的 `Appender` 或 `Encoder`，並透過 Logback 的 `logger` 設定針對特定 package 寫入專屬的 log file。

以下是改進的 `logback-spring.xml` 配置範例：

### 更新的配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender -->
    <appender name="ConsoleJsonLine" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %highlight(%-5level) [%thread] %logger{36}.%M(%line) %message%n</pattern>
        </encoder>
    </appender>

    <!-- General Rolling File Appender -->
    <appender name="GeneralFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/general.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/general.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>180</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %-5level [%thread] %logger{36}.%M(%line) %message%n</pattern>
        </encoder>
    </appender>

    <!-- Capacity-specific Appender -->
    <appender name="CapacityFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/capacity.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/capacity.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>180</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- Log as JSON -->
            <customFields>{"app":"asimov","module":"capacity"}</customFields>
        </encoder>
    </appender>

    <!-- Capacity-specific Logger -->
    <logger name="com.gtcafe.asimov.platform.capacity" level="INFO" additivity="false">
        <appender-ref ref="CapacityFile" />
    </logger>

    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="ConsoleJsonLine" />
        <appender-ref ref="GeneralFile" />
    </root>
</configuration>
```

---

### 說明
1. **Capacity-specific Appender**:
   - 定義了一個 `CapacityFile` Appender，專門處理來自 `com.gtcafe.asimov.platform.capacity` package 的 log。
   - 使用了 `LogstashEncoder`，能將 log 轉換為 JSON 格式，並加入自訂欄位（例如：`capacity remaining` 或 `capacity consumed` 等）。

2. **Logger 配置**:
   - 針對 package `com.gtcafe.asimov.platform.capacity` 配置了一個 `Logger`，且設置 `additivity="false"` 確保 log 不會傳播到其他 Appender。
   - 此 logger 會將 log 寫入 `CapacityFile`。

3. **Log 格式化**:
   - 使用 `LogstashEncoder` 可產生 JSON 格式的 log，並可根據需求動態新增欄位。

---

### 進一步動態欄位設置
在程式碼中，你可以使用 MDC（Mapped Diagnostic Context）來動態設置 log 中的欄位：

#### 使用範例
```java
import org.slf4j.MDC;

public class CapacityService {
    public void processRequest() {
        try {
            // 設置 MDC 欄位
            MDC.put("capacityRemaining", "100");
            MDC.put("capacityConsumed", "50");

            // 寫 log
            logger.info("Processed capacity request successfully");
        } finally {
            // 清除 MDC
            MDC.clear();
        }
    }
}
```

#### 產生的 JSON log 範例
```json
{
  "timestamp": "2025-01-02T15:30:00.123",
  "level": "INFO",
  "message": "Processed capacity request successfully",
  "capacityRemaining": "100",
  "capacityConsumed": "50",
  "app": "asimov",
  "module": "capacity"
}
```

---

這樣的配置和程式碼能夠讓你針對不同 package 實現高度自訂化的 log 格式，同時支持送出 Elasticsearch 進行分析。
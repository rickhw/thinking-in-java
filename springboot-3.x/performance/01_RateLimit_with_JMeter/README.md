用 java17, springboot 3.2, gradle, 寫一個 RESTful API. 這個 api 本身具備 Rate Limit，可以透過設定控制 RPS (Request Per Second)，如果超過 RPS 設定，則會回應 429.

1. 請幫我完成這個應用程式，並提供完整的程式碼。
2. 請用 jmeter 提供一個測試案例，測試這個 api 的 Rate Limit 功能。


我將分步驟為您完成這個 Rate Limit API 的實現。



```java
Project Structure:
.
├── build.gradle
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── ratelimit
│   │   │               ├── RateLimitApplication.java
│   │   │               ├── config
│   │   │               │   └── RateLimitConfig.java
│   │   │               ├── controller
│   │   │               │   └── DemoController.java
│   │   │               └── interceptor
│   │   │                   └── RateLimitInterceptor.java
│   │   └── resources
│   │       └── application.yml
│   └── test
│       └── java
│           └── com
│               └── example
│                   └── ratelimit
│                       └── RateLimitApplicationTests.java
└── jmeter
    └── RateLimitTest.jmx

```

接下來我會按順序展示每個文件的實現：

1. build.gradle
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
    useJUnitPlatform()
}

```

2. application.yml
```yaml
spring:
  application:
    name: rate-limit-demo

ratelimit:
  enabled: true
  requests-per-second: 5

```

3. RateLimitApplication.java
```java
package com.example.ratelimit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimitApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimitApplication.class, args);
    }
}

```

4. RateLimitConfig.java
```java
package com.example.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitConfig {
    private boolean enabled = true;
    private int requestsPerSecond = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }
}

```

5. RateLimitInterceptor.java
```java
package com.example.ratelimit.interceptor;

import com.example.ratelimit.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitConfig config;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitInterceptor(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!config.isEnabled()) {
            return true;
        }

        String clientId = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        return false;
    }

    private Bucket createNewBucket(String clientId) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(
                config.getRequestsPerSecond(), 
                Refill.greedy(config.getRequestsPerSecond(), Duration.ofSeconds(1))
            ))
            .build();
    }
}

```

6. DemoController.java
```java
package com.example.ratelimit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Rate Limited World!";
    }
}

```

7. RateLimitApplicationTests.java
```java
package com.example.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RateLimitApplicationTests {
    @Test
    void contextLoads() {
    }
}

```

8. JMeter 測試腳本
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.5">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Rate Limit Test" enabled="true">
      <stringProp name="TestPlan.comments"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.elements"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Rate Limit Test Group" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">-1</intProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">10</stringProp>
        <stringProp name="ThreadGroup.ramp_time">1</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">10</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSampler guiclass="HttpTestSampleGui" testclass="HTTPSampler" testname="Rate Limit Request" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
            <collectionProp name="Arguments.elements"/>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding"></stringProp>
          <stringProp name="HTTPSampler.path">/demo/hello</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
          <stringProp name="HTTPSampler.connect_timeout"></stringProp>
          <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </HTTPSampler>
        <hashTree>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Response Assertion" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49587">429</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message">Rate Limit Exceeded</stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">true</boolProp>
            <intProp name="Assertion.test_type">8</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
      </hashTree>
    </hashTree>
  </hashTree>

```

最後，我需要補充一個重要的依賴庫 bucket4j，請在 build.gradle 中添加：

```groovy
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0'
```

這個 Rate Limit 實現具備以下特性：

1. 可通過 `application.yml` 配置 RPS
2. 使用 `bucket4j` 實現令牌桶算法
3. 基於客戶端 IP 進行限流
4. 超過限流會返回 429 狀態碼

JMeter 測試腳本:
- 模擬 10 個線程
- 持續 10 秒
- 測試 `/demo/hello` 接口
- 檢查是否有 429 狀態碼

使用步驟:
1. 確保已安裝 JDK 17
2. 安裝 Gradle
3. 安裝 JMeter
4. 執行 Spring Boot 應用
5. 運行 JMeter 測試腳本

希望這個實現滿足您的需求！如有任何問題，歡迎隨時詢問。


---

以下是使用 CLI 執行 JMeter 測試的詳細步驟：

1. 確保已安裝 JMeter
```bash
# 下載並解壓 JMeter（假設已下載）
tar -xvzf apache-jmeter-5.5.tgz
cd apache-jmeter-5.5/bin
```

2. 執行 JMeter 測試的基本命令
```bash
# 基本測試執行命令
./jmeter -n -t /path/to/RateLimitTest.jmx -l test_results.jtl
```

3. 完整的測試執行命令（包含更多選項）
```bash
./jmeter -n \
  -t /path/to/RateLimitTest.jmx \
  -l test_results.jtl \
  -j jmeter.log \
  -e \
  -o test_report
```

命令參數解釋：
- `-n`：非 GUI 模式運行
- `-t`：指定測試計劃文件
- `-l`：結果日誌文件
- `-j`：JMeter 日誌文件
- `-e`：在測試後生成報告
- `-o`：報告輸出目錄

4. 查看測試結果
```bash
# 查看結果日誌
cat test_results.jtl

# 查看 JMeter 日誌
cat jmeter.log

# 在瀏覽器打開報告
# 打開 test_report/index.html
```

5. 高級用法：設置系統屬性和測試參數
```bash
./jmeter -n \
  -t /path/to/RateLimitTest.jmx \
  -l test_results.jtl \
  -j jmeter.log \
  -e -o test_report \
  -Jthreads=20 \
  -Jrampup=2 \
  -Jduration=30
```

注意事項：
- 確保 JMeter 的 bin 目錄在 PATH 中
- 路徑需要使用絕對路徑
- 需要 Java 運行環境支持

建議的完整流程：
1. 準備測試文件
2. 啟動 Spring Boot 應用
3. 執行 JMeter 測試
4. 分析結果報告


---

## 解釋 JMeter

我很樂意為您詳細解釋 JMeter 測試腳本的配置和請求發送邏輯。讓我們逐步解析 JMeter 測試腳本的關鍵部分：

```xml
<ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Rate Limit Test Group" enabled="true">
    <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
    <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
        <boolProp name="LoopController.continue_forever">false</boolProp>
        <intProp name="LoopController.loops">-1</intProp>
    </elementProp>
    <stringProp name="ThreadGroup.num_threads">10</stringProp>
    <stringProp name="ThreadGroup.ramp_up">1</stringProp>
    <boolProp name="ThreadGroup.scheduler">true</boolProp>
    <stringProp name="ThreadGroup.duration">10</stringProp>
    <stringProp name="ThreadGroup.delay"></stringProp>
    <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
</ThreadGroup>
```

详细解释：

1. `ThreadGroup.num_threads="10"`: 
   - 同時創建 10 個線程
   - 每個線程都會執行相同的請求（訪問 `/demo/hello`）

2. `ThreadGroup.ramp_up="1"`: 
   - 在 1 秒內逐步啟動所有 10 個線程
   - 意味著每 0.1 秒（1秒/10線程）啟動一個新線程

3. `ThreadGroup.duration="10"`: 
   - 測試持續 10 秒
   - 10 秒後所有線程停止

4. `LoopController.loops="-1"`: 
   - 每個線程在持續時間內無限循環執行請求
   - `-1` 表示只受時間限制，不限制循環次數

請求發送頻率分析：
- 10 個線程
- 每秒啟動 10 個線程
- 每個線程盡可能快地發送請求

實際效果：
- 理論上每秒會發送 10 * X 次請求（X 取決於單個請求處理速度）
- 遠超過我們設置的 5 RPS（每秒 5 個請求）
- 大多數請求會收到 429 Too Many Requests 響應

改進建議的 JMeter 配置：

```xml
<ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Rate Limit Test Group" enabled="true">
    <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
    <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
        <boolProp name="LoopController.continue_forever">false</boolProp>
        <intProp name="LoopController.loops">-1</intProp>
    </elementProp>
    <stringProp name="ThreadGroup.num_threads">10</stringProp>
    <stringProp name="ThreadGroup.ramp_up">1</stringProp>
    <boolProp name="ThreadGroup.scheduler">true</boolProp>
    <stringProp name="ThreadGroup.duration">10</stringProp>
    <stringProp name="ThreadGroup.delay"></stringProp>
    <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
</ThreadGroup>
<RandomController guiclass="RandomControlGui" testclass="RandomController" testname="Random Controller" enabled="true">
    <intProp name="RandomController.style">1</intProp>
</RandomController>
<ConstantTimer guiclass="ConstantTimerGui" testclass="ConstantTimer" testname="Constant Timer" enabled="true">
    <stringProp name="ConstantTimer.delay">200</stringProp>
</ConstantTimer>
```

改進說明：
- 添加 `ConstantTimer`，每個請求間隔 200 毫秒
- 這樣可以更準確地模擬接近 5 RPS 的請求頻率
- 增加 `RandomController` 增加一些隨機性

總結：
- 原配置會快速、密集地發送大量請求
- 建議配置能更精確地模擬限流場景
- 可根據實際需求調整 `ConstantTimer` 的延遲時間
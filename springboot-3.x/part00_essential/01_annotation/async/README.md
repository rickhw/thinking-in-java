很好問題！而且這個需求**實務上很常見**，像是你要做監控（比如 Grafana, Actuator）也會需要。

在 Spring Boot 裡，如果你用的是 `ThreadPoolTaskExecutor`（底層用 `java.util.concurrent.ThreadPoolExecutor`），
**可以在 Runtime 拿到以下資訊**：

| 你想要的資訊         | 怎麼拿                      |
|:---------------------|:----------------------------|
| Core pool size        | `executor.getCorePoolSize()` |
| Max pool size         | `executor.getMaxPoolSize()`  |
| Current active count  | `executor.getActiveCount()`  |
| Queue size (使用中)   | `executor.getThreadPoolExecutor().getQueue().size()` |
| Queue capacity (總量) | 需要自己知道（從設定或外部傳入）|

---

## 實作範例

假設你的 `AsyncConfig` 裡有定義這樣的 `ThreadPoolTaskExecutor`：

### `AsyncConfig.java`
```java
package com.example.asyncdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("async-thread-");
        executor.initialize();
        return executor;
    }
}
```

---

### 監控 API
可以弄一支 REST API，來查 ThreadPool 的狀況：

#### `ThreadPoolStatusController.java`
```java
package com.example.asyncdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ThreadPoolStatusController {

    @Autowired
    private ThreadPoolTaskExecutor asyncExecutor;

    @GetMapping("/threadpool/status")
    public Map<String, Object> getThreadPoolStatus() {
        return Map.of(
            "corePoolSize", asyncExecutor.getCorePoolSize(),
            "maxPoolSize", asyncExecutor.getMaxPoolSize(),
            "activeCount", asyncExecutor.getActiveCount(),
            "poolSize", asyncExecutor.getPoolSize(),
            "queueSize", asyncExecutor.getThreadPoolExecutor().getQueue().size()
        );
    }
}
```

---
## 呼叫結果範例
你打：

```bash
curl http://localhost:8080/threadpool/status
```

會回傳類似這樣：

```json
{
  "corePoolSize": 2,
  "maxPoolSize": 5,
  "activeCount": 3,
  "poolSize": 5,
  "queueSize": 7
}
```

---
## 注意
- `queueSize` 是指現在排在 queue 等待執行的任務數量。
- `queueCapacity`（queue 總容量）**ThreadPoolExecutor 不提供直接 API 查詢**，通常是你自己設定的時候記下來。
- 如果你要一併提供 queue capacity，可以在 `AsyncConfig` 裡記錄下來，或是包成一個自己的類別。

---

要不要順便也教你怎麼把這些 metrics 自動掛到 Spring Boot Actuator？  
（這樣可以直接 Grafana / Prometheus 抓，不用自己開 API）  
要的話我可以一併教你～要不要？🎯
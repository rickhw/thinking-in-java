
## Prompt

這個 api 的上游，會透過 http header 的方式帶入一些值，像是 tenantId, appName, roleName，我想把這些值封裝成一個 叫做 TenantContext 的 class. 然後當 API Server 收到的時候，會先把這個 object 建立起來，然後往下帶，不管在 controller, service 都可以使用。

請提供我滿足上述需求的範例。


## ChatGPT

為了實現您的需求，可以使用 **Spring 的 `HandlerInterceptor`** 和 **ThreadLocal** 技術來實現 `TenantContext` 的傳遞機制。以下是完整的實現範例：

---

### 1. 新增 `TenantContext` 類別
`TenantContext` 將用來存放 `tenantId`、`appName` 和 `roleName` 等資料。

```java
package com.example.tenant.context;

public class TenantContext {
    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private String tenantId;
    private String appName;
    private String roleName;

    public static TenantContext getCurrentContext() {
        return CONTEXT.get();
    }

    public static void setCurrentContext(TenantContext tenantContext) {
        CONTEXT.set(tenantContext);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
```

---

### 2. 實現 `TenantContextInterceptor`
使用 `HandlerInterceptor` 從 HTTP Header 中讀取資料並建立 `TenantContext`。

```java
package com.example.tenant.interceptor;

import com.example.tenant.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TenantContext tenantContext = new TenantContext();
        tenantContext.setTenantId(request.getHeader("tenantId"));
        tenantContext.setAppName(request.getHeader("appName"));
        tenantContext.setRoleName(request.getHeader("roleName"));
        TenantContext.setCurrentContext(tenantContext);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
```

---

### 3. 註冊 Interceptor
在 `WebMvcConfigurer` 中註冊 `TenantContextInterceptor`。

```java
package com.example.tenant.config;

import com.example.tenant.interceptor.TenantContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantContextInterceptor tenantContextInterceptor;

    public WebConfig(TenantContextInterceptor tenantContextInterceptor) {
        this.tenantContextInterceptor = tenantContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantContextInterceptor);
    }
}
```

---

### 4. 在 Controller 和 Service 中使用 `TenantContext`

#### **在 Controller 使用**
```java
package com.example.tenant.controller;

import com.example.tenant.context.TenantContext;
import com.example.tenant.model.Tenant;
import com.example.tenant.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("TenantId: " + context.getTenantId());
        System.out.println("AppName: " + context.getAppName());
        System.out.println("RoleName: " + context.getRoleName());

        return ResponseEntity.ok(tenantService.getAllTenants(null));
    }
}
```

#### **在 Service 使用**
```java
package com.example.tenant.service;

import com.example.tenant.context.TenantContext;
import com.example.tenant.model.Tenant;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TenantService {

    private final Map<Long, Tenant> tenantStore = new HashMap<>();
    private long idCounter = 1;

    public List<Tenant> getAllTenants(String name) {
        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("Service - TenantId: " + context.getTenantId());
        System.out.println("Service - AppName: " + context.getAppName());
        System.out.println("Service - RoleName: " + context.getRoleName());

        if (name != null) {
            return tenantStore.values().stream()
                    .filter(tenant -> tenant.getName().equalsIgnoreCase(name))
                    .toList();
        }
        return new ArrayList<>(tenantStore.values());
    }

    // Other CRUD methods...
}
```

---

### 5. 測試範例

#### **測試 Request**
```http
GET /api/tenants
Host: localhost:8080
tenantId: tenant-123
appName: my-app
roleName: admin
```

#### **日誌輸出**
當請求抵達時，`TenantContext` 會打印如下內容：
```
TenantId: tenant-123
AppName: my-app
RoleName: admin
Service - TenantId: tenant-123
Service - AppName: my-app
Service - RoleName: admin
```

---

### 6. 注意事項

1. **ThreadLocal 的清理**  
   確保在請求完成後，使用 `TenantContext.clear()` 清理 `ThreadLocal`，防止記憶體洩漏。
   
2. **多線程注意事項**  
   如果使用異步操作（例如 @Async 或多線程），需要顯式傳遞 `TenantContext`，因為 `ThreadLocal` 僅在同一線程中有效。

3. **Header 驗證**  
   可以擴展 `TenantContextInterceptor` 增加檢查邏輯，確保 Header 資料完整性。


---

## Prompt

你的建議看起來不錯，這個寫法能確保是每個 request 的範圍，而且是 thread-safe？


## ChatGPT

是的，這個設計是 **thread-safe** 的，並且確保了 `TenantContext` 的範圍僅限於每個請求。以下是詳細分析：

---

### **1. `ThreadLocal` 的特性**
`ThreadLocal` 是 Java 提供的一種線程本地變量機制：
- 每個線程都會擁有一份獨立的 `ThreadLocal` 副本。
- 同一個 `ThreadLocal` 在不同線程之間互不干擾。
- 當線程結束時，`ThreadLocal` 中的變量也會隨著線程一同銷毀。

這意味著，即使是高併發環境下，每個請求（對應到不同的工作線程）都有自己的 `TenantContext`，彼此不會干擾，從而實現了 **thread-safe**。

---

### **2. 範圍限定**
在設計中，`TenantContext` 是通過以下流程進行使用的：
1. **`Interceptor` 的 `preHandle` 方法**：  
   在每個請求進入時執行，從 HTTP Header 中解析出必要的資訊並初始化 `TenantContext`。

2. **`Interceptor` 的 `afterCompletion` 方法**：  
   在請求完成後（無論成功還是異常）清理 `ThreadLocal`，確保該線程上的 `TenantContext` 不會影響下一個請求。

這種設計使得 `TenantContext` 的作用域明確限定在單一請求範圍內。

---

### **3. 高併發的安全性**
即使有多個請求同時處理，由於每個請求都由不同的線程執行，而 `ThreadLocal` 提供線程隔離性，因此可以確保：
- 不同請求的 `TenantContext` 相互獨立。
- 不存在數據競爭的問題。

---

### **4. 問題及擴展**
雖然這種方式在大多數同步請求場景下是安全的，但以下情況需要特別考慮：

#### **異步操作**
如果在應用中使用了異步任務（例如 Spring 的 `@Async`、`CompletableFuture` 或其他線程池操作），則 `ThreadLocal` 中的資料不會自動傳遞到新線程中。需要手動處理：
- 類似於 **MDC（Mapped Diagnostic Context）** 的模式。
- 將 `TenantContext` 顯式傳遞給新的線程。

以下是異步情況下的處理範例：

```java
import java.util.concurrent.Callable;

public class TenantContextWrapper {

    public static <T> Callable<T> wrap(Callable<T> task) {
        TenantContext tenantContext = TenantContext.getCurrentContext();
        return () -> {
            try {
                TenantContext.setCurrentContext(tenantContext);
                return task.call();
            } finally {
                TenantContext.clear();
            }
        };
    }
}
```

在需要異步執行的地方，包裝任務即可：

```java
CompletableFuture.supplyAsync(
    TenantContextWrapper.wrap(() -> {
        // 異步操作
        System.out.println("TenantId: " + TenantContext.getCurrentContext().getTenantId());
        return "Done";
    })
);
```

---

### **5. 測試驗證**
可以通過以下方式驗證範例設計的安全性：

#### **併發測試**
模擬多線程請求，檢查 `TenantContext` 是否被錯誤覆蓋。

```java
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TenantContextTest {

    @Test
    void testThreadLocalIsolation() {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        Runnable task1 = () -> {
            TenantContext tenantContext = new TenantContext();
            tenantContext.setTenantId("Tenant-1");
            TenantContext.setCurrentContext(tenantContext);

            System.out.println("Task1: " + TenantContext.getCurrentContext().getTenantId());
        };

        Runnable task2 = () -> {
            TenantContext tenantContext = new TenantContext();
            tenantContext.setTenantId("Tenant-2");
            TenantContext.setCurrentContext(tenantContext);

            System.out.println("Task2: " + TenantContext.getCurrentContext().getTenantId());
        };

        executor.submit(task1);
        executor.submit(task2);

        executor.shutdown();
    }
}
```

#### **清理驗證**
模擬多個請求，檢查是否在請求完成後清理了 `ThreadLocal`。

---

### **6. 總結**
- **安全性**：該設計依賴 `ThreadLocal`，確保每個請求的資料隔離，滿足 thread-safe 要求。
- **範圍限定**：`TenantContext` 僅在請求內生效，並在完成後清理，避免污染其他請求。
- **可擴展性**：適用於同步場景，對於異步場景需要額外封裝。

這樣的設計在大多數情況下是業界標準，也適用於高併發的 Spring Boot 應用。
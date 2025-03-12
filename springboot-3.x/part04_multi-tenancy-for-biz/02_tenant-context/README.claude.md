
我來幫您設計一個 thread-safe 的解決方案，使用 ThreadLocal 來存儲 TenantContext。

```java
// TenantContext.java
public class TenantContext {
    private final String tenantId;
    private final String appName;
    private final String roleName;

    private TenantContext(String tenantId, String appName, String roleName) {
        this.tenantId = tenantId;
        this.appName = appName;
        this.roleName = roleName;
    }

    public static TenantContext of(String tenantId, String appName, String roleName) {
        return new TenantContext(tenantId, appName, roleName);
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAppName() {
        return appName;
    }

    public String getRoleName() {
        return roleName;
    }
}

// TenantContextHolder.java
public class TenantContextHolder {
    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    public static void setContext(TenantContext context) {
        CONTEXT.set(context);
    }

    public static TenantContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

// TenantInterceptor.java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    private static final String TENANT_ID_HEADER = "tenantId";
    private static final String APP_NAME_HEADER = "appName";
    private static final String ROLE_NAME_HEADER = "roleName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        String appName = request.getHeader(APP_NAME_HEADER);
        String roleName = request.getHeader(ROLE_NAME_HEADER);

        // 可以在這裡加入驗證邏輯，確保必要的 header 都存在
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId is required");
        }

        TenantContext context = TenantContext.of(tenantId, appName, roleName);
        TenantContextHolder.setContext(context);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        // 清理 ThreadLocal，避免記憶體洩漏
        TenantContextHolder.clear();
    }
}

// WebMvcConfig.java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
               .addPathPatterns("/**")  // 攔截所有請求
               .excludePathPatterns("/health", "/metrics");  // 排除不需要租戶信息的端點
    }
}

// 使用範例 - Controller
@RestController
@RequestMapping("/api")
public class SampleController {
    
    @GetMapping("/example")
    public ResponseEntity<String> example() {
        TenantContext context = TenantContextHolder.getContext();
        String tenantId = context.getTenantId();
        String appName = context.getAppName();
        // 使用租戶信息進行處理
        return ResponseEntity.ok("Processing for tenant: " + tenantId);
    }
}

// 使用範例 - Service
@Service
public class SampleService {
    
    public void doSomething() {
        TenantContext context = TenantContextHolder.getContext();
        String tenantId = context.getTenantId();
        // 使用租戶信息進行業務邏輯處理
    }
}

```

這個實現方案主要包含以下幾個部分：

1. **TenantContext**：
   - 不可變的類，存儲租戶相關信息
   - 使用 builder 模式創建實例
   - 所有字段都是 final 的，確保線程安全

2. **TenantContextHolder**：
   - 使用 ThreadLocal 來存儲 TenantContext
   - 提供設置、獲取和清理上下文的靜態方法
   - ThreadLocal 確保了線程安全性

3. **TenantInterceptor**：
   - 實現 HandlerInterceptor 接口
   - 在請求處理前從 HTTP headers 中提取信息
   - 創建 TenantContext 並存儲到 ThreadLocal 中
   - 在請求完成後清理 ThreadLocal

4. **WebMvcConfig**：
   - 註冊 TenantInterceptor
   - 配置攔截器的路徑規則

使用方式：
1. 當請求進入時，攔截器自動從 headers 中提取信息並創建 TenantContext
2. 在任何 Controller 或 Service 中，都可以通過 `TenantContextHolder.getContext()` 獲取當前租戶信息
3. 請求結束後，攔截器自動清理 ThreadLocal，防止記憶體洩漏

這個實現是線程安全的，因為：
- TenantContext 是不可變的
- 使用 ThreadLocal 確保每個線程都有自己獨立的 context
- 在請求結束時清理 ThreadLocal，避免記憶體洩漏

您要注意的點：
1. 記得在適當的時機清理 ThreadLocal
2. 考慮添加必要的參數驗證
3. 可以根據需求調整排除的路徑
4. 可以添加日誌記錄來追踪租戶信息

需要我解釋任何部分或進行調整嗎？


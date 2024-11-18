
## Prompt 

同樣的 rest api, 接下來我想處理 error handling 部分，我把驗證分成底下三個部分:

1. payload validation: 驗證 payload 是否符合 api spec 的定義，例如 name 只能是 [a-z0-0] 
2. check service quota: 檢查這個請求的資源, 是否符合系統的 quota 宣告 
3. check association: 檢驗 payload 裡如果有 參照, 例如 plan 有分成 a, b, c 三種, 檢查是否在這三種之中        

幫我用 tenant 這個 resource 當作 api, 用 create tenant 為例, tenant 的 input payload 如下:

```json
{
    "name": "rick",
    "plan": "a"
}
```

當使用者輸入的 payload 有問題時，會拋出對應的 exception, 例如 InvalidNameException.

請提供我上述的完整程式，以及錯誤處理的框架.


## Claude

我幫您設計一個完整的錯誤處理框架，包含驗證層次和統一的錯誤處理機制。



```java
// 1. DTO and Entity
@Data
public class CreateTenantRequest {
    @Pattern(regexp = "[a-z0-9]+", message = "Name must contain only lowercase letters and numbers")
    private String name;
    
    @NotNull(message = "Plan is required")
    private String plan;
}

@Entity
public class Tenant {
    @Id
    private String name;
    private String plan;
    // other fields...
}

// 2. Custom Exceptions
public class BaseException extends RuntimeException {
    private final String code;
    private final String message;
    
    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}

public class InvalidNameException extends BaseException {
    public InvalidNameException(String message) {
        super("INVALID_NAME", message);
    }
}

public class InvalidPlanException extends BaseException {
    public InvalidPlanException(String message) {
        super("INVALID_PLAN", message);
    }
}

public class QuotaExceededException extends BaseException {
    public QuotaExceededException(String message) {
        super("QUOTA_EXCEEDED", message);
    }
}

// 3. Validation Service
@Service
@Validated
public class TenantValidationService {
    private static final Set<String> VALID_PLANS = Set.of("a", "b", "c");
    
    public void validatePayload(CreateTenantRequest request) {
        // Additional custom validation beyond @Pattern
        if (!request.getName().matches("[a-z0-9]+")) {
            throw new InvalidNameException("Name contains invalid characters");
        }
    }
    
    public void validateQuota(String tenantId) {
        // Example quota check logic
        long currentTenantCount = getCurrentTenantCount();
        if (currentTenantCount >= 1000) {
            throw new QuotaExceededException("Maximum number of tenants reached");
        }
    }
    
    public void validateAssociations(CreateTenantRequest request) {
        if (!VALID_PLANS.contains(request.getPlan())) {
            throw new InvalidPlanException("Invalid plan selected. Valid plans are: " + 
                String.join(", ", VALID_PLANS));
        }
    }
    
    private long getCurrentTenantCount() {
        // Implementation to get current tenant count
        return 0;
    }
}

// 4. Error Response DTO
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> details;
}

// 5. Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            details.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .details(details)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}

// 6. Controller
@RestController
@RequestMapping("/api/tenants")
@Validated
public class TenantController {
    
    private final TenantValidationService validationService;
    private final TenantService tenantService;
    
    @Autowired
    public TenantController(TenantValidationService validationService,
                          TenantService tenantService) {
        this.validationService = validationService;
        this.tenantService = tenantService;
    }
    
    @PostMapping
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        // 1. Payload Validation
        validationService.validatePayload(request);
        
        // 2. Quota Check
        validationService.validateQuota(request.getName());
        
        // 3. Association Check
        validationService.validateAssociations(request);
        
        // 4. Process the request
        Tenant tenant = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
    }
}

// 7. Service Implementation
@Service
@Transactional
public class TenantService {
    
    private final TenantRepository tenantRepository;
    
    @Autowired
    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }
    
    public Tenant createTenant(CreateTenantRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setPlan(request.getPlan());
        return tenantRepository.save(tenant);
    }
}

```

這個實現包含了完整的錯誤處理框架，主要特點如下：

1. **多層驗證**:
   - Payload validation: 使用 JSR-380 (@Valid) 和自定義驗證
   - Service quota: 檢查系統資源限制
   - Association validation: 檢查關聯數據的有效性

2. **異常體系**:
   - BaseException: 所有自定義異常的基類
   - 特定異常: InvalidNameException, InvalidPlanException, QuotaExceededException

3. **統一的錯誤響應格式**:
```json
{
    "code": "ERROR_CODE",
    "message": "Error message",
    "timestamp": "2024-11-18T10:30:00",
    "path": "/api/tenants",
    "details": {
        "fieldName": "error detail"
    }
}
```

4. **使用示例**:
```bash
# 正確的請求
POST /api/tenants
{
    "name": "rick123",
    "plan": "a"
}

# 錯誤的請求（無效的名稱）
POST /api/tenants
{
    "name": "Rick@123",  # 包含非法字符
    "plan": "a"
}

# 錯誤的請求（無效的計劃）
POST /api/tenants
{
    "name": "rick123",
    "plan": "d"  # 不存在的計劃
}
```

5. **錯誤處理流程**:
   - Controller 層使用 @Valid 進行基本驗證
   - ValidationService 處理複雜的驗證邏輯
   - GlobalExceptionHandler 統一處理所有異常

主要優點：
1. 清晰的驗證層次
2. 統一的錯誤處理機制
3. 可擴展的異常體系
4. 詳細的錯誤信息
5. 符合 RESTful API 最佳實踐

您可以根據需求：
1. 添加更多的驗證規則
2. 擴展錯誤代碼
3. 自定義錯誤響應格式
4. 添加日誌記錄
5. 添加更多的異常類型

需要我解釋任何部分或進行調整嗎？




# Claude
---

在 Spring Boot 的 runtime 過程中，您有多種方式可以獲取 Session 的相關資訊。以下是幾種常用方法：

### 1. 在 Controller 中直接注入 HttpSession

最直接的方式是在 Controller 的方法參數中注入 `HttpSession`：

```java
@RestController
public class SessionController {
    
    @GetMapping("/session-info")
    public Map<String, Object> getSessionInfo(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        
        // 獲取 Session ID
        sessionInfo.put("sessionId", session.getId());
        
        // 獲取 Session 創建時間
        sessionInfo.put("creationTime", new Date(session.getCreationTime()));
        
        // 獲取最後訪問時間
        sessionInfo.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
        
        // 獲取 Session 最大閒置時間（秒）
        sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        // 獲取 Session 是否為新建
        sessionInfo.put("isNew", session.isNew());
        
        // 獲取 Session 中所有屬性
        Enumeration<String> attributeNames = session.getAttributeNames();
        Map<String, Object> attributes = new HashMap<>();
        
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            attributes.put(name, session.getAttribute(name));
        }
        
        sessionInfo.put("attributes", attributes);
        
        return sessionInfo;
    }
}
```

### 2. 使用 HttpServletRequest 獲取 Session

通過 `HttpServletRequest` 也可以獲取 Session：

```java
@RestController
public class SessionController {
    
    @GetMapping("/session-info")
    public Map<String, Object> getSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 獲取 Session 信息，同上...
        // ...
    }
}
```

### 3. 在 Service 層通過 RequestContextHolder 獲取 Session

如果您需要在非 Controller 層的代碼中（如 Service 層）獲取當前的 Session，可以使用 `RequestContextHolder`：

```java
@Service
public class UserService {
    
    public void processUserData() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attrs.getRequest().getSession();
        
        // 獲取或操作 Session
        String username = (String) session.getAttribute("username");
        // ...
    }
}
```

### 4. 使用 SessionRegistry 獲取所有活躍的 Session（當使用 Spring Security 時）

如果您使用了 Spring Security，可以通過配置 `SessionRegistry` 來獲取所有活躍的 Session：

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
```

然後在您的代碼中：

```java
@RestController
public class AdminController {
    
    @Autowired
    private SessionRegistry sessionRegistry;
    
    @GetMapping("/active-sessions")
    public List<Object> getActiveSessions() {
        // 獲取所有活躍的 Session
        List<Object> principals = sessionRegistry.getAllPrincipals();
        
        List<Object> sessions = new ArrayList<>();
        for (Object principal : principals) {
            sessions.addAll(sessionRegistry.getAllSessions(principal, false));
        }
        
        return sessions;
    }
}
```

### 5. 使用 Spring Session API 獲取存儲的 Session 信息

如果您使用了 Spring Session（如 Redis 或 JDBC 存儲），可以直接操作相應的 Repository 來查詢 Session：

```java
@RestController
public class AdminController {
    
    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    
    @GetMapping("/find-sessions")
    public Map<String, ? extends Session> getSessionsByUsername(@RequestParam String username) {
        // 根據用戶名查找相關的所有 Session
        return sessionRepository.findByPrincipalName(username);
    }
    
    @GetMapping("/get-session")
    public ResponseEntity<Object> getSessionById(@RequestParam String sessionId) {
        // 根據 Session ID 查找 Session
        Session session = sessionRepository.findById(sessionId);
        
        if (session != null) {
            Map<String, Object> sessionDetails = new HashMap<>();
            sessionDetails.put("id", session.getId());
            sessionDetails.put("creationTime", session.getCreationTime());
            sessionDetails.put("lastAccessedTime", session.getLastAccessedTime());
            sessionDetails.put("maxInactiveInterval", session.getMaxInactiveInterval());
            sessionDetails.put("expired", session.isExpired());
            
            // 獲取所有屬性
            Map<String, Object> attributes = new HashMap<>();
            for (String name : session.getAttributeNames()) {
                attributes.put(name, session.getAttribute(name));
            }
            sessionDetails.put("attributes", attributes);
            
            return ResponseEntity.ok(sessionDetails);
        }
        
        return ResponseEntity.notFound().build();
    }
}
```

### 6. 通過自定義 Session 事件監聽器

您可以實現 `HttpSessionListener` 接口來監聽 Session 創建和銷毀事件：

```java
@Component
public class SessionListener implements HttpSessionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        logger.info("Session 已創建: ID={}, 創建時間={}", 
                session.getId(), new Date(session.getCreationTime()));
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        logger.info("Session 已銷毀: ID={}", session.getId());
    }
}
```

以上這些方法涵蓋了在 Spring Boot 運行時獲取 Session 信息的多種場景。根據您的具體需求，可以選擇最適合的方式來獲取和管理 Session 信息。
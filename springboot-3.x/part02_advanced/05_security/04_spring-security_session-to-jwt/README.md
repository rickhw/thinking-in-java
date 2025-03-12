# Prompt

這個 spring security 我已經大概可以運作，幫我整理一些資訊給我，包含以下：

1. 使用者登入的 session 資訊有哪些？應該如何取得？
2. 承上，我想調整 session 的設定，應該如何配置？
3. 承上，這些設定能不能動態調整？如果可以，那應該怎麼做？


---

# ChatGPT


Spring Security 中的 **Session** 主要用於管理使用者登入狀態和授權資訊。以下是你的問題整理後的解答：  

---

## **1. 使用者登入的 Session 資訊有哪些？應該如何取得？**
當使用者登入後，Spring Security 會在 `SecurityContext` 中維護該使用者的 Session 資訊，常見的資訊包括：

| **資訊類別** | **如何取得** | **說明** |
|-------------|------------|----------|
| **使用者詳細資訊 (UserDetails)** | `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` | 包含使用者名稱、角色、帳號狀態等 |
| **授權 (Authorities, Roles)** | `SecurityContextHolder.getContext().getAuthentication().getAuthorities()` | 使用者擁有的權限/角色 |
| **Session ID** | `request.getSession().getId()` | 取得目前 Session ID |
| **Session 內容** | `request.getSession().getAttribute("key")` | 存取 Session 內的自訂屬性 |
| **登入時間** | `request.getSession().getCreationTime()` | 取得 Session 創建時間 |
| **最後存取時間** | `request.getSession().getLastAccessedTime()` | 取得最後存取時間 |
| **Session Timeout 設定** | `request.getSession().getMaxInactiveInterval()` | 取得 Session 逾時設定 (秒) |

**程式範例：**
```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    public static void printSessionInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                System.out.println("使用者名稱: " + userDetails.getUsername());
                System.out.println("使用者角色: " + userDetails.getAuthorities());
            }
        }

        HttpSession session = request.getSession(false); // false: 如果沒有 Session 就回傳 null
        if (session != null) {
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Timeout: " + session.getMaxInactiveInterval());
            System.out.println("Session 創建時間: " + session.getCreationTime());
            System.out.println("Session 最後存取時間: " + session.getLastAccessedTime());
        }
    }
}
```

---

## **2. 如何調整 Session 設定？**
Spring Security 提供 `SessionManagementConfigurer` 來控制 Session 行為，常見的設定有：

### **a. Session 逾時 (Timeout)**
在 `application.yml` 設定：
```yaml
server:
  servlet:
    session:
      timeout: 30m  # 30 分鐘
```
或者在 Java 配置：
```java
@Bean
public ServletListenerRegistrationBean<HttpSessionListener> sessionListener() {
    return new ServletListenerRegistrationBean<>(new HttpSessionListener() {
        @Override
        public void sessionCreated(HttpSessionEvent se) {
            se.getSession().setMaxInactiveInterval(1800); // 30 分鐘
        }
    });
}
```

---

### **b. 控制 Session 策略**
透過 `HttpSecurity` 設定 Session 管理策略：
```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 需要時才建立 Session
                .maximumSessions(1) // 限制每個使用者最多只能有 1 個 Session
                .expiredUrl("/session-expired") // Session 過期時導向的 URL
            );
        return http.build();
    }
}
```

**SessionCreationPolicy 選項：**
| **選項** | **行為** |
|----------|---------|
| `ALWAYS` | 每次請求都建立新的 Session |
| `IF_REQUIRED` | 預設值，必要時才建立 Session |
| `NEVER` | 不建立 Session，但會使用現有 Session |
| `STATELESS` | 完全不使用 Session (通常用於 REST API) |

---

## **3. 這些設定能不能動態調整？如果可以，那應該怎麼做？**
可以透過 **程式動態修改** 或 **Spring Actuator 來熱更新**，以下是不同方式的處理方法：

### **a. 動態修改 Session Timeout**
如果你想在系統運行時動態變更 Session Timeout，可以在 Controller 設定：
```java
@RestController
@RequestMapping("/session")
public class SessionController {

    @PostMapping("/update-timeout")
    public ResponseEntity<String> updateSessionTimeout(HttpServletRequest request, @RequestParam int timeout) {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(timeout);
        return ResponseEntity.ok("Session Timeout 已更新為 " + timeout + " 秒");
    }
}
```
📌 **測試方式：**
```bash
curl -X POST "http://localhost:8080/session/update-timeout?timeout=600"
```
這樣 Session Timeout 會立即更新為 600 秒 (10 分鐘)。

---

### **b. 動態修改 CORS、最大 Session 數等設定**
如果想動態調整 Spring Security 的 `SecurityFilterChain` 配置，則需要自訂 Bean，或者使用 **Spring Actuator** 搭配 `Environment` 動態變更。

**範例：Spring Actuator 動態調整**
1. **開啟 Actuator**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: "env"
   ```

2. **動態更新屬性**
   ```bash
   curl -X POST "http://localhost:8080/actuator/env" -H "Content-Type: application/json" -d '{"name": "server.servlet.session.timeout", "value": "1800"}'
   ```
   這樣就可以在不重啟 Spring Boot 的情況下，動態修改 Session Timeout。

---

## **總結**
| **需求** | **解決方式** |
|---------|------------|
| 取得使用者 Session 資訊 | `SecurityContextHolder.getContext().getAuthentication()` |
| 取得 Session ID、逾時時間 | `request.getSession().getId()`、`getMaxInactiveInterval()` |
| 設定 Session 逾時 | `application.yml` 或 `HttpSessionListener` |
| 限制 Session 數量 | `sessionManagement().maximumSessions(1)` |
| 動態調整 Session Timeout | `session.setMaxInactiveInterval(timeout)` |
| 熱更新 Security 配置 | `Spring Actuator` 修改 `env` 變數 |

這樣應該能滿足你的需求！如果有其他細節需要調整，歡迎繼續討論 🚀
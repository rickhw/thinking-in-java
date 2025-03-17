你的 `SecurityConfig.java` 設定中，有這段程式碼：

```java
.sessionManagement(session -> session
    .maximumSessions(1) // 限制每個帳號只能有 1 個 session
    .maxSessionsPreventsLogin(true) // 避免多次登入（新的登入請求會被拒絕）
)
```

這代表：
1. **`maximumSessions(1)`**：同一個使用者 **最多** 只能有 **1 個有效的 session**。
2. **`maxSessionsPreventsLogin(true)`**：如果使用者已經有 **1 個 session**，新的登入 **將被拒絕**。

---

### **可能的問題**
1. 你已經在 **其他瀏覽器、裝置，或同一瀏覽器的不同分頁** 登入過這個帳號。
2. 你的 Session 尚未過期，即使你關閉瀏覽器，Session 仍然有效。
3. 伺服器未正確清除舊的 Session，導致新的登入請求失敗。

---

### **解決方法**
#### **方法 1：允許新的登入覆蓋舊的 Session**
目前 `maxSessionsPreventsLogin(true)` 會 **阻止新登入**，你可以改成 `false`，讓新登入 **取代舊的 Session**：
```java
.sessionManagement(session -> session
    .maximumSessions(1)
    .maxSessionsPreventsLogin(false) // 允許新登入，會踢掉舊的 session
)
```
這樣，每次新登入時，舊的 Session 會自動失效，確保最新的登入有效。

---

#### **方法 2：確認登出時正確銷毀 Session**
如果你期望每次登出後都能正常重新登入，請確認 `logout()` 設定已正確清除 Session：
```java
.logout(logout -> logout
    .logoutUrl("/perform-logout")
    .logoutSuccessUrl("/login?logout=true")
    .invalidateHttpSession(true) // 使 HTTP Session 失效
    .clearAuthentication(true) // 清除認證資訊
    .deleteCookies("JSESSIONID") // 刪除 Session Cookie
    .permitAll()
)
```
你可以試試 **手動登出** (`/perform-logout`)，然後再登入，確保 Session 被正確清除。

---

#### **方法 3：減少 Session 存活時間**
如果 Session 存活時間過長，舊的 Session 可能仍然有效，影響新登入。你可以在 `application.yaml` 中降低 Session 的存活時間：
```yaml
server:
  servlet:
    session:
      timeout: 10m # 設定 Session 過期時間為 10 分鐘
```
這樣，舊的 Session 會在 10 分鐘內過期，避免影響新登入。

---

#### **方法 4：手動清除 Session**
如果問題仍然存在，你可以在 Redis（如果有用 Redis 存 Session）或應用程式內部手動清除：
```java
@Autowired
private SessionRegistry sessionRegistry;

public void expireUserSessions(String username) {
    sessionRegistry.getAllSessions(username, false)
        .forEach(SessionInformation::expireNow);
}
```
或者在 **資料庫**（如果 `spring-session-jdbc`）中，刪除該使用者的 Session 記錄。

---

### **結論**
- **推薦解法：**
  1. 改成 `maxSessionsPreventsLogin(false)`，允許新的登入覆蓋舊的 Session。
  2. 確保登出 (`/perform-logout`) 後 Session 被清除 (`invalidateHttpSession(true)` + `deleteCookies("JSESSIONID")`)。
  3. 如果使用 Redis 或 JDBC 存 Session，檢查是否有未過期的舊 Session。

試試這些方法，看看問題是否解決！ 🚀


在 **Spring Boot 3.2** 中，`spring.session` 相關的設定主要用於管理 **Spring Session**，提供分佈式會話存儲的能力，支援多種存儲方式（如 Redis、JDBC、Hazelcast、MongoDB）。這裡介紹常見的 `application.yaml` 設定及其用途。

---

## **1. 通用 `spring.session` 設定**
```yaml
spring:
  session:
    store-type: redis  # 指定 session 存儲類型，可選 none, redis, jdbc, hazelcast
    timeout: 30m       # 會話超時時間（全局設定，預設 30 分鐘）
    tracking-mode: cookie  # 設定會話追蹤模式，可選 cookie, url, session
    cookie:
      name: SESSIONID  # 設定 Session Cookie 名稱
      http-only: true  # 限制 Cookie 只能透過 HTTP 存取，防止 JavaScript 讀取
      secure: false    # 是否僅限 HTTPS 傳輸
      same-site: lax   # 控制 SameSite 設定，可選 none, lax, strict
```

---

## **2. 使用 Redis 存儲 Session**
若要使用 **Redis** 來存儲 `spring.session`，可使用以下設定：
```yaml
spring:
  session:
    store-type: redis  # 指定 Redis 作為 Session 存儲
    redis:
      namespace: spring:session  # Redis 存儲 session 的 key 前綴
      flush-mode: on_save        # session 變更時才同步到 Redis
      save-mode: on_set_attribute # 只同步被修改的 attribute，可選 on_set_attribute, on_get_attribute, always
```
**相關依賴：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```
---

## **3. 使用 JDBC 存儲 Session**
若要將 Session 存入 **MySQL / PostgreSQL**：
```yaml
spring:
  session:
    store-type: jdbc  # 指定 JDBC 作為 Session 存儲
    jdbc:
      initialize-schema: always  # 自動建立 session 資料表
```
**相關依賴：**
```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-jdbc</artifactId>
</dependency>
```
**建立資料表（MySQL 範例）：**
```sql
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    PRIMARY KEY (PRIMARY_ID)
);
```
---

## **4. 設定 `@EnableRedisHttpSession`**
如果使用 **Redis** 存儲 Session，則需要在 Spring Boot **Configuration** 類別加上：
```java
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
}
```
這樣 Spring Boot 會自動使用 Redis 來存取 `HttpSession`。

---

## **5. 自訂 Session 銷毀邏輯**
Spring Boot 允許自訂 session 銷毀事件：
```java
@Component
public class SessionDestroyedListener implements ApplicationListener<SessionDestroyedEvent> {
    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        System.out.println("Session destroyed: " + event.getId());
    }
}
```

---

## **6. Session 共享與 Sticky Session**
當應用部署在多個節點時，可以透過 **Redis** 來確保 Session 共享：
```yaml
server:
  servlet:
    session:
      persistent: true  # 允許 session 持久化
  session:
    timeout: 30m  # 全局 session 超時時間
```
如果 Session 需要綁定到特定的應用節點（**Sticky Session**），可確保 **負載均衡器** 設定基於 Cookie 進行流量導向，例如 Nginx 的 `ip_hash` 或 AWS ALB 的 `sticky sessions`。

---

## **7. Spring Security 整合 Session**
當 Spring Boot 整合 Spring Security 時，可以使用 `session-management` 來限制 Session：
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 需要時才創建 session
                .maximumSessions(1) // 限制每個使用者僅有一個有效 session
                .maxSessionsPreventsLogin(true) // 避免多次登入
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
```
這樣可以防止一個帳號在多個裝置上同時登入。

---

## **結論**
Spring Boot 3.2 提供了強大的 `spring.session` 設定，支援 **Redis、JDBC、Hazelcast** 等存儲方式，並可透過 `application.yaml` 設定：
- `timeout` 控制 session 過期時間
- `store-type` 決定 session 存儲方式
- `cookie` 設定 HTTP Cookie
- `flush-mode` & `save-mode` 優化 Redis 存取行為
- 可整合 Spring Security 限制 session 數量

如果你的系統是 **分散式架構**，建議使用 **Redis** 作為 Session 存儲，以確保多節點同步及高效能存取。


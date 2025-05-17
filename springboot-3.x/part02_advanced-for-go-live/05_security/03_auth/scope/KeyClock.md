好的，以下是如何使用 **Keycloak 產出帶有 `scope` 權限的 OAuth2 Token**，並整合到你先前的 Spring Boot 3.2 + Java 17 OAuth2 Resource Server 範例中。

---

## ✅ 步驟總覽

1. 安裝與啟動 Keycloak（Docker 版）
2. 設定 Realm、Client、User 與 Scope
3. 發行 Access Token 並包含 scope 權限
4. Spring Boot 中整合 Keycloak issuer-uri

---

## 🐳 1. Keycloak 安裝（Docker 版）

```bash
docker run -d --name keycloak \
  -p 8081:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0.3 \
  start-dev
```

瀏覽 [http://localhost:8081](http://localhost:8081) 登入：

* 使用者帳號：`admin`
* 密碼：`admin`

---

## ⚙️ 2. 設定 Keycloak

### 🔸 建立 Realm

* 名稱：`demo-realm`

### 🔸 建立 Client

* 名稱：`springboot-client`
* Client Type：`OpenID Connect`
* Access Type：`confidential`
* 記得啟用 `Client Authentication`
* `Valid Redirect URIs` 可設為 `http://localhost:8080/*`
* 儲存後請記下 `Client Secret`

### 🔸 建立角色當作 scope

在左側選單 → `Realm Roles` → 建立：

* 名稱：`hello.read`
* 名稱：`hello.write`

這些角色就是我們之後會當作 scope 權限。

### 🔸 建立使用者

建立一個使用者，並：

* 設定密碼
* 指派剛剛的角色給這個使用者（在 "Role Mappings" 分頁）

---

## 🔐 3. 測試產生 token（使用 client credentials）

### 取得 token：

```bash
curl -X POST http://localhost:8081/realms/demo-realm/protocol/openid-connect/token \
  -d "client_id=springboot-client" \
  -d "client_secret=TTKaiKDIa6a5vQMYDvAbGd0jlqHElPDS" \
  -d "username=testuser" \
  -d "password=testpass" \
  -d "grant_type=password"
```

你會得到像這樣的 `access_token`：

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6...",
  "expires_in": 300,
  "scope": "hello.read hello.write",
  ...
}
```

---

## 🧩 4. Spring Boot 整合 issuer-uri

修改 `application.yml`：

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8081/realms/demo-realm
```

---

## ✅ 最終測試

使用取得的 token 呼叫 API：

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8080/api/hello
```

如你有設定 `hasAuthority("SCOPE_hello.read")`，只有具備 `hello.read` scope 的 token 才可呼叫成功。

---

需要我幫你：

* 提供 Keycloak 匯出設定？
* 幫你寫一個自動建立 Realm/Client/Role 的 CLI？
* 加入 Spring Authorization Server 自建授權中心？

你想自己 host Keycloak，還是打算用 cloud 服務（如 Auth0、Okta）也可以討論。

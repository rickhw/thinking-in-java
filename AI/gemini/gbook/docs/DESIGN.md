### 2. 設計文件 (Design Document)

**2.1. 系統架構 (System Architecture)**
本系統採用標準的客戶端-伺服器架構：
1.  **前端 (Client):** React 應用程式，運行在使用者瀏覽器中，負責 UI 渲染與使用者互動。
2.  **後端 (Server):** Spring Boot 應用程式，提供 RESTful API 供前端呼叫，處理所有業務邏輯與資料庫操作。
3.  **資料庫 (Database):** MySQL 資料庫，用於持久化儲存使用者和文章資料。

**2.2. 資料庫設計 (Database Schema)**
初期規劃兩張核心資料表：

*   **`users`**
    *   `id` (BIGINT, Primary Key, Auto Increment)
    *   `google_id` (VARCHAR, Unique, Not Null) - 從 Google OAuth 取得的唯一識別碼
    *   `email` (VARCHAR, Unique, Not Null)
    *   `name` (VARCHAR, Not Null)
    *   `profile_image_url` (VARCHAR)
    *   `created_at` (TIMESTAMP)
    *   `updated_at` (TIMESTAMP)

*   **`posts`**
    *   `id` (BIGINT, Primary Key, Auto Increment)
    *   `user_id` (BIGINT, Foreign Key to `users.id`, Not Null)
    *   `content` (TEXT, Not Null)
    *   `status` (VARCHAR, e.g., 'PUBLISHED', 'DELETED') - 用於軟刪除
    *   `created_at` (TIMESTAMP)
    *   `updated_at` (TIMESTAMP)

**2.3. API 規格 (API Specification)**
所有 API 的基礎路徑為 `/api`。認證方式採用 `Bearer Token` (JWT)。

**認證 API (Authentication)**

*   **`GET /api/auth/google/callback`**
    *   **說明:** Google OAuth 2.0 的回呼端點。後端接收 Google 返回的 code，換取 access token，取得使用者資訊。如果使用者是第一次登入，則建立新帳號；否則直接登入。成功後，回傳系統自定義的 JWT。
    *   **請求:** Query Parameters (`code`, `state`) 由 Google 導向。
    *   **成功回應 (`200 OK`):**
        ```json
        {
          "token": "your_jwt_token_here",
          "user": {
            "id": 1,
            "name": "John Doe",
            "email": "john.doe@example.com",
            "profile_image_url": "url_to_image"
          }
        }
        ```

**文章 API (Posts)**

*   **`POST /api/posts` (非同步)**
    *   **說明:** 建立一篇新文章。
    *   **認證:** 需要 JWT。
    *   **請求 Body:**
        ```json
        {
          "content": "這是我的第一篇貼文！"
        }
        ```
    *   **成功回應 (`202 Accepted`):** 立即回應，表示請求已被接受並正在處理。
        ```json
        {
          "taskId": "some-unique-task-id",
          "status": "PENDING",
          "message": "Your post is being processed."
        }
        ```

*   **`PUT /api/posts/{postId}` (非同步)**
    *   **說明:** 修改一篇已存在的文章。使用者只能修改自己的文章。
    *   **認證:** 需要 JWT。
    *   **請求 Body:**
        ```json
        {
          "content": "我更新了我的貼文內容。"
        }
        ```
    *   **成功回應 (`202 Accepted`):**
        ```json
        {
          "taskId": "another-unique-task-id",
          "status": "PENDING",
          "message": "Your post update is being processed."
        }
        ```
    *   **錯誤回應 (`403 Forbidden`):** 當使用者嘗試修改不屬於自己的文章時。
    *   **錯誤回應 (`404 Not Found`):** 當文章不存在時。

*   **`DELETE /api/posts/{postId}` (非同步)**
    *   **說明:** 刪除一篇文章。建議使用軟刪除 (soft delete)。
    *   **認證:** 需要 JWT。
    *   **成功回應 (`202 Accepted`):**
        ```json
        {
          "taskId": "delete-task-id",
          "status": "PENDING",
          "message": "Your post deletion is being processed."
        }
        ```
    *   **錯誤回應 (`403 Forbidden`, `404 Not Found`)**

*   **`GET /api/posts`**
    *   **說明:** 取得最新的公開文章列表（時間軸）。可支援分頁。
    *   **認證:** 可選。
    *   **Query Parameters:** `?page=0&size=20`
    *   **成功回應 (`200 OK`):**
        ```json
        {
          "content": [
            {
              "id": 101,
              "content": "...",
              "author": { "id": 2, "name": "Jane Smith" },
              "createdAt": "2025-07-19T10:00:00Z"
            }
          ],
          "page": 0,
          "size": 20,
          "totalPages": 5
        }
        ```

**使用者 API (Users)**

*   **`GET /api/users/me/posts`**
    *   **說明:** 取得當前登入使用者的所有文章。
    *   **認證:** 需要 JWT。
    *   **成功回應 (`200 OK`):** 回應格式同 `GET /api/posts`。

*   **`GET /api/users/{userId}/posts`**
    *   **說明:** 取得指定使用者的所有文章。
    *   **認證:** 可選。
    *   **成功回應 (`200 OK`):** 回應格式同 `GET /api/posts`。

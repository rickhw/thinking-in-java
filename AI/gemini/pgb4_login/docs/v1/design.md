# 設計文件 (Design Document)

## 1. 系統架構 (System Architecture)

本專案採用經典的客戶端-伺服器（Client-Server）架構。

-   **前端 (Client):**
    -   使用 React 開發的單頁應用（SPA）。
    -   負責使用者介面、互動邏輯。
    -   透過 HTTP 請求呼叫後端 RESTful API 來獲取和提交資料。

-   **後端 (Backend):** (位於 `backend/` 目錄)
    -   使用 Spring Boot 開發的應用程式。
    -   負責業務邏輯、資料庫存取、以及提供 RESTful API。
    -   對於寫入操作（建立、更新、刪除），後端 API 會立即回傳 `202 Accepted`，並將任務放入非同步處理佇列（例如使用 Spring 的 `@Async` 功能），由背景執行緒完成資料庫操作。

-   **資料庫 (Database):**
    -   使用 MySQL 儲存所有應用資料，主要是使用者和文章資訊。

## 2. 資料庫設計 (Database Schema)

系統包含三個核心資料表：`users`、`messages` 和 `tasks`。

**`users` 資料表**

| 欄位名稱 (Column) | 資料型別 (Type)    | 描述 (Description)                     |
| ----------------- | ------------------ | -------------------------------------- |
| `id`              | `BIGINT`           | 主鍵 (Primary Key), 自動增長 (Auto-Increment) |
| `username`        | `VARCHAR(255)`     | 用戶名，唯一                           |
| `password`        | `VARCHAR(255)`     | 密碼（實際應用中應加密存儲）           |
| `email`           | `VARCHAR(255)`     | 電子郵件地址                           |

**`messages` 資料表**

| 欄位名稱 (Column) | 資料型別 (Type)    | 描述 (Description)                     |
| ----------------- | ------------------ | -------------------------------------- |
| `id`              | `BIGINT`           | 主鍵 (Primary Key), 自動增長 (Auto-Increment) |
| `user_id`         | `VARCHAR(255)`     | 發表訊息的使用者 ID                    |
| `content`         | `TEXT`             | 訊息內容                               |
| `created_at`      | `TIMESTAMP`        | 建立時間，預設為當前時間               |
| `updated_at`      | `TIMESTAMP`        | 最後更新時間                           |

**`tasks` 資料表**

| 欄位名稱 (Column) | 資料型別 (Type)    | 描述 (Description)                     |
| ----------------- | ------------------ | -------------------------------------- |
| `id`              | `VARCHAR(255)`     | 任務 ID，主鍵                          |
| `status`          | `VARCHAR(50)`      | 任務狀態 (PENDING, COMPLETED, FAILED)  |
| `result`          | `TEXT`             | 任務結果（可選）                       |
| `error`           | `TEXT`             | 錯誤訊息（可選）                       |
| `created_at`      | `TIMESTAMP`        | 任務建立時間                           |
| `updated_at`      | `TIMESTAMP`        | 任務更新時間                           |

## 3. API 設計 (OpenAPI 3.0.0 Spec)

```yaml
openapi: 3.0.0
info:
  title: "Twitter-like Message Board API"
  description: "一個簡易留言板的 RESTful API，提供非同步的訊息操作功能。"
  version: "1.0.0"
servers:
  - url: "/api/v1"
    description: "API V1"

paths:
  /users/register:
    post:
      summary: "用戶註冊"
      description: "註冊新用戶帳號"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserRegistration'
      responses:
        '200':
          description: "註冊成功"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '400':
          description: "註冊資料錯誤"

  /users/login:
    post:
      summary: "用戶登入"
      description: "用戶登入驗證"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: "登入成功"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LoginResponse'
        '401':
          description: "登入失敗"

  /users/{id}:
    get:
      summary: "取得用戶資料"
      parameters:
        - name: "id"
          in: "path"
          required: true
          schema:
            type: "integer"
            format: "int64"
      responses:
        '200':
          description: "成功取得用戶資料"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: "找不到用戶"
    put:
      summary: "更新用戶資料"
      parameters:
        - name: "id"
          in: "path"
          required: true
          schema:
            type: "integer"
            format: "int64"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserUpdate'
      responses:
        '200':
          description: "更新成功"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: "找不到用戶"

  /tasks/{taskId}:
    get:
      summary: "查詢任務狀態"
      parameters:
        - name: "taskId"
          in: "path"
          required: true
          schema:
            type: "string"
      responses:
        '200':
          description: "成功取得任務狀態"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Task'
        '404':
          description: "找不到任務"

paths:
  /messages:
    get:
      summary: "取得所有訊息 (分頁，按創建時間倒序)"
      description: "獲取所有訊息，按創建時間倒序排列（最新的在前面）"
      parameters:
        - name: "page"
          in: "query"
          required: false
          schema:
            type: "integer"
            format: "int32"
            default: 0
          description: "頁碼 (從 0 開始)"
        - name: "size"
          in: "query"
          required: false
          schema:
            type: "integer"
            format: "int32"
            default: 10
          description: "每頁數量"
      responses:
        '200':
          description: "成功取得訊息列表，按創建時間倒序排列。"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessagePage'
    post:
      summary: "建立新訊息 (非同步)"
      description: "接收訊息資料，立即返回並在背景非同步處理建立。"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/NewMessage'
      responses:
        '202':
          description: "請求已接受，正在處理中。"
        '400':
          description: "請求格式錯誤。"

  /users/{userId}/messages:
    get:
      summary: "取得特定使用者的所有訊息 (分頁，按創建時間倒序)"
      description: "獲取特定用戶的所有訊息，按創建時間倒序排列（最新的在前面）"
      parameters:
        - name: "userId"
          in: "path"
          required: true
          schema:
            type: "string"
        - name: "page"
          in: "query"
          required: false
          schema:
            type: "integer"
            format: "int32"
            default: 0
          description: "頁碼 (從 0 開始)"
        - name: "size"
          in: "query"
          required: false
          schema:
            type: "integer"
            format: "int32"
            default: 10
          description: "每頁數量"
      responses:
        '200':
          description: "成功取得訊息列表，按創建時間倒序排列。"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessagePage'
        '404':
          description: "找不到使用者。"

  /messages/{messageId}:
    get:
      summary: "取得單一訊息"
      parameters:
        - name: "messageId"
          in: "path"
          required: true
          schema:
            type: "integer"
            format: "int64"
      responses:
        '200':
          description: "成功取得訊息。"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Message'
        '404':
          description: "找不到訊息。"
    put:
      summary: "修改訊息 (非同步)"
      description: "接收更新資料，立即返回並在背景非同步處理更新。"
      parameters:
        - name: "messageId"
          in: "path"
          required: true
          schema:
            type: "integer"
            format: "int64"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateMessage'
      responses:
        '202':
          description: "請求已接受，正在處理中。"
        '400':
          description: "請求格式錯誤。"
        '404':
          description: "找不到要修改的訊息。"
    delete:
      summary: "刪除訊息 (非同步)"
      description: "接收刪除請求，立即返回並在背景非同步處理刪除。"
      parameters:
        - name: "messageId"
          in: "path"
          required: true
          schema:
            type: "integer"
            format: "int64"
      responses:
        '202':
          description: "請求已接受，正在處理中。"
        '404':
          description: "找不到要刪除的訊息。"

components:
  schemas:
    User:
      type: "object"
      properties:
        id:
          type: "integer"
          format: "int64"
        username:
          type: "string"
        email:
          type: "string"
          format: "email"
        password:
          type: "string"
          writeOnly: true

    UserRegistration:
      type: "object"
      properties:
        username:
          type: "string"
        email:
          type: "string"
          format: "email"
        password:
          type: "string"
      required:
        - "username"
        - "email"
        - "password"

    UserUpdate:
      type: "object"
      properties:
        username:
          type: "string"
        email:
          type: "string"
          format: "email"
        password:
          type: "string"
          description: "新密碼（可選）"

    LoginRequest:
      type: "object"
      properties:
        username:
          type: "string"
        password:
          type: "string"
      required:
        - "username"
        - "password"

    LoginResponse:
      type: "object"
      properties:
        success:
          type: "boolean"
        user:
          $ref: '#/components/schemas/User'
        message:
          type: "string"

    Task:
      type: "object"
      properties:
        id:
          type: "string"
        status:
          type: "string"
          enum: ["PENDING", "COMPLETED", "FAILED"]
        result:
          type: "string"
        error:
          type: "string"
        createdAt:
          type: "string"
          format: "date-time"
        updatedAt:
          type: "string"
          format: "date-time"

    Message:
      type: "object"
      properties:
        id:
          type: "integer"
          format: "int64"
        userId:
          type: "string"
        content:
          type: "string"
        createdAt:
          type: "string"
          format: "date-time"
        updatedAt:
          type: "string"
          format: "date-time"

    NewMessage:
      type: "object"
      properties:
        userId:
          type: "string"
          description: "發表訊息的使用者 ID"
        content:
          type: "string"
          description: "訊息內容"
      required:
        - "userId"
        - "content"

    UpdateMessage:
      type: "object"
      properties:
        content:
          type: "string"
          description: "新的訊息內容"
      required:
        - "content"

    MessagePage:
      type: "object"
      properties:
        content:
          type: "array"
          items:
            $ref: '#/components/schemas/Message'
        totalElements:
          type: "integer"
          format: "int64"
        totalPages:
          type: "integer"
          format: "int32"
        number:
          type: "integer"
          format: "int32"
          description: "當前頁碼 (從 0 開始)"
        size:
          type: "integer"
          format: "int32"
          description: "每頁數量"
        first:
          type: "boolean"
          description: "是否為第一頁"
        last:
          type: "boolean"
          description: "是否為最後一頁"
        empty:
          type: "boolean"
          description: "內容是否為空"
```

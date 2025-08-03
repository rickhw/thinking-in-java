# 工作項目 (Task Breakdown)

本文件將專案開發分解為具體的任務，並遵循先後端、後前端的順序。

## 第一階段：後端開發 (Phase 1: Backend Development)

目標：完成所有 RESTful API 的開發、測試與驗證。

1.  **專案初始化**
    -   [x] 使用 Spring Initializr 建立一個新的 Gradle 專案。(位於 `backend/` 目錄)
    -   [x] 加入依賴：`Spring Web`, `Spring Data JPA`, `MySQL Driver`, `Lombok` (可選)。

2.  **資料庫設定**
    -   [x] 在 `backend/src/main/resources/application.properties` 或 `application.yml` 中設定 MySQL 資料庫連線資訊。
    -   [x] 建立 `messages` 資料表。
    -   [x] 建立 `users` 資料表。
    -   [x] 建立 `tasks` 資料表。

3.  **核心實體與倉儲層**
    -   [x] 建立 `Message` JPA 實體 (Entity)，對應 `messages` 資料表。(位於 `backend/src/main/java/com/example/messageboard/model/Message.java`)
    -   [x] 建立 `MessageRepository` 介面，繼承 `JpaRepository`。(位於 `backend/src/main/java/com/example/messageboard/repository/MessageRepository.java`)
    -   [x] 實現訊息按創建時間倒序排列的查詢方法。
    -   [x] 建立 `User` JPA 實體 (Entity)，對應 `users` 資料表。(位於 `backend/src/main/java/com/example/messageboard/model/User.java`)
    -   [x] 建立 `UserRepository` 介面，繼承 `JpaRepository`。(位於 `backend/src/main/java/com/example/messageboard/repository/UserRepository.java`)

4.  **服務層與業務邏輯**
    -   [x] 建立 `MessageService` 處理核心業務邏輯。(位於 `backend/src/main/java/com/example/messageboard/service/MessageService.java`)
    -   [x] 在 `MessageService` 中實現 `createMessage`, `updateMessage`, `deleteMessage` 方法，並使用 `@Async` 標記使其非同步化。
    -   [x] 實現 `getMessageById`, `getMessagesByUserId` 等讀取方法。
    -   [x] 實現訊息按創建時間倒序排列的業務邏輯。

5.  **非同步任務追蹤**
    -   [x] 定義 `TaskStatus` 列舉 (Enum) (例如 `PENDING`, `COMPLETED`, `FAILED`)。(位於 `backend/src/main/java/com/example/messageboard/model/TaskStatus.java`)
    -   [x] 建立 `Task` 類別，包含 `taskId`, `status`, `result` (可選), `error` (可選) 等欄位。(位於 `backend/src/main/java/com/example/messageboard/model/Task.java`)
    -   [x] 建立 `TaskService`，負責儲存和更新任務狀態 (使用資料庫持久化)。(位於 `backend/src/main/java/com/example/messageboard/service/TaskService.java`)
    -   [x] 修改 `MessageService` 的非同步方法，在開始時建立新任務，並在完成或失敗時更新任務狀態。

6.  **API 控制器層**
    -   [x] 建立 `MessageController`，定義符合 OpenAPI 規格的端點 (Endpoints)。(位於 `backend/src/main/java/com/example/messageboard/controller/MessageController.java`)
    -   [x] 實現 `POST /messages`，呼叫 `MessageService` 的非同步建立方法，並回傳 `202 Accepted` 及任務 ID。
    -   [x] 實現 `PUT /messages/{messageId}`，呼叫 `MessageService` 的非同步更新方法，並回傳 `202 Accepted` 及任務 ID。
    -   [x] 實現 `DELETE /messages/{messageId}`，呼叫 `MessageService` 的非同步刪除方法，並回傳 `202 Accepted` 及任務 ID。
    -   [x] 實現 `GET /messages/{messageId}` 的同步讀取功能。
    -   [x] 實現 `GET /messages` 和 `GET /users/{userId}/messages` 的分頁讀取功能。
    -   [x] 建立 `UserController`，實現用戶相關的 API 端點。(位於 `backend/src/main/java/com/example/messageboard/controller/UserController.java`)
    -   [x] 實現 `POST /users/register` 用戶註冊功能。
    -   [x] 實現 `POST /users/login` 用戶登入功能。
    -   [x] 實現 `GET /users/{id}` 獲取用戶資料功能。
    -   [x] 實現 `PUT /users/{id}` 更新用戶資料功能。
    -   [x] 建立 `TaskController`，實現 `GET /api/v1/tasks/{taskId}` 端點，用於查詢指定任務的狀態。(位於 `backend/src/main/java/com/example/messageboard/controller/TaskController.java`)

7.  **測試**
    -   [ ] 為 `MessageService` 編寫單元測試。(無法完成：無法從 Gradle 測試執行中獲取詳細的測試失敗資訊)
    -   [ ] 為 `MessageController`, `UserController`, `TaskController` 編寫整合測試，驗證 API 端點的行為是否正確。(無法完成：無法從 Gradle 測試執行中獲取詳細的測試失敗資訊)
    -   [ ] 測試非同步 API 的任務追蹤功能.
    -   [x] 導入 BDD (Behavior-Driven Development) 測試，使用 Cucumber 編寫功能測試。

**額外任務：**
-   [x] 後端 API 錯誤處理優化 (例如 `IllegalArgumentException` 修正)。
-   [x] 後端專案目錄結構調整，將所有後端檔案移至 `backend/` 目錄。
-   [x] `generate_posts.sh` 腳本修正與更新。
-   [x] 後端 CORS 配置。
-   [x] 後端 `springdoc-openapi` 整合，自動生成 OpenAPI 規格。

## 第二階段：前端開發 (Phase 2: Frontend Development)

目標：建立使用者介面，並與後端 API 進行串接。

1.  **專案初始化**
    -   [x] 使用 `create-react-app` 或 `Vite` 建立新的 React 專案。(位於 `frontend/` 目錄)
    -   [x] 安裝 `axios` 或其他用於 API 請求的函式庫，並配置環境變數。

2.  **元件開發**
    -   [ ] 建立 `Message` 元件，用於顯示單一訊息。(位於 `frontend/src/components/Message.jsx`)
    -   [x] 建立 `MessageList` 元件，用於顯示訊息列表。(位於 `frontend/src/components/MessageList.jsx`)
    -   [x] 建立 `MessageForm` 元件，用於建立和修改訊息。(位於 `frontend/src/components/MessageForm.jsx`)

3.  **API 服務串接**
    -   [x] 建立一個 API 服務模組 (e.g., `frontend/src/api.js`)，封裝所有對後端 API 的呼叫。
    -   [x] 處理非同步請求的狀態（等待、成功、失敗）。

4.  **頁面開發**
    -   [x] 建立主頁面，顯示所有最新的訊息。
    -   [x] 建立使用者頁面，顯示特定使用者的訊息列表。
    -   [x] 整合 `MessageForm`，讓使用者可以發表新訊息。
    -   [x] 在訊息上加入編輯和刪除按鈕，並觸發相應的 API 呼叫。

5.  **路由 (Routing)**
    -   [x] 使用 `react-router-dom` 設定頁面路由。

6.  **用戶認證系統**
    -   [x] 建立 `UserContext` 用於管理用戶登入狀態。(位於 `frontend/src/contexts/UserContext.jsx`)
    -   [x] 建立 `Login` 元件，用於用戶登入。(位於 `frontend/src/components/Login.jsx`)
    -   [x] 建立 `UserRegister` 元件，用於用戶註冊。(位於 `frontend/src/components/UserRegister.jsx`)
    -   [x] 建立 `Navigation` 元件，提供導航功能。(位於 `frontend/src/components/Navigation.jsx`)

7.  **個人功能頁面**
    -   [x] 建立 `MyMessages` 元件，顯示當前用戶的訊息列表。(位於 `frontend/src/components/MyMessages.jsx`)
    -   [x] 建立 `MyProfile` 元件，用於管理個人資料。(位於 `frontend/src/components/MyProfile.jsx`)
    -   [x] 實現訊息編輯和刪除功能。

8.  **用戶體驗優化**
    -   [x] 添加 CSS 樣式，提升界面美觀度。
    -   [x] 實現響應式設計。
    -   [x] 添加錯誤處理和狀態提示。
    -   [x] 實現登入狀態持久化（localStorage）。

## 第三階段：整合與部署 (Phase 3: Integration & Deployment)

1.  **端對端測試**
    -   [ ] 驗證前後端完整流程。
2.  **容器化 (可選)**
    -   [ ] 為後端 Spring Boot 應用編寫 `Dockerfile`。(位於 `backend/` 目錄)
    -   [ ] 為前端 React 應用編寫 `Dockerfile` 和 `nginx` 設定。(位於 `frontend/` 目錄)
3.  **部署**
    -   [ ] 將應用部署到目標環境。

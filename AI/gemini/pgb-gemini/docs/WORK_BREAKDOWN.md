### 3. 工作項目 (Work Breakdown Structure)

**階段一：後端開發 (Backend Development)**

1.  **專案初始化:**
    *   [x] 使用 Spring Initializr 建立 Gradle 專案 (Java 17, Spring Web, Spring Data JPA, Spring Security, MySQL Driver)。
2.  **資料庫設定:**
    *   [x] 設計並建立 `users` 和 `posts` 資料表 (已透過 JPA Entity 隱含設計)。
    *   [ ] 使用 Flyway 或 Liquibase 進行資料庫版本控制。
3.  **使用者認證:**
    *   [x] 整合 Spring Security 與 Google OAuth 2.0。
    *   [x] 實作 OAuth2 `successHandler` 來處理使用者登入/註冊邏輯。
    *   [x] 實作 JWT 的產生與驗證機制。
4.  **API 開發 (同步版本):**
    *   [x] 建立 `Post` 和 `User` 的 Entity 與 Repository。
    *   [x] 建立 `PostController` 和 `UserController`。
    *   [x] 實作所有 GET 端點 (瀏覽文章)。
    *   [x] 實作 `POST`, `PUT`, `DELETE` 的核心業務邏輯 (同步版本)。
5.  **非同步化處理:**
    *   [x] 引入 Spring 的 `@Async` 或設定一個訊息佇列 (如 RabbitMQ)。
    *   [x] 將 `POST`, `PUT`, `DELETE` 的服務層邏輯改為非同步執行。
    *   [x] 修改 Controller 回應 `202 Accepted`。
6.  **測試:**
    *   [ ] 為 Service 層撰寫單元測試 (Unit Tests)。
    *   [ ] 為 API 端點撰寫整合測試 (Integration Tests)。

**階段二：前端開發 (Frontend Development)**

1.  **專案初始化:**
    *   [x] 使用 `create-react-app` 或 Vite 建立 React 專案。
2.  **頁面與路由:**
    *   [x] 設定 React Router，建立主要頁面：`HomePage`, `ProfilePage`, `LoginPage`。
3.  **元件開發:**
    *   [x] `Navbar` (導覽列)。
    *   [x] `PostCard` (單一文章卡片)。
    *   [x] `PostList` (文章列表)。
    *   [x] `CreatePostForm` (建立文章表單)。
    *   [x] `LoginButton` (Google 登入按鈕)。
4.  **狀態管理:**
    *   [ ] 選擇並設定狀態管理工具 (如 Redux Toolkit, Zustand 或 React Context)。
5.  **API 整合:**
    *   [x] 建立一個 API 客戶端 (e.g., using Axios)。
    *   [x] 實作 Google 登入流程，並在成功後儲存 JWT。
    *   [x] 將 JWT 附加到所有需要認證的請求 Header 中。
    *   [x] 串接所有後端 API。
6.  **UI/UX:**
    *   [ ] 使用 CSS 框架 (如 Material-UI, Tailwind CSS) 進行基礎樣式設計 (已安裝 MUI，但未全面應用)。

**階段三：整合與部署 (Integration & Deployment)**

1.  **CORS 設定:**
    *   [x] 在 Spring Boot 後端設定 CORS，允許前端應用程式的跨域請求 (已設定，否則本地開發無法運作)。
2.  **環境變數:**
    *   [ ] 設定前後端的環境變數 (如資料庫連線資訊, Google OAuth Client ID/Secret)。
3.  **建構與容器化:**
    *   [ ] 為後端應用撰寫 `Dockerfile`。
    *   [ ] 為前端應用撰寫 `Dockerfile` (使用 multi-stage build)。
4.  **部署:**
    *   [ ] 使用 Docker Compose 在本地整合測試。
    *   [ ] 部署到雲端平台 (如 AWS, GCP, Heroku)。

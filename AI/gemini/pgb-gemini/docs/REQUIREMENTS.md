### 1. 需求文件 (Requirements Document)

**1.1. 專案目標**
建立一個現代化、前後端分離的社群留言板應用程式。使用者可以透過 Google 帳號快速註冊登入，並進行貼文、修改、刪除和瀏覽等基本社群互動。

**1.2. 核心功能 (Core Features)**

*   **使用者認證 (User Authentication)**
    *   使用者需透過 SSO (Single Sign-On) 方式註冊與登入。
    *   初期僅支援 Google OAuth 2.0。
    *   成功認證後，系統應為新使用者自動建立帳號。
    *   後端需產生一組 token (例如 JWT) 供前端在後續請求中進行身份驗證。

*   **文章管理 (Post Management)**
    *   **建立文章 (Create):** 已登入的使用者可以發表新的文字內容。
    *   **修改文章 (Update):** 使用者只能修改自己發表的文章。
    *   **刪除文章 (Delete):** 使用者只能刪除自己發表的文章。
    *   **瀏覽文章 (Read):**
        *   使用者可以瀏覽自己發表過的所有文章。
        *   使用者可以瀏覽指定他人的所有文章。
        *   使用者可以瀏覽一個包含所有使用者最新文章的時間軸 (Timeline)。

**1.3. 技術規格 (Technical Specifications)**

*   **架構:** 前後端分離 (Decoupled Architecture)。
*   **後端 (Backend):**
    *   語言: Java 17
    *   框架: Spring Boot 3
    *   建構工具: Gradle
    *   資料庫: MySQL
*   **前端 (Frontend):**
    *   框架: React
*   **API:**
    *   風格: RESTful API。
    *   **非同步處理:** 建立、修改、刪除文章的操作需為非同步 API，以提升使用者體驗。伺服器接收請求後應立即回傳 `202 Accepted`，並在背景完成操作。

**1.4. 非功能性需求 (Non-Functional Requirements)**

*   **安全性:** API 端點需受保護，僅有通過認證的使用者才能執行需授權的操作。
*   **使用者體驗:** 非同步的 API 設計確保使用者在執行耗時操作時無需等待。

# 需求文件 (Requirements Document)

## 1. 專案概述 (Project Overview)

本專案旨在開發一個功能類似 Twitter 的簡易留言板應用。使用者可以註冊帳號、登入系統、發布訊息、修改和刪除自己的訊息，並瀏覽自己和他人的訊息。

專案將採用前後端分離的架構，後端提供 RESTful API 供前端呼叫。所有寫入操作（新增、修改、刪除）應為非同步處理，以提升系統的回應速度和吞吐量。

## 2. 核心功能 (Core Features)

### 2.1 用戶認證與管理
-   **用戶註冊 (User Registration):** 新用戶可以註冊帳號，提供用戶名、電子郵件和密碼。
-   **用戶登入 (User Login):** 已註冊用戶可以使用用戶名和密碼登入系統。
-   **用戶登出 (User Logout):** 已登入用戶可以安全登出系統。
-   **個人資料管理 (Profile Management):** 用戶可以查看和編輯自己的個人資料，包括用戶名、電子郵件和密碼。

### 2.2 訊息管理
-   **訊息發布 (Message Creation):** 已登入的使用者可以建立新的訊息。
-   **訊息修改 (Message Editing):** 使用者可以修改自己發布過的訊息。
-   **訊息刪除 (Message Deletion):** 使用者可以刪除自己發布過的訊息。
-   **瀏覽自己的訊息 (View Own Messages):** 使用者可以查看自己所有訊息的列表，並進行編輯和刪除操作。訊息按創建時間倒序排列（最新的在前面）。
-   **瀏覽他人訊息 (View Others' Messages):** 使用者可以查看特定其他使用者的所有訊息列表，按創建時間倒序排列。
-   **瀏覽所有訊息 (View All Messages):** 提供一個公開的時間軸，按創建時間倒序顯示所有使用者的訊息（最新的在前面）。

## 3. 技術規格 (Technical Specifications)

-   **後端 (Backend):** (位於 `backend/` 目錄)
    -   語言: Java 17
    -   框架: Spring Boot 3
    -   建置工具: Gradle
    -   資料庫: MySQL
-   **前端 (Frontend):** (位於 `frontend/` 目錄)
    -   框架: React
    -   建置工具: Vite
    -   狀態管理: React Context API
    -   路由: React Router

## 4. 架構要求 (Architectural Requirements)

-   **前後端分離:** 前端和後端獨立開發、獨立部署。
-   **RESTful API:** 後端需提供設計良好的 RESTful API。
-   **非同步寫入:** 貼文、修文、刪文等寫入操作的 API 應為非同步，立即返回接受請求的狀態，並在背景完成實際操作。
-   **用戶認證:** 實現基本的用戶登入認證系統，支持用戶狀態管理和持久化。

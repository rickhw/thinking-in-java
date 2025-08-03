# 需求文件 (Requirements Document)

## 1. 專案概述 (Project Overview)

本專案旨在開發一個功能類似 Twitter 的簡易留言板應用。使用者可以發布、修改、刪除自己的文章，並瀏覽自己和他人的文章。

專案將採用前後端分離的架構，後端提供 RESTful API 供前端呼叫。所有寫入操作（新增、修改、刪除）應為非同步處理，以提升系統的回應速度和吞吐量。

## 2. 核心功能 (Core Features)

-   **文章發布 (Post Creation):** 已登入的使用者可以建立新的文章。
-   **文章修改 (Post Editing):** 使用者可以修改自己發布過的文章。
-   **文章刪除 (Post Deletion):** 使用者可以刪除自己發布過的文章。
-   **瀏覽自己的文章 (View Own Posts):** 使用者可以查看自己所有文章的列表。
-   **瀏覽他人文章 (View Others' Posts):** 使用者可以查看特定其他使用者的所有文章列表。
-   **瀏覽所有文章 (View All Posts):** 提供一個公開的時間軸，按時間倒序顯示所有使用者的文章。

## 3. 技術規格 (Technical Specifications)

-   **後端 (Backend):** (位於 `backend/` 目錄)
    -   語言: Java 17
    -   框架: Spring Boot 3
    -   建置工具: Gradle
    -   資料庫: MySQL
-   **前端 (Frontend):** (位於 `frontend/` 目錄)
    -   框架: React

## 4. 架構要求 (Architectural Requirements)

-   **前後端分離:** 前端和後端獨立開發、獨立部署。
-   **RESTful API:** 後端需提供設計良好的 RESTful API。
-   **非同步寫入:** 貼文、修文、刪文等寫入操作的 API 應為非同步，立即返回接受請求的狀態，並在背景完成實際操作。

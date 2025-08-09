# Message Board 留言板系統

一個功能類似 Twitter 的簡易留言板應用，支持用戶登入、發布訊息、查看和管理個人訊息。

## 功能特色

### 用戶功能
- **用戶註冊**: 新用戶可以註冊帳號
- **用戶登入/登出**: 安全的登入系統
- **個人資料管理**: 用戶可以查看和編輯自己的資料

### 訊息功能
- **瀏覽所有訊息**: 查看所有用戶發布的訊息（時間倒序）
- **發布訊息**: 登入用戶可以發布新訊息
- **我的訊息**: 查看自己發布的所有訊息
- **編輯訊息**: 編輯自己發布的訊息
- **刪除訊息**: 刪除自己發布的訊息

## 技術架構

- **後端**: Java 17 + Spring Boot 3 + MySQL
- **前端**: React + Vite
- **API**: RESTful API 設計
- **非同步處理**: 寫入操作採用非同步處理

## 快速開始

### 前置需求
- Java 17+
- Node.js 20+
- MySQL 5.7+

### 啟動後端服務

1. 進入後端目錄：
```bash
cd backend
```

2. 啟動 Spring Boot 應用：
```bash
./gradlew bootRun
```

後端服務將在 `http://localhost:8080` 啟動

### 啟動前端服務

1. 進入前端目錄：
```bash
cd frontend
```

2. 安裝依賴：
```bash
npm install
```

3. 啟動開發服務器：
```bash
npm run dev
```

前端服務將在 `http://localhost:5173` 或 `http://localhost:5174` 啟動

### 創建測試用戶

執行以下腳本創建測試用戶：
```bash
cd backend
./generate_users.sh
```

這將創建以下測試帳號：
- 用戶名: `rick`, 密碼: `password123`
- 用戶名: `alice`, 密碼: `password123`
- 用戶名: `bob`, 密碼: `password123`
- 用戶名: `charlie`, 密碼: `password123`
- 用戶名: `diana`, 密碼: `password123`

## 使用說明

### 登入系統
1. 訪問前端應用
2. 點擊「登入」
3. 使用測試帳號登入（例如：用戶名 `rick`，密碼 `password123`）

### 發布訊息
1. 登入後點擊導航欄的「發布訊息」
2. 在專門的發布頁面輸入訊息內容
3. 點擊「發布訊息」完成發布
4. 發布成功後自動跳轉回首頁

### 瀏覽和互動
1. 在首頁瀏覽所有用戶的訊息
2. 點擊訊息中的用戶名可查看該用戶的所有訊息
3. 使用分頁功能瀏覽更多內容

### 管理個人訊息
1. 登入後點擊導航欄的「我的訊息」
2. 可以查看、編輯或刪除自己的訊息
3. 支持分頁瀏覽個人歷史訊息

### 個人資料管理
1. 登入後點擊導航欄的「我的資料」
2. 可以更新用戶名、電子郵件和密碼

### 分頁導航
系統支持 URL 分頁，你可以：
- 直接訪問特定頁面：`/page/2`、`/messages/page/3`、`/user/rick/messages/page/2`
- 使用瀏覽器的前進/後退按鈕
- 收藏或分享特定頁面的 URL
- 第一頁使用簡潔的 URL（`/` 而不是 `/page/1`）

### 用戶互動
- 點擊訊息中的用戶名可查看該用戶的所有訊息
- 每個用戶都有專門的訊息頁面
- 支持直接分享用戶訊息頁面的連結

## API 端點

### 用戶相關
- `POST /api/v1/users/register` - 用戶註冊
- `POST /api/v1/users/login` - 用戶登入
- `GET /api/v1/users/{id}` - 獲取用戶資料
- `PUT /api/v1/users/{id}` - 更新用戶資料

### 訊息相關
- `GET /api/v1/messages` - 獲取所有訊息（分頁）
- `POST /api/v1/messages` - 創建新訊息（非同步）
- `GET /api/v1/messages/{id}` - 獲取單一訊息
- `PUT /api/v1/messages/{id}` - 更新訊息（非同步）
- `DELETE /api/v1/messages/{id}` - 刪除訊息（非同步）
- `GET /api/v1/users/{userId}/messages` - 獲取特定用戶的訊息

### 任務狀態
- `GET /api/v1/tasks/{taskId}` - 查詢非同步任務狀態

## 專案結構

```
├── backend/                 # 後端 Spring Boot 應用
│   ├── src/main/java/      # Java 源代碼
│   ├── src/main/resources/ # 配置文件
│   ├── build.gradle        # Gradle 構建文件
│   └── generate_users.sh   # 測試用戶生成腳本
├── frontend/               # 前端 React 應用
│   ├── src/               # React 源代碼
│   ├── public/            # 靜態資源
│   └── package.json       # NPM 配置文件
├── docs/                  # 專案文檔
│   ├── message-id-redesign/ # Message ID 重新設計文檔
│   ├── v1/                # 版本 1.0 文檔
│   └── v1.1/              # 版本 1.1 文檔
└── .kiro/specs/           # Kiro 規格文件
    └── message-id-redesign/ # Message ID 重新設計規格
```

## 開發說明

- 後端採用非同步處理寫入操作，提升系統性能
- 前端使用 React Context 管理用戶登入狀態
- 支持 CORS 跨域請求
- 使用 localStorage 保存用戶登入狀態

## 系統更新

### Message ID 重新設計 (v2.0.0)

系統已更新為使用 36 位字符串 ID 替代原有的 Long 類型 ID，提供更好的安全性和可擴展性。

**新 ID 格式**: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`
**字符集**: A-Z, 0-9（大寫字母和數字）

📖 **完整文檔**: 查看 [`docs/message-id-redesign/`](./docs/message-id-redesign/) 目錄獲取：
- API 變更說明
- OpenAPI 規格文件
- 部署和配置指南
- 遷移操作手冊

## 注意事項

⚠️ **安全提醒**: 這是一個演示專案，密碼以明文存儲。在生產環境中請務必：
- 使用密碼哈希（如 bcrypt）
- 實現 JWT 或 Session 管理
- 添加輸入驗證和 SQL 注入防護
- 使用 HTTPS
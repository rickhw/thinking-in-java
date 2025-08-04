# 設計文件 - 單一訊息顯示頁面

## 概述

本設計文件描述了為 pgb4_login 留言板系統添加單一訊息顯示頁面的技術實現方案。該功能將允許使用者通過唯一的 permalink URI 直接訪問和查看特定的訊息內容，並提供相應的互動功能。

## 架構

### 前端架構

單一訊息顯示頁面將整合到現有的 React 前端架構中：

- **路由層**: 在 React Router 中添加新的路由 `/message/:messageId`
- **組件層**: 創建新的 `SingleMessage` 組件來處理單一訊息的顯示和互動
- **服務層**: 利用現有的 `api.js` 中的 `getMessageById` 函數
- **狀態管理**: 使用現有的 `UserContext` 進行用戶認證狀態管理

### 後端架構

後端無需修改，將使用現有的 API 端點：

- `GET /api/v1/messages/{messageId}` - 獲取單一訊息
- `PUT /api/v1/messages/{messageId}` - 更新訊息（非同步）
- `DELETE /api/v1/messages/{messageId}` - 刪除訊息（非同步）
- `GET /api/v1/tasks/{taskId}` - 查詢任務狀態

## 組件和介面

### 1. SingleMessage 組件

**位置**: `frontend/src/components/SingleMessage.jsx`

**Props**:
```javascript
// 無 props，使用 useParams 從 URL 獲取 messageId
```

**狀態**:
```javascript
const [message, setMessage] = useState(null);
const [loading, setLoading] = useState(true);
const [error, setError] = useState(null);
const [isEditing, setIsEditing] = useState(false);
const [editContent, setEditContent] = useState('');
const [actionLoading, setActionLoading] = useState(false);
```

**主要功能**:
- 根據 URL 參數中的 messageId 獲取訊息詳情
- 顯示訊息內容、作者資訊、時間戳
- 為訊息作者提供編輯和刪除功能
- 提供導航連結（返回首頁、查看作者其他訊息）
- 處理載入狀態和錯誤狀態

### 2. 路由配置更新

**位置**: `frontend/src/App.jsx`

在現有路由中添加：
```javascript
<Route path="/message/:messageId" element={<SingleMessage />} />
```

### 3. MessageList 組件更新

**位置**: `frontend/src/components/MessageList.jsx`

為每個訊息項目添加 permalink 連結：
```javascript
<Link to={`/message/${message.id}`} className="message-permalink">
  查看詳情
</Link>
```

## 資料模型

### Message 資料結構

使用現有的 Message 資料結構：
```javascript
{
  id: number,           // 訊息 ID
  userId: string,       // 作者 ID
  content: string,      // 訊息內容
  createdAt: string,    // 創建時間 (ISO 8601)
  updatedAt: string     // 更新時間 (ISO 8601)
}
```

### 頁面標題資料結構

```javascript
{
  title: string,        // 頁面標題
  description: string,  // 頁面描述（用於 meta 標籤）
  author: string,       // 訊息作者
  publishedTime: string // 發布時間
}
```

## 錯誤處理

### 1. 訊息不存在 (404)

當 `getMessageById` API 返回 404 錯誤時：
- 顯示友好的錯誤頁面
- 提供返回首頁的連結
- 提供搜索其他訊息的建議

### 2. 網路錯誤

當 API 請求失敗時：
- 顯示錯誤訊息
- 提供重試按鈕
- 保持頁面結構完整

### 3. 權限錯誤

當用戶嘗試編輯他人訊息時：
- 隱藏編輯和刪除按鈕
- 在嘗試操作時顯示權限錯誤訊息

## 測試策略

### 1. 單元測試

**測試範圍**:
- SingleMessage 組件的渲染邏輯
- 不同用戶權限下的按鈕顯示/隱藏
- 錯誤狀態的處理
- 編輯模式的切換

**測試工具**: React Testing Library + Jest

### 2. 整合測試

**測試範圍**:
- 路由導航到單一訊息頁面
- API 調用和資料載入
- 編輯和刪除操作的完整流程
- 錯誤處理的端到端流程

### 3. 使用者體驗測試

**測試範圍**:
- 不同螢幕尺寸下的響應式設計
- 載入狀態的使用者體驗
- 錯誤狀態的使用者體驗
- 導航流程的直觀性

## 使用者介面設計

### 1. 佈局結構

```
┌─────────────────────────────────────┐
│ Navigation Bar                      │
├─────────────────────────────────────┤
│ ← 返回所有訊息                        │
├─────────────────────────────────────┤
│ 訊息詳情                             │
│ ┌─────────────────────────────────┐ │
│ │ 作者: [用戶名] (連結)              │ │
│ │ 時間: [創建時間]                  │ │
│ │ 更新: [更新時間] (如果有)          │ │
│ ├─────────────────────────────────┤ │
│ │ [訊息內容]                       │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [編輯] [刪除] (僅作者可見)            │
│                                     │
│ 相關連結:                           │
│ • 查看 [作者] 的所有訊息              │
│ • 返回所有訊息                       │
└─────────────────────────────────────┘
```

### 2. 響應式設計

**桌面版 (≥768px)**:
- 訊息內容居中顯示，最大寬度 800px
- 按鈕橫向排列
- 充足的邊距和行間距

**移動版 (<768px)**:
- 全寬度顯示
- 按鈕垂直堆疊
- 緊湊的間距設計

### 3. 視覺狀態

**載入狀態**:
- 顯示骨架屏或載入指示器
- 保持頁面結構完整

**錯誤狀態**:
- 顯示錯誤圖示和訊息
- 提供操作建議（重試、返回等）

**編輯狀態**:
- 內容區域變為可編輯的文字區域
- 顯示儲存和取消按鈕
- 禁用其他操作按鈕

## SEO 和分享優化

### 1. Meta 標籤

動態設置頁面的 meta 標籤：
```html
<title>訊息詳情 - {訊息內容前50字} - pgb4_login</title>
<meta name="description" content="{訊息內容前150字}" />
<meta property="og:title" content="訊息詳情 - pgb4_login" />
<meta property="og:description" content="{訊息內容前150字}" />
<meta property="og:type" content="article" />
<meta property="og:url" content="{當前頁面URL}" />
```

### 2. 結構化資料

添加 JSON-LD 結構化資料：
```json
{
  "@context": "https://schema.org",
  "@type": "SocialMediaPosting",
  "headline": "{訊息內容前100字}",
  "author": {
    "@type": "Person",
    "name": "{作者用戶名}"
  },
  "datePublished": "{創建時間}",
  "dateModified": "{更新時間}",
  "text": "{完整訊息內容}"
}
```

## 效能考量

### 1. 載入優化

- 使用 React.lazy 進行組件懶載入
- 實現適當的載入狀態指示
- 避免不必要的重新渲染

### 2. 快取策略

- 利用瀏覽器快取已載入的訊息
- 實現適當的快取失效機制
- 考慮使用 React Query 或 SWR 進行資料快取

### 3. 錯誤邊界

- 實現 React Error Boundary 來捕獲組件錯誤
- 提供優雅的錯誤恢復機制
- 記錄錯誤資訊用於除錯

## 安全考量

### 1. 輸入驗證

- 驗證 messageId 參數的格式
- 防止 XSS 攻擊（React 預設已處理）
- 驗證編輯內容的長度和格式

### 2. 權限控制

- 在前端隱藏未授權操作的按鈕
- 依賴後端 API 進行最終的權限驗證
- 處理權限錯誤的回應

### 3. 資料保護

- 不在 URL 中暴露敏感資訊
- 確保訊息內容的適當轉義
- 實現適當的錯誤訊息，避免資訊洩露
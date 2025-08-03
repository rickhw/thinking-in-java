# 測試資料生成腳本說明

## 📋 腳本概覽

本目錄包含用於生成測試資料的腳本，幫助快速設置留言板系統的測試環境。

### 🔧 腳本文件

| 腳本名稱 | 功能描述 | 用途 |
|---------|---------|------|
| `setup_test_data.sh` | **主要腳本** - 一鍵設置完整測試環境 | 推薦使用 |
| `generate_users.sh` | 創建 10 個測試用戶 | 單獨執行 |
| `generate_posts.sh` | 為用戶生成 50-100 條隨機訊息 | 單獨執行 |
| `cleanup_test_data.sh` | 清理測試資料的說明和統計 | 資料清理 |

## 🚀 快速開始

### 1. 一鍵設置（推薦）

```bash
# 進入後端目錄
cd backend

# 執行整合設置腳本
chmod +x setup_test_data.sh
./setup_test_data.sh
```

### 2. 分步執行

```bash
# 步驟 1: 創建測試用戶
chmod +x generate_users.sh
./generate_users.sh

# 步驟 2: 生成測試訊息
chmod +x generate_posts.sh
./generate_posts.sh
```

## 👥 測試用戶資訊

### 用戶列表
腳本會創建以下 10 個測試用戶：

| 用戶名 | 密碼 | 電子郵件 |
|--------|------|----------|
| alice | password123 | alice@example.com |
| bob | password123 | bob@example.com |
| charlie | password123 | charlie@example.com |
| diana | password123 | diana@example.com |
| eve | password123 | eve@example.com |
| frank | password123 | frank@example.com |
| grace | password123 | grace@example.com |
| henry | password123 | henry@example.com |
| iris | password123 | iris@example.com |
| jack | password123 | jack@example.com |

### 訊息資料
- **每個用戶**: 50-100 條隨機訊息
- **總訊息數**: 約 500-1000 條
- **內容類型**: 包含中文模板訊息、主題討論、隨機想法等

## 📊 資料特色

### 真實感內容
訊息內容包含多種類型：
- 📝 **模板訊息**: 預設的常見社交媒體內容
- 🎯 **主題討論**: 基於工作、生活、學習等主題
- 💭 **隨機想法**: 編號的隨想內容
- ⏰ **時間標記**: 部分訊息包含時間戳

### 多樣化分佈
- 每個用戶的訊息數量隨機（50-100條）
- 訊息內容隨機選擇不同模板
- 發布時間自然分散

## 🔧 前置需求

### 必要條件
1. **後端服務運行**: 確保 Spring Boot 應用在 `http://localhost:8080` 運行
2. **資料庫連接**: 確保資料庫連接正常
3. **網路連接**: 腳本需要通過 HTTP API 與後端通信

### 檢查後端狀態
```bash
# 檢查後端健康狀態
curl http://localhost:8080/actuator/health

# 如果後端未運行，啟動它
./gradlew bootRun
```

## 🧹 資料清理

### 清理方法

#### 方法 1: 重啟服務（推薦）
```bash
# 停止後端服務 (Ctrl+C)
# 重新啟動
./gradlew bootRun
```

#### 方法 2: 使用清理腳本
```bash
chmod +x cleanup_test_data.sh
./cleanup_test_data.sh
```

#### 方法 3: 手動 SQL 清理
```sql
DELETE FROM messages;
DELETE FROM users;
DELETE FROM tasks;
```

## 🧪 測試建議

### 基本測試流程
1. **執行設置腳本** - 生成測試資料
2. **啟動前端** - 訪問 `http://localhost:5174`
3. **登入測試** - 使用任一測試帳號登入
4. **功能測試** - 測試瀏覽、發布、編輯、刪除功能

### 進階測試場景
- **分頁測試**: 查看不同頁面的訊息
- **用戶切換**: 測試不同用戶的訊息列表
- **搜索功能**: 點擊用戶名查看個人訊息
- **權限測試**: 確認只能編輯自己的訊息

## ⚠️ 注意事項

### 性能考量
- 腳本包含適當的延遲以避免服務器過載
- 大量資料生成可能需要幾分鐘時間
- 建議在開發環境中使用

### 資料安全
- 這些是測試資料，不要在生產環境使用
- 密碼使用簡單格式，僅供測試
- 建議定期清理測試資料

### 故障排除
- 如果腳本執行失敗，檢查後端服務狀態
- 確認資料庫連接正常
- 查看後端日誌以獲取錯誤詳情

## 📈 統計資訊

執行完成後，你將獲得：
- ✅ 10 個活躍的測試用戶
- ✅ 500-1000 條多樣化的測試訊息
- ✅ 完整的分頁測試資料
- ✅ 真實的用戶互動場景

這些資料足以測試留言板系統的所有核心功能！
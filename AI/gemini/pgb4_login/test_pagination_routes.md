# 分頁路由測試

## 測試 URL 路由

### 首頁分頁
- `http://localhost:5174/` - 第一頁（預設）
- `http://localhost:5174/page/2` - 第二頁
- `http://localhost:5174/page/3` - 第三頁

### 我的訊息分頁
- `http://localhost:5174/my-messages` - 第一頁（預設）
- `http://localhost:5174/my-messages/page/2` - 第二頁
- `http://localhost:5174/my-messages/page/3` - 第三頁

## 預期行為

1. **URL 同步**: 當用戶點擊分頁按鈕時，URL 應該更新
2. **直接訪問**: 用戶可以直接通過 URL 訪問特定頁面
3. **瀏覽器歷史**: 支持瀏覽器的前進/後退按鈕
4. **書籤支持**: 用戶可以收藏特定頁面的 URL
5. **分享友好**: URL 可以直接分享給其他用戶

## 測試步驟

1. 訪問首頁 `http://localhost:5174/`
2. 點擊 "Next" 按鈕，URL 應該變為 `/page/2`
3. 直接在瀏覽器地址欄輸入 `/page/3`，應該顯示第三頁
4. 使用瀏覽器後退按鈕，應該回到上一頁
5. 登入後訪問 `/my-messages/page/2`，應該顯示個人訊息的第二頁

## 邊界情況測試

- 訪問不存在的頁面（如 `/page/999`）
- 訪問負數頁面（如 `/page/-1`）
- 訪問非數字頁面（如 `/page/abc`）
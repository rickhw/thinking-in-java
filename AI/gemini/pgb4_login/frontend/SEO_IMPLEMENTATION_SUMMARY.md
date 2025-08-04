# SEO 優化和頁面標題實作總結

## 實作概述

本次實作為 SingleMessage 組件添加了完整的 SEO 優化功能，包括動態頁面標題、meta 標籤管理和結構化資料支援。

## 實作的功能

### 1. 增強的 PageContext

**檔案**: `frontend/src/contexts/PageContext.jsx`

**新增功能**:
- 動態更新 document.title
- 管理 meta description 標籤
- 管理 Open Graph meta 標籤 (og:title, og:description, og:type, og:url)
- 管理 JSON-LD 結構化資料
- 提供 `setPageMeta()` 和 `resetPageMeta()` 便利方法

**支援的 Meta 標籤**:
- `<meta name="description" content="..." />`
- `<meta property="og:title" content="..." />`
- `<meta property="og:description" content="..." />`
- `<meta property="og:type" content="article|website" />`
- `<meta property="og:url" content="..." />`
- `<script type="application/ld+json">...</script>` (結構化資料)

### 2. SEO 工具函數

**檔案**: `frontend/src/utils/seo.js`

**提供的功能**:
- `truncateText()` - 文字截斷功能
- `cleanTextForSEO()` - 清理文字格式（移除多餘空白和換行）
- `generateMessageTitle()` - 生成訊息頁面標題
- `generateMessageDescription()` - 生成訊息頁面描述
- `generateMessageStructuredData()` - 生成結構化資料
- 預定義的頁面標題和描述常數

### 3. SingleMessage 組件更新

**檔案**: `frontend/src/components/SingleMessage.jsx`

**新增的 SEO 功能**:
- 載入狀態的頁面標題更新
- 錯誤狀態的頁面標題更新（404、網路錯誤、一般錯誤）
- 基於訊息內容的動態頁面標題
- 自動生成 meta description
- 結構化資料支援
- 組件卸載時重置頁面 meta 資料

### 4. HTML 模板優化

**檔案**: `frontend/index.html`

**新增的基本 SEO 標籤**:
- 設定語言為 `zh-TW`
- 基本 meta description
- meta keywords
- meta author
- Open Graph site_name, type, locale

## 頁面標題規則

### 訊息頁面
- **格式**: `{訊息內容前50字}... - Message Board`
- **描述**: 訊息內容前150字
- **結構化資料**: SocialMediaPosting 類型

### 載入狀態
- **標題**: `載入中... - Message Board`
- **描述**: `正在載入訊息內容...`

### 錯誤狀態
- **404 錯誤**: `訊息不存在 - Message Board`
- **網路錯誤**: `網路連線錯誤 - Message Board`
- **一般錯誤**: `載入錯誤 - Message Board`

## 結構化資料格式

使用 Schema.org 的 SocialMediaPosting 類型：

```json
{
  "@context": "https://schema.org",
  "@type": "SocialMediaPosting",
  "headline": "頁面標題",
  "author": {
    "@type": "Person",
    "name": "作者用戶名"
  },
  "datePublished": "發布時間",
  "dateModified": "更新時間",
  "text": "訊息描述",
  "url": "當前頁面URL"
}
```

## 測試檔案

**檔案**: `frontend/test-page-title.html`

提供了完整的測試介面來驗證：
- 訊息標題生成
- 長文本截斷
- 載入狀態標題
- 錯誤狀態標題
- Meta 標籤更新
- 結構化資料生成

## 使用方式

### 在組件中使用 PageContext

```javascript
import { usePageTitle } from '../contexts/PageContext';

const MyComponent = () => {
    const { setPageMeta, resetPageMeta } = usePageTitle();
    
    // 設置頁面 meta 資料
    setPageMeta({
        title: '頁面標題',
        description: '頁面描述',
        author: '作者',
        publishedTime: '2025-01-08T10:00:00Z'
    });
    
    // 重置頁面 meta 資料
    useEffect(() => {
        return () => resetPageMeta();
    }, [resetPageMeta]);
};
```

### 使用 SEO 工具函數

```javascript
import { generateMessageTitle, generateMessageDescription } from '../utils/seo';

const title = generateMessageTitle(message, 50);
const description = generateMessageDescription(message, 150);
```

## 效能考量

- 使用 useEffect 來避免不必要的 DOM 操作
- 在組件卸載時清理 meta 標籤
- 文字處理函數進行了優化以處理邊界情況
- 結構化資料只在有完整訊息資料時才生成

## 瀏覽器相容性

- 支援所有現代瀏覽器
- 使用標準的 DOM API
- 不依賴任何外部 SEO 庫

## 符合的需求

- ✅ **需求 1.4**: 在瀏覽器標題欄顯示包含訊息內容預覽的標題
- ✅ **需求 5.1**: 提供適當的 meta 標籤用於社交媒體預覽
- ✅ **需求 5.2**: 提供結構化的內容供搜索引擎索引

## 未來改進建議

1. 添加更多的 Open Graph 標籤（如 og:image）
2. 支援 Twitter Card meta 標籤
3. 添加 canonical URL 支援
4. 實作更複雜的結構化資料類型
5. 添加 sitemap 生成功能
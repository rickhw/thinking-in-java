# 實作計劃

- [ ] 1. 建立專案結構和核心配置
  - 建立 Spring Boot 後端專案結構，包含 controller、service、repository、entity 等套件
  - 設定 Gradle 建置檔案，包含所需依賴項（Spring Boot 3、Spring Security、JPA、MySQL、Redis）
  - 建立 React 前端專案結構，包含 components、services、hooks、utils 等目錄
  - 配置開發環境的 application.yml 和前端環境變數檔案
  - _需求: 4.1, 4.5, 5.1, 5.2_

- [ ] 2. 實作資料模型和資料庫配置
- [ ] 2.1 建立 JPA 實體類別和資料庫 schema
  - 實作 User 實體類別，包含 Google OAuth 相關欄位和 JPA 註解
  - 實作 Post 實體類別，包含與 User 的關聯關係和軟刪除機制
  - 建立資料庫 migration 腳本，定義資料表結構和索引
  - 撰寫實體類別的單元測試，驗證 JPA 映射和驗證規則
  - _需求: 2.3, 2.4, 2.5, 2.6_

- [ ] 2.2 實作 Repository 層和資料存取邏輯
  - 建立 UserRepository 介面，包含根據 Google ID 查詢使用者的方法
  - 建立 PostRepository 介面，包含分頁查詢、使用者文章查詢等方法
  - 實作自訂查詢方法，支援軟刪除和排序功能
  - 撰寫 Repository 層的整合測試，使用 @DataJpaTest 驗證資料存取邏輯
  - _需求: 3.1, 3.2, 3.3, 3.4_

- [ ] 3. 實作 Google OAuth 認證系統
- [ ] 3.1 建立 OAuth 配置和 JWT 處理
  - 配置 Spring Security OAuth2 客戶端，設定 Google OAuth 參數
  - 實作 JWT Token 產生和驗證邏輯，使用 RS256 演算法
  - 建立 JwtAuthenticationFilter 處理 HTTP 請求中的 JWT Token
  - 撰寫 JWT 相關的單元測試，驗證 Token 產生和驗證邏輯
  - _需求: 1.1, 1.2, 1.4, 1.5_

- [ ] 3.2 實作認證服務和使用者管理
  - 建立 AuthService 處理 Google OAuth 授權碼交換和使用者建立
  - 實作 UserService 管理使用者資料和個人資料更新
  - 建立認證相關的 DTO 類別（AuthResponse、UserInfo 等）
  - 撰寫認證服務的單元測試，模擬 Google OAuth 回應
  - _需求: 1.2, 1.3, 1.6_

- [ ] 3.3 建立認證 API 端點
  - 實作 AuthController 提供 /auth/google 和 /auth/refresh 端點
  - 實作 UserController 提供 /users/me 端點
  - 加入請求驗證和錯誤處理邏輯
  - 撰寫 Controller 層的整合測試，使用 MockMvc 測試 API 端點
  - _需求: 1.1, 1.4, 1.5, 1.6, 4.3, 4.4_

- [ ] 4. 實作文章管理功能
- [ ] 4.1 建立非同步處理基礎設施
  - 配置 Spring Async 和執行緒池，支援非同步操作
  - 建立 AsyncOperationService 管理非同步操作狀態
  - 實作操作狀態追蹤機制，使用 Redis 儲存操作狀態
  - 撰寫非同步處理的單元測試，驗證操作狀態管理
  - _需求: 2.1, 2.7, 4.2_

- [ ] 4.2 實作文章服務層邏輯
  - 建立 PostService 包含文章 CRUD 的非同步方法
  - 實作文章內容驗證邏輯（長度限制、空內容檢查）
  - 實作權限檢查邏輯，確保使用者只能修改自己的文章
  - 撰寫 PostService 的單元測試，包含各種邊界條件測試
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [ ] 4.3 建立文章 API 端點
  - 實作 PostController 提供文章相關的 RESTful API
  - 實作分頁查詢邏輯，支援排序和過濾功能
  - 加入 API 請求驗證和權限檢查
  - 撰寫 PostController 的整合測試，測試所有 API 端點
  - _需求: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 4.1, 4.3, 4.4_

- [ ] 5. 實作前端認證功能
- [ ] 5.1 建立認證相關元件和服務
  - 建立 AuthContext 和 AuthProvider 管理全域認證狀態
  - 實作 GoogleOAuthButton 元件處理 Google OAuth 登入流程
  - 建立 authService 處理 API 呼叫和 Token 管理
  - 實作 PrivateRoute 元件保護需要認證的頁面
  - _需求: 1.1, 1.2, 1.4, 1.5, 1.6_

- [ ] 5.2 建立使用者介面和導航
  - 實作 Header 元件顯示使用者資訊和登出按鈕
  - 建立 LoginPage 元件提供登入介面
  - 實作 UserProfile 元件顯示使用者個人資訊
  - 撰寫認證相關元件的單元測試
  - _需求: 1.1, 1.6, 5.3, 5.4_

- [ ] 6. 實作前端文章功能
- [ ] 6.1 建立文章顯示和列表元件
  - 實作 PostList 元件顯示文章列表，支援分頁和載入狀態
  - 建立 PostItem 元件顯示單篇文章內容和作者資訊
  - 實作 UserPostList 元件顯示特定使用者的文章
  - 撰寫文章顯示相關元件的單元測試
  - _需求: 3.1, 3.2, 3.3, 3.4_

- [ ] 6.2 建立文章編輯和管理元件
  - 實作 PostEditor 元件支援文章建立和編輯
  - 建立 PostActions 元件提供編輯和刪除按鈕
  - 實作文章內容驗證和錯誤顯示邏輯
  - 加入非同步操作狀態顯示和進度追蹤
  - _需求: 2.1, 2.2, 2.3, 2.5, 2.6, 2.7_

- [ ] 6.3 建立文章服務和 API 整合
  - 實作 postService 處理所有文章相關的 API 呼叫
  - 建立 usePost 和 usePosts 自訂 Hook 管理文章狀態
  - 實作錯誤處理和重試邏輯
  - 撰寫文章服務和 Hook 的單元測試
  - _需求: 2.1, 2.2, 2.3, 4.3, 4.4, 5.5_

- [ ] 7. 實作全域錯誤處理和安全機制
- [ ] 7.1 建立後端全域異常處理
  - 實作 GlobalExceptionHandler 處理各種異常類型
  - 建立標準化的錯誤回應格式
  - 實作 API 呼叫頻率限制機制
  - 撰寫異常處理的單元測試
  - _需求: 4.3, 4.4_

- [ ] 7.2 實作前端錯誤處理和使用者體驗
  - 建立 ErrorBoundary 元件捕獲 React 錯誤
  - 實作全域錯誤通知系統
  - 建立 LoadingSpinner 和 ErrorMessage 通用元件
  - 實作網路錯誤重試機制
  - _需求: 4.4, 5.6_

- [ ] 8. 建立完整的測試套件
- [ ] 8.1 撰寫後端整合測試
  - 建立完整的 API 整合測試，涵蓋所有端點
  - 實作資料庫測試，使用測試資料庫和測試資料
  - 撰寫安全測試，驗證認證和授權機制
  - 建立效能測試，測試非同步操作和併發處理
  - _需求: 1.1-1.6, 2.1-2.7, 3.1-3.4, 4.1-4.4_

- [ ] 8.2 撰寫前端端到端測試
  - 使用 Cypress 建立完整的使用者流程測試
  - 測試 Google OAuth 登入流程（使用模擬）
  - 測試文章建立、編輯、刪除的完整流程
  - 測試錯誤處理和邊界條件
  - _需求: 1.1-1.6, 2.1-2.7, 3.1-3.4_

- [ ] 9. 配置部署和環境設定
- [ ] 9.1 建立 Docker 容器化配置
  - 建立後端 Dockerfile 和 docker-compose.yml
  - 建立前端 Dockerfile 和建置配置
  - 配置資料庫和 Redis 容器
  - 撰寫部署腳本和環境變數配置
  - _需求: 5.1, 5.2, 5.5_

- [ ] 9.2 配置 CORS 和生產環境設定
  - 配置 Spring Boot CORS 設定，限制允許的來源
  - 建立生產環境的 application.yml 配置
  - 配置前端建置和部署設定
  - 實作健康檢查端點和監控配置
  - _需求: 4.6, 5.4, 5.5, 5.6_
# Implementation Plan

- [x] 1. 創建 ID 生成器服務
  - 實作 MessageIdGenerator 服務類，包含 36 位 ID 生成邏輯
  - 使用時間戳、隨機數和機器標識的組合確保唯一性
  - 實作 ID 格式驗證方法
  - 添加 ID 唯一性檢查功能
  - 編寫單元測試驗證 ID 生成的正確性和唯一性
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. 更新 Message 實體類
  - 將 Message 實體的 id 欄位從 Long 改為 String 類型
  - 添加 @PrePersist 方法自動生成 ID
  - 更新 JPA 註解以支援 VARCHAR(36) 類型
  - 編寫單元測試驗證實體類的變更
  - _Requirements: 1.1, 4.1, 4.3_

- [x] 3. 更新 Repository 介面
  - 將 MessageRepository 的泛型參數從 Long 改為 String
  - 更新所有查詢方法以支援新的 ID 類型
  - 確保現有的自定義查詢方法正常工作
  - 編寫整合測試驗證 Repository 功能
  - _Requirements: 2.4, 4.2_

- [x] 4. 更新 MessageService 服務層
  - 修改所有方法的參數類型從 Long 改為 String
  - 在服務方法中添加 ID 格式驗證
  - 更新 createMessage 方法以使用新的 ID 生成器
  - 更新 getMessageById、updateMessage、deleteMessage 方法
  - 編寫單元測試驗證服務層的所有變更
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 5. 更新 MessageController 控制器
  - 將路徑參數 messageId 的類型從 Long 改為 String
  - 在控制器方法中添加 ID 格式驗證
  - 更新錯誤處理以支援無效 ID 格式的情況
  - 確保 API 響應格式保持一致
  - 編寫控制器測試驗證 API 端點功能
  - _Requirements: 2.2, 2.3, 6.1, 6.3_

- [x] 6. 創建數據庫遷移腳本
  - 編寫 SQL 腳本將 id 欄位從 BIGINT 改為 VARCHAR(36)
  - 為現有數據生成新格式的 ID
  - 更新相關的索引和約束
  - 編寫測試驗證遷移腳本的正確性
  - _Requirements: 4.1, 4.2, 4.4_

- [x] 7. 添加 ID 格式驗證和錯誤處理
  - 創建 InvalidMessageIdException 異常類
  - 添加全域異常處理器處理 ID 格式錯誤
  - 實作統一的錯誤響應格式
  - 編寫測試驗證錯誤處理邏輯
  - _Requirements: 6.2, 6.3_

- [x] 8. 更新前端 API 調用
  - 修改 api.js 中的所有 message 相關 API 調用
  - 添加前端 ID 格式驗證函數
  - 更新錯誤處理以支援新的錯誤類型
  - 編寫前端單元測試驗證 API 調用
  - _Requirements: 3.3, 5.3_

- [x] 9. 更新前端組件以支援新 ID 格式
  - 修改 MessageList 組件處理新的 ID 格式
  - 更新 SingleMessage 組件的路由參數處理
  - 確保所有 message 相關的前端操作正常工作
  - 更新 URL 路由以支援新的 ID 格式
  - 編寫組件測試驗證前端功能
  - _Requirements: 3.1, 3.2, 3.4, 5.2_

- [x] 10. 更新前端路由和導航
  - 修改 React Router 配置以支援新的 ID 格式
  - 更新所有包含 message ID 的連結和導航
  - 確保瀏覽器前進後退功能正常
  - 編寫 E2E 測試驗證路由功能
  - _Requirements: 3.2, 5.2_

- [x] 11. 編寫整合測試
  - 創建端到端測試驗證完整的 message 流程
  - 測試新 ID 格式的 CRUD 操作
  - 驗證前後端整合的正確性
  - 測試錯誤場景和邊界條件
  - _Requirements: 5.1, 5.4_

- [ ] 12. 性能測試和優化
  - 測試新 ID 生成器的性能
  - 驗證數據庫查詢性能沒有退化
  - 測試大量數據下的系統表現
  - 優化索引和查詢效率
  - _Requirements: 4.2, 5.4_

- [ ] 13. 更新文檔和配置
  - 更新 API 文檔以反映新的 ID 格式
  - 修改 OpenAPI 規範文件
  - 更新部署和配置文檔
  - 編寫遷移指南和操作手冊
  - _Requirements: 6.1, 6.4_
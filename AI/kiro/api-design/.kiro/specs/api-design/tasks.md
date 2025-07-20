# Implementation Plan

- [x] 1. 建立專案結構和核心配置
  - 建立 Spring Boot 3.x 專案結構
  - 配置 Gradle build.gradle 檔案，包含所需依賴（Spring Boot, Docker Java API, H2, JPA, Redis, Spring Cache, Spring Async）
  - 建立 application.yml 配置檔案，包含快取和非同步執行器配置
  - 設定基本的 package 結構（controller, service, repository, model, dto, exception, async, cache）
  - 配置 Redis 連接和快取管理器
  - 配置非同步任務執行器和執行緒池
  - _Requirements: 10.1, 10.2, 10.3_

- [x] 2. 實作資料模型和狀態管理
- [x] 2.1 建立 Instance 實體和狀態枚舉
  - 實作 Instance JPA 實體類別，包含所有必要欄位
  - 建立 InstanceState 枚舉，定義所有 EC2 狀態
  - 實作 Instance 實體的 equals, hashCode, toString 方法
  - 建立對應的單元測試
  - _Requirements: 1.2, 2.4, 7.1_

- [x] 2.3 建立 Operation 實體和相關枚舉
  - 實作 AsyncOperation JPA 實體類別，用於追蹤非同步操作
  - 建立 OperationType 和 OperationStatus 枚舉
  - 實作 Operation 實體的 equals, hashCode, toString 方法
  - 建立對應的單元測試
  - _Requirements: 8.1, 8.2, 8.4_

- [x] 2.2 實作狀態機制和轉換邏輯
  - 建立 InstanceStateMachine 組件，定義完整的狀態轉換規則（包含 TERMINATED 狀態）
  - 實作狀態轉換驗證方法和終端狀態檢查
  - 實作狀態轉換圖中定義的所有有效轉換路徑
  - 建立狀態轉換的單元測試，涵蓋所有有效和無效轉換，包含狀態機圖驗證
  - _Requirements: 7.1, 7.2, 7.3, 7.6_

- [ ] 3. 建立資料存取層
- [ ] 3.1 實作 Instance Repository 介面和實作
  - 建立 InstanceRepository 繼承 JpaRepository，支援分頁查詢
  - 實作自定義查詢方法（按狀態查詢、按 imageId 查詢等）
  - 加入分頁和排序支援的查詢方法
  - 建立 @DataJpaTest 測試類別，測試所有 repository 方法包含分頁功能
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 3.2 實作 Operation Repository 介面和實作
  - 建立 OperationRepository 繼承 JpaRepository，支援分頁查詢
  - 實作按狀態、instanceId 查詢的方法
  - 實作操作記錄清理的查詢方法
  - 建立對應的單元測試
  - _Requirements: 8.1, 8.6_

- [ ] 4. 實作 Docker 整合服務
- [ ] 4.1 建立 Docker 服務抽象層
  - 實作 DockerService 類別，封裝 Docker Java API 操作
  - 實作 container 的建立、啟動、停止、重啟、刪除方法
  - 建立 Docker 連接配置和錯誤處理
  - 實作 container 狀態查詢方法
  - _Requirements: 1.1, 3.1, 4.1, 5.1, 6.1_

- [ ] 4.2 建立 Docker 服務測試
  - 使用 Testcontainers 建立 Docker 整合測試
  - 測試所有 Docker 操作方法
  - 實作 Mock 版本的 DockerService 供其他測試使用
  - _Requirements: 1.4, 3.5, 4.5, 5.5, 6.5_

- [ ] 5. 實作核心業務邏輯服務
- [ ] 5.1 建立 Instance 服務基礎結構
  - 實作 InstanceService 類別的基本架構，支援快取和分頁
  - 注入 InstanceRepository、DockerService 和 CacheManager 依賴
  - 實作 getInstance 查詢方法，加入 @Cacheable 註解
  - 實作 listInstances 分頁查詢方法，支援狀態和 imageId 篩選
  - 建立基本的單元測試框架
  - _Requirements: 2.1, 2.2, 2.3, 9.1, 9.3_

- [ ] 5.8 建立 Operation 服務
  - 實作 OperationService 類別，管理非同步操作記錄
  - 實作 createOperation、getOperation、listOperations 方法
  - 實作 updateOperationStatus、completeOperation、failOperation 方法
  - 加入定期清理已完成操作的排程任務
  - 建立 OperationService 的完整單元測試
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

- [ ] 5.9 建立非同步任務服務
  - 實作 AsyncTaskService 類別，處理長時間運行的操作
  - 實作各種非同步操作方法（create, start, stop, restart, terminate）
  - 加入 @Async 註解和 CompletableFuture 回傳類型
  - 實作操作進度追蹤和狀態更新機制
  - 建立非同步任務的單元測試和整合測試
  - _Requirements: 1.1, 1.4, 3.1, 3.5, 4.1, 4.5, 5.1, 5.5, 6.1, 6.5_

- [ ] 5.2 實作非同步 Instance 建立邏輯
  - 實作 createInstanceAsync 方法，返回 AsyncOperation 物件
  - 實作 ID 生成邏輯和初始狀態設定
  - 整合 AsyncTaskService 進行背景處理
  - 加入快取清除機制（@CacheEvict）
  - 建立非同步建立的完整單元測試
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

- [ ] 5.3 實作非同步 Instance 啟動邏輯
  - 實作 startInstanceAsync 方法，返回 AsyncOperation 物件
  - 實作狀態驗證和錯誤處理
  - 整合 AsyncTaskService 進行背景處理
  - 加入快取清除機制（@CacheEvict）
  - 建立非同步啟動的完整單元測試
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [ ] 5.4 實作非同步 Instance 停止邏輯
  - 實作 stopInstanceAsync 方法，返回 AsyncOperation 物件
  - 實作優雅停止和強制停止邏輯
  - 整合 AsyncTaskService 進行背景處理
  - 加入快取清除機制（@CacheEvict）
  - 建立非同步停止的完整單元測試
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [ ] 5.5 實作非同步 Instance 重啟邏輯
  - 實作 restartInstanceAsync 方法，返回 AsyncOperation 物件
  - 實作重啟過程的狀態管理
  - 整合 AsyncTaskService 進行背景處理
  - 加入快取清除機制（@CacheEvict）
  - 建立非同步重啟的完整單元測試
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [ ] 5.6 實作非同步 Instance 刪除邏輯
  - 實作 terminateInstanceAsync 方法，返回 AsyncOperation 物件
  - 實作強制刪除和優雅刪除邏輯
  - 實作 TERMINATED 狀態的資料庫記錄自動清理機制
  - 整合 AsyncTaskService 進行背景處理
  - 加入快取清除機制（@CacheEvict）
  - 建立非同步刪除的完整單元測試，包含狀態轉換驗證
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 7.7_

- [ ] 5.7 實作狀態同步和恢復機制
  - 實作 syncInstanceStates 方法，同步 Instance 狀態與 Docker container 實際狀態
  - 實作 recoverInstanceStates 方法，在應用啟動時恢復狀態一致性
  - 實作 cleanupTerminatedInstances 定期清理任務
  - 加入狀態不一致時的自動修復邏輯
  - 建立狀態同步和恢復的單元測試
  - _Requirements: 7.8_

- [ ] 6. 建立 DTO 和資料轉換
- [ ] 6.1 實作請求和回應 DTO
  - 建立 CreateInstanceRequest, InstanceResponse, ContainerInfo 等 DTO
  - 建立 AsyncOperationResponse, OperationResponse, PagedResponse 等 DTO
  - 實作 DTO 驗證註解（@Valid, @NotNull 等）
  - 建立 Instance 和 Operation 實體與 DTO 之間的轉換方法
  - 實作 DTO 轉換的單元測試
  - _Requirements: 10.3, 10.4, 10.7_

- [ ] 7. 實作 REST API 控制器
- [ ] 7.1 建立基礎控制器結構
  - 實作 InstanceController 類別的基本架構
  - 注入 InstanceService 依賴
  - 設定基本的 RequestMapping 和路由
  - 建立控制器的基礎測試類別
  - _Requirements: 8.1, 8.2_

- [ ] 7.2 實作 Instance 查詢 API 端點
  - 實作 GET /instances/{id}（查詢單個）端點，支援快取
  - 實作 GET /instances（查詢列表）端點，支援分頁、排序和篩選
  - 加入分頁參數驗證（page, size, sort）
  - 實作 PagedResponse 回應格式
  - 建立這些端點的 @WebMvcTest 測試，包含分頁和快取測試
  - _Requirements: 2.1, 2.2, 2.3, 2.6, 2.7, 10.7_

- [ ] 7.3 實作非同步 Instance 操作 API 端點
  - 實作 POST /instances（建立）端點，返回 202 Accepted 和 AsyncOperationResponse
  - 實作 POST /instances/{id}/start、POST /instances/{id}/stop、POST /instances/{id}/restart 端點
  - 實作 DELETE /instances/{id} 端點
  - 所有操作端點都返回 202 Accepted 和 operation ID
  - 加入操作權限驗證和狀態檢查
  - 建立這些端點的 @WebMvcTest 測試
  - _Requirements: 1.1, 1.6, 3.1, 3.6, 4.1, 4.6, 5.1, 5.6, 6.1, 6.6, 10.3_

- [ ] 7.4 實作 Operation 追蹤 API 端點
  - 實作 OperationController 類別
  - 實作 GET /operations/{operationId}（查詢單個操作）端點
  - 實作 GET /operations（查詢操作列表）端點，支援分頁和篩選
  - 加入操作狀態和進度回應
  - 建立 OperationController 的 @WebMvcTest 測試
  - _Requirements: 8.1, 8.2, 8.3, 8.7_

- [ ] 8. 實作全域異常處理
- [ ] 8.1 建立自定義異常類別
  - 實作 InstanceNotFoundException, InvalidStateTransitionException, DockerOperationException
  - 建立異常類別的繼承結構
  - 實作異常訊息的國際化支援
  - _Requirements: 8.2, 8.4_

- [ ] 8.2 實作全域異常處理器
  - 建立 @RestControllerAdvice 全域異常處理器
  - 實作各種異常的處理方法和 HTTP 狀態碼映射
  - 建立統一的錯誤回應格式
  - 加入非同步操作相關異常處理
  - 建立異常處理器的單元測試
  - _Requirements: 10.2, 10.5, 10.6_

- [ ] 8.3 實作快取管理和配置
  - 建立 CacheConfig 配置類別，配置 Redis 和本地快取
  - 實作快取失效策略和 TTL 設定
  - 實作快取預熱機制
  - 建立自定義快取 Key 生成器
  - 實作快取監控和統計功能
  - 建立快取相關的單元測試和整合測試
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

- [ ] 9. 建立整合測試
- [ ] 9.1 實作 API 整合測試
  - 建立 @SpringBootTest 整合測試類別
  - 使用 TestRestTemplate 測試完整的 API 流程
  - 實作測試資料的自動建立和清理
  - 測試完整的 Instance 生命週期操作
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_

- [ ] 9.2 實作 Docker 整合測試
  - 使用 Testcontainers 建立真實的 Docker 環境測試
  - 測試與實際 Docker daemon 的整合
  - 驗證狀態同步的正確性
  - 測試錯誤恢復機制
  - _Requirements: 7.4_

- [ ] 10. 實作應用程式配置和啟動
- [ ] 10.1 完善應用程式配置
  - 完善 application.yml 配置，包含資料庫、Docker、日誌設定
  - 建立不同環境的配置檔案（dev, test, prod）
  - 實作配置屬性的驗證
  - 建立 Docker Compose 檔案用於本地開發
  - _Requirements: 8.1, 8.2_

- [ ] 10.2 實作應用程式主類別和健康檢查
  - 建立 Spring Boot 主應用程式類別
  - 整合 Spring Boot Actuator 提供健康檢查端點
  - 實作自定義健康檢查指標（Docker 連接狀態）
  - 建立應用程式啟動和關閉的整合測試
  - _Requirements: 7.4_
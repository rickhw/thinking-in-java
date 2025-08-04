# Design Document

## Overview

本設計文檔描述了將 message ID 從 Long 類型自增 ID 改為 36 位長度大寫字母與數字組成的唯一 ID 的實現方案。這個改變將提供更好的安全性、可擴展性和分散式系統支援，同時確保系統的向後兼容性。

新的 ID 格式將採用類似 UUID 的結構，但使用自定義的字符集（大寫字母 A-Z 和數字 0-9），總長度為 36 位，提供足夠的唯一性保證。

## Architecture

### 系統架構概覽

```mermaid
graph TB
    A[Frontend React App] --> B[REST API Controller]
    B --> C[Message Service]
    C --> D[ID Generator Service]
    C --> E[Message Repository]
    E --> F[MySQL Database]
    
    subgraph "ID Generation"
        D --> G[Custom ID Algorithm]
        G --> H[Uniqueness Validation]
    end
    
    subgraph "Database Layer"
        F --> I[messages table]
        I --> J[VARCHAR(36) id column]
    end
```

### 核心變更領域

1. **數據庫層**: Message 表的 ID 欄位從 BIGINT 改為 VARCHAR(36)
2. **服務層**: 新增 ID 生成服務和相關邏輯
3. **控制器層**: 路徑參數類型從 Long 改為 String
4. **前端層**: 處理新格式的 ID 字符串

## Components and Interfaces

### 1. ID Generator Service

**職責**: 生成 36 位長度的唯一 ID

```java
@Service
public class MessageIdGenerator {
    
    /**
     * 生成 36 位長度的唯一 ID
     * 格式: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
     * 字符集: A-Z, 0-9
     */
    public String generateId();
    
    /**
     * 驗證 ID 格式是否正確
     */
    public boolean isValidId(String id);
    
    /**
     * 檢查 ID 是否已存在於數據庫中
     */
    public boolean isIdUnique(String id);
}
```

**實現策略**:
- 使用時間戳 + 隨機數 + 機器標識的組合
- 採用 Base32 編碼（使用 A-Z, 2-7 字符集）確保大寫字母和數字
- 添加校驗位確保數據完整性
- 格式: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX` (8-4-4-4-12)

### 2. Updated Message Entity

```java
@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;  // 從 Long 改為 String
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 構造函數中自動生成 ID
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = messageIdGenerator.generateId();
        }
    }
}
```

### 3. Updated Repository Interface

```java
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    @Query("SELECT m FROM Message m WHERE m.userId = :userId ORDER BY m.createdAt DESC")
    Page<Message> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT m FROM Message m ORDER BY m.createdAt DESC")
    Page<Message> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Message> findByUserId(String userId, Pageable pageable);
    
    // 新增: 檢查 ID 是否存在
    boolean existsById(String id);
}
```

### 4. Updated Controller

```java
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    
    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable String messageId) {
        // 添加 ID 格式驗證
        if (!messageIdGenerator.isValidId(messageId)) {
            return ResponseEntity.badRequest().build();
        }
        
        return messageService.getMessageById(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{messageId}")
    public ResponseEntity<TaskResponse> updateMessage(
            @PathVariable String messageId,
            @RequestBody UpdateMessageRequest request) {
        // ID 格式驗證邏輯
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<TaskResponse> deleteMessage(@PathVariable String messageId) {
        // ID 格式驗證邏輯
    }
}
```

### 5. Updated Service Layer

```java
@Service
public class MessageService {
    
    private final MessageIdGenerator messageIdGenerator;
    
    @Async
    public CompletableFuture<String> createMessage(Message message) {
        String taskId = UUID.randomUUID().toString();
        taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        
        try {
            // 確保 message 有有效的 ID
            if (message.getId() == null) {
                message.setId(messageIdGenerator.generateId());
            }
            
            messageRepository.save(message);
            taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
            return CompletableFuture.completedFuture(taskId);
        } catch (Exception e) {
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
    
    public Optional<Message> getMessageById(String id) {
        if (!messageIdGenerator.isValidId(id)) {
            return Optional.empty();
        }
        return messageRepository.findById(id);
    }
    
    // 其他方法類似更新...
}
```

## Data Models

### Database Schema Changes

**現有 Schema**:
```sql
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**新 Schema**:
```sql
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_messages_id (id),
    INDEX idx_messages_user_id (user_id),
    INDEX idx_messages_created_at (created_at)
);
```

### Migration Strategy

由於這是一個破壞性變更，需要謹慎的遷移策略：

1. **階段 1**: 添加新的 ID 欄位
   - 添加 `new_id VARCHAR(36)` 欄位
   - 為所有現有記錄生成新 ID
   - 建立新 ID 的索引

2. **階段 2**: 應用程式更新
   - 更新應用程式代碼使用新 ID
   - 保持對舊 ID 的向後兼容性（暫時）

3. **階段 3**: 完成遷移
   - 刪除舊的 `id` 欄位
   - 將 `new_id` 重命名為 `id`
   - 更新所有約束和索引

### ID Format Specification

**格式**: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`

**字符集**: A-Z, 0-9 (32 個字符)

**結構**:
- 前 8 位: 時間戳編碼 (精確到秒)
- 中間 8 位: 隨機數
- 後 20 位: 機器標識 + 序列號 + 校驗位

**唯一性保證**:
- 時間戳確保時間唯一性
- 機器標識確保多實例唯一性
- 隨機數和序列號確保同一時刻的唯一性
- 總計算空間: 32^36 ≈ 1.2 × 10^54

## Error Handling

### ID 驗證錯誤

```java
public class InvalidMessageIdException extends RuntimeException {
    public InvalidMessageIdException(String messageId) {
        super("Invalid message ID format: " + messageId);
    }
}

@ExceptionHandler(InvalidMessageIdException.class)
public ResponseEntity<ErrorResponse> handleInvalidMessageId(InvalidMessageIdException e) {
    return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_MESSAGE_ID", e.getMessage()));
}
```

### 前端錯誤處理

```javascript
// API 調用中的 ID 驗證
export const getMessageById = async (messageId) => {
    // 基本格式驗證
    if (!isValidMessageId(messageId)) {
        throw new Error('Invalid message ID format');
    }
    
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`);
    if (!response.ok) {
        if (response.status === 400) {
            throw new Error('Invalid message ID format');
        }
        if (response.status === 404) {
            throw new Error('Message not found');
        }
        throw new Error(`Network response was not ok (${response.status})`);
    }
    return response.json();
};

// ID 格式驗證函數
const isValidMessageId = (id) => {
    const pattern = /^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$/;
    return pattern.test(id);
};
```

## Testing Strategy

### 單元測試

1. **ID Generator Tests**
   - 測試 ID 格式正確性
   - 測試 ID 唯一性
   - 測試大量生成的性能
   - 測試邊界條件

2. **Service Layer Tests**
   - 測試新 ID 格式的 CRUD 操作
   - 測試 ID 驗證邏輯
   - 測試錯誤處理

3. **Controller Tests**
   - 測試路徑參數驗證
   - 測試 API 響應格式
   - 測試錯誤響應

### 整合測試

1. **Database Integration Tests**
   - 測試新 schema 的數據操作
   - 測試索引性能
   - 測試約束驗證

2. **API Integration Tests**
   - 測試完整的 API 流程
   - 測試前後端整合
   - 測試錯誤場景

### 前端測試

1. **Component Tests**
   - 測試新 ID 格式的顯示
   - 測試路由參數處理
   - 測試錯誤狀態顯示

2. **E2E Tests**
   - 測試完整的用戶流程
   - 測試頁面導航
   - 測試錯誤恢復

### 性能測試

1. **ID Generation Performance**
   - 測試 ID 生成速度
   - 測試並發生成
   - 測試記憶體使用

2. **Database Query Performance**
   - 測試新 ID 格式的查詢性能
   - 測試索引效果
   - 測試大數據量場景

## Migration Plan

### 準備階段
1. 備份現有數據庫
2. 在測試環境驗證遷移腳本
3. 準備回滾計劃

### 執行階段
1. 停止應用服務
2. 執行數據庫 schema 變更
3. 為現有數據生成新 ID
4. 部署新版本應用
5. 驗證功能正常

### 驗證階段
1. 檢查所有 API 端點
2. 驗證前端功能
3. 檢查數據完整性
4. 監控系統性能

這個設計確保了系統能夠平滑地從舊的 Long 類型 ID 遷移到新的 36 位字符串 ID，同時保持良好的性能和用戶體驗。
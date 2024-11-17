

## Prompt

我想開發一個應用程式，這個應用程式處理資源的狀態機，處理過程使用 rabbitmq 的 queue，每個狀態移轉 (Transition) 的邏輯，則在程式裡面的 Consumer Handler 裡實作。

這個程式用 java 17, springboot 3.2, gradle, rabbitmq 作為基礎，請完成完整的程式碼框架，滿足以下條件：

- 資源種類部分：
    - 有一個叫做 Tenant 的資源，起始與終止狀態為 Starting / Terminated，其他狀態還有 Active / Inactive
- 核心狀態機移轉處理架構：
    - 使用 CQRS 方式實作
    - 每個移轉 (Transition) 都透過獨立的 method 處理, 例如從 active to inactive 是一個 method. 所以四個狀態最多有 16 個排列組合，但是同樣狀態不需要處理。
- RabbitMQ 部分:
    - 每種資源類型，提供一個 Queue 與多個 binding key
    - 幫我配置好 RabbitMQ 初始設定，像是發現 Queue / Exchange 沒有建立，就自動建立
    - 未來可以透過 application.yaml 增加不同資源的 queue
- java 部分：
    - 提供完整的程式碼，包含程式架構，以及能夠順利執行的必要 class
    - 請提供 build.gradle, 並且幫我確認都可以正常編譯
    - 這個應用程式的 package name 叫做: com.gtcafe.app
    - 幫我完善程式碼，包含使用 lombok 簡化程式內容
- 提供 curl 的測試案例，讓我驗證以下：
    - 建立 tenant, 狀態 從 initing to active
    - 查詢 tenant 資訊
    - 改變 tenant 為 inactive
    - 修改 tenant 為 terminated

請幫我完成上述需求的完整程式，並且打包成 zip



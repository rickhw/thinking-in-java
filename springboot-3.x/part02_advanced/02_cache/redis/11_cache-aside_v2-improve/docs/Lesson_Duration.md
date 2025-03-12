
`Duration` 是來自 `java.time.Duration`，這是 Java 8 引入的時間 API 的一部分。完整的 import 語句應該是：

```java
import java.time.Duration;
```

`java.time.Duration` 用於表示一段時間的持續時間，例如 "3 分鐘" 或 "2 小時" 等。它提供了很多便利的方法來創建和操作時間段：

```java
// 創建 Duration 的一些常用方法
Duration.ofSeconds(30);    // 30 秒
Duration.ofMinutes(5);     // 5 分鐘
Duration.ofHours(2);       // 2 小時
Duration.ofDays(1);        // 1 天

// 獲取時間值的方法
duration.getSeconds();     // 獲取總秒數
duration.toMillis();       // 轉換為毫秒
duration.toMinutes();      // 轉換為分鐘
```

在我們的 `setIfNotExists` 方法中，使用 `Duration` 作為參數類型比直接使用 `long` 更有優勢，因為：
1. 更清楚地表達了這個參數的用途是表示時間
2. 提供了更好的類型安全性
3. 可以方便地進行時間單位的轉換
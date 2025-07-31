以下是一個**不使用資料庫**的簡單 Java 範例，來展示 **樂觀鎖**與**悲觀鎖**的特性與差異。

我們模擬一個「多個執行緒同時嘗試扣除庫存」的情境：

---

## 🔧 基本設定

我們有一個 `Inventory` 類別，裡面有一個 `stock = 100`。每個執行緒都會嘗試將庫存減 1。

---

## ✅ 樂觀鎖實作（使用 `AtomicInteger` 模擬 version）

```java
import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticLockDemo {
    private static int stock = 100;
    private static final AtomicInteger version = new AtomicInteger(0);

    public static void main(String[] args) {
        Runnable task = () -> {
            int retries = 5;
            while (retries-- > 0) {
                int currentVersion = version.get();
                if (stock > 0) {
                    // 模擬延遲與並發情境
                    try { Thread.sleep((long)(Math.random() * 10)); } catch (InterruptedException ignored) {}
                    // 嘗試更新 version (模擬 CAS)
                    if (version.compareAndSet(currentVersion, currentVersion + 1)) {
                        stock--;
                        System.out.println(Thread.currentThread().getName() + " 成功扣庫存，剩餘：" + stock);
                        break;
                    } else {
                        System.out.println(Thread.currentThread().getName() + " 發現版本衝突，重試...");
                    }
                }
            }
        };

        for (int i = 0; i < 110; i++) {
            new Thread(task).start();
        }
    }
}
```

### ✨ 重點：

* 不加鎖，每個執行緒樂觀地假設沒人跟我搶。
* 如果有人搶先了，我會根據版本衝突（`compareAndSet`）進行重試。

---

## 🔒 悲觀鎖實作（使用 `synchronized`）

```java
public class PessimisticLockDemo {
    private static int stock = 100;

    public static void main(String[] args) {
        Runnable task = () -> {
            synchronized (PessimisticLockDemo.class) {
                if (stock > 0) {
                    try { Thread.sleep((long)(Math.random() * 10)); } catch (InterruptedException ignored) {}
                    stock--;
                    System.out.println(Thread.currentThread().getName() + " 扣庫存成功，剩餘：" + stock);
                } else {
                    System.out.println(Thread.currentThread().getName() + " 沒有庫存了");
                }
            }
        };

        for (int i = 0; i < 110; i++) {
            new Thread(task).start();
        }
    }
}
```

### ✨ 重點：

* 每次操作都上鎖，只有一個執行緒可以進來。
* 避免任何衝突，但犧牲了並發性。

---

## 📊 差異觀察

| 特性     | 樂觀鎖        | 悲觀鎖      |
| ------ | ---------- | -------- |
| 衝突處理方式 | 發生時重試      | 一開始就排他處理 |
| 並發效率   | 高，但需處理重試邏輯 | 低，但簡單安全  |
| 適合場景   | 衝突機率低      | 衝突機率高    |

---

如需將這個邏輯移植到真實的應用場景（如 Redis、檔案、記憶體快取系統等），都能依此原則延伸。

要我幫你補上改寫成 Redis 或非同步 queue 的版本嗎？

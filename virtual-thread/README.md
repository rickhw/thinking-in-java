

Java 19 引入了 Virtual Threads，為傳統的 Java  threading model 帶來了一個重大變革。以下是 Virtual Threads 與傳統 Thread 的主要差異，以及範例程式碼展示其效能測試。

### 差異：
1. **傳統 Thread (Platform Threads)**：
   - 每個 Thread 直接對應一個操作系統的 thread，這意味著其數量受限於底層操作系統的資源（如 CPU 核心）。
   - 螢光筆：切換 thread 的成本較高，尤其在高併發情況下，會有更多的 thread context switching 和阻塞。

2. **Virtual Threads**：
   - Virtual Threads 是輕量級的 thread，並不直接對應到操作系統的 thread。
   - 數量上能夠達到數百萬級別，理論上解決了傳統 Thread 無法支撐大量併發的問題。
   - 螢光筆：Virtual Threads 是由 JVM 管理的，能夠在 blocking I/O 時釋放出來，而不用佔用真正的操作系統資源。

### 範例程式碼：
我們來進行一個效能測試，展示傳統 Thread 與 Virtual Thread 在大量併發情況下的表現差異。

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPerformanceTest {
    public static void main(String[] args) throws InterruptedException {
        int taskCount = 100_000;  // 要啟動的任務數量

        System.out.println("==== Using Platform Threads ====");
        long startTime = System.currentTimeMillis();
        testWithPlatformThreads(taskCount);
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + "ms\n");

        System.out.println("==== Using Virtual Threads ====");
        startTime = System.currentTimeMillis();
        testWithVirtualThreads(taskCount);
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    // 傳統的 Platform Threads 測試
    private static void testWithPlatformThreads(int taskCount) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(100);  // 使用固定大小的 thread pool
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                simulateTask();
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
    }

    // Virtual Threads 測試
    private static void testWithVirtualThreads(int taskCount) throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();  // 使用 virtual thread 執行器
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                simulateTask();
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
    }

    // 模擬任務 (例如 I/O 任務)
    private static void simulateTask() {
        try {
            Thread.sleep(10);  // 模擬一些延遲 (例如阻塞 I/O)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### 程式碼說明：
- **Platform Threads 測試**：我們使用 `Executors.newFixedThreadPool(100)` 創建了一個固定大小的 thread pool，模擬傳統的 thread 併發。
- **Virtual Threads 測試**：我們使用 `Executors.newVirtualThreadPerTaskExecutor()`，這個 API 是 Java 19 新增的，專門用來生成 virtual threads。
- **simulateTask()**：模擬了一個會阻塞的任務（如 I/O 操作），以觀察 virtual thread 和傳統 thread 處理阻塞任務的效率差異。

### 測試結果：
你可以執行這個程式碼，根據你的 CPU 核心數和系統環境，會發現 Virtual Threads 能夠支撐更多的併發任務，且在某些情況下能顯著降低 thread context switching 帶來的開銷，尤其是當有大量 I/O-bound 任務時。


---

## 實際執行

```bash
~/repos/thinking-in-java/virtual-thread$ javac ThreadPerformanceTest.java


~/repos/thinking-in-java/virtual-thread$ java ThreadPerformanceTest
==== Using Platform Threads ====
Time taken: 10031ms

==== Using Virtual Threads ====
Time taken: 602ms


~/repos/thinking-in-java/virtual-thread$ java ThreadPerformanceTest
==== Using Platform Threads ====
Time taken: 10029ms

==== Using Virtual Threads ====
Time taken: 703ms

```


---

## Reference

- https://liaoxuefeng.com/books/java/threading/virtual-thread/index.html
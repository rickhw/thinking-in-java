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
package com.gtcafe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thread-test")
@Slf4
public class ThreadTestController {
    
    
    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;
    
    @GetMapping("/info")
    public Map<String, Object> getThreadInfo() {
        Thread currentThread = Thread.currentThread();
        Map<String, Object> info = new HashMap<>();
        info.put("threadName", currentThread.getName());
        info.put("isVirtual", currentThread.isVirtual());
        info.put("virtualThreadsEnabled", virtualThreadsEnabled);
        return info;
    }
    
    @GetMapping("/performance")
    public Map<String, Object> testPerformance(
            @RequestParam(defaultValue = "10000") int taskCount,
            @RequestParam(defaultValue = "10") int sleepTimeMs) {
        
        long startTime;
        Map<String, Object> results = new HashMap<>();
        
        try {
            if (virtualThreadsEnabled) {
                logger.info("Running performance test with Virtual Threads for {} tasks", taskCount);
                startTime = System.currentTimeMillis();
                testWithVirtualThreads(taskCount, sleepTimeMs);
                long virtualTime = System.currentTimeMillis() - startTime;
                results.put("threadType", "Virtual Threads");
                results.put("timeTakenMs", virtualTime);
                results.put("tasksCompleted", taskCount);
            } else {
                logger.info("Running performance test with Platform Threads for {} tasks", taskCount);
                startTime = System.currentTimeMillis();
                testWithPlatformThreads(taskCount, sleepTimeMs);
                long platformTime = System.currentTimeMillis() - startTime;
                results.put("threadType", "Platform Threads");
                results.put("timeTakenMs", platformTime);
                results.put("tasksCompleted", taskCount);
            }
        } catch (InterruptedException e) {
            results.put("error", e.getMessage());
        }
        
        return results;
    }
    
    @GetMapping("/compare")
    public Map<String, Object> comparePerformance(
            @RequestParam(defaultValue = "10000") int taskCount,
            @RequestParam(defaultValue = "10") int sleepTimeMs) {
        
        Map<String, Object> results = new HashMap<>();
        
        try {
            logger.info("Starting performance comparison test");
            
            // Platform Threads Test
            logger.info("Running test with Platform Threads");
            long startTime = System.currentTimeMillis();
            testWithPlatformThreads(taskCount, sleepTimeMs);
            long platformTime = System.currentTimeMillis() - startTime;
            
            // Virtual Threads Test
            logger.info("Running test with Virtual Threads");
            startTime = System.currentTimeMillis();
            testWithVirtualThreads(taskCount, sleepTimeMs);
            long virtualTime = System.currentTimeMillis() - startTime;
            
            // Results
            results.put("taskCount", taskCount);
            results.put("sleepTimeMs", sleepTimeMs);
            results.put("platformThreadsTimeMs", platformTime);
            results.put("virtualThreadsTimeMs", virtualTime);
            results.put("improvement", String.format("%.2f%%", (platformTime - virtualTime) * 100.0 / platformTime));
            
        } catch (InterruptedException e) {
            results.put("error", e.getMessage());
        }
        
        return results;
    }

    // 传统的 Platform Threads 测试
    private void testWithPlatformThreads(int taskCount, int sleepTimeMs) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(100);  // 使用固定大小的 thread pool
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                simulateTask(sleepTimeMs);
            });
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
    }

    // Virtual Threads 测试
    private void testWithVirtualThreads(int taskCount, int sleepTimeMs) throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();  // 使用 virtual thread 执行器
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                simulateTask(sleepTimeMs);
            });
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
    }

    // 模拟任务 (例如 I/O 任务)
    private void simulateTask(int sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);  // 模拟一些延迟 (例如阻塞 I/O)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
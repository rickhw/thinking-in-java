package com.gtcafe.asimov;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    private final ThreadPoolTaskExecutor asyncExecutor;
    
    public HelloService(ThreadPoolTaskExecutor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }
    
    @Async
    public void asyncHello() {
        System.out.println(Thread.currentThread().getName() + " - 開始執行 asyncHello");
        printAsyncExecutor();
        try {
            Thread.sleep(3000); // 模擬長時間工作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println(Thread.currentThread().getName() + " - 結束執行 asyncHello");
    }

    private void printAsyncExecutor() {
        System.out.printf("corePoolSize: [%s], maxPoolSize: [%s], activeCount: [%s], poolSize: [%s], queueSize: [%s]\n", 
            asyncExecutor.getCorePoolSize(), asyncExecutor.getMaxPoolSize(),
            asyncExecutor.getActiveCount(), asyncExecutor.getPoolSize(),
            asyncExecutor.getThreadPoolExecutor().getQueue().size()
        );
    }
}

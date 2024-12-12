package com.gtcafe.asimov;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AsyncService {

    @Async
    public CompletableFuture<String> performAsyncTask() {
        log.info("Service: Starting async task");
        try {
            Thread.sleep(5000); // 模擬耗時操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Service: Completed async task");
        return CompletableFuture.completedFuture("Async process complete!");
    }
}

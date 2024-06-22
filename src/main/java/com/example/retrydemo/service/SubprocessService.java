package com.example.retrydemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class SubprocessService {

    private static final Logger logger = LoggerFactory.getLogger(SubprocessService.class);

    @Retryable(
        value = { RuntimeException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000))
    public void executeSubprocess(int id) {
        logger.info("Executing subprocess " + id + "...");
        // 模擬子程序的失敗
        if (Math.random() > 0.7) {
            logger.info("Subprocess " + id + " completed successfully.");
        } else {
            logger.error("Subprocess " + id + " failed. Retrying...");
            throw new RuntimeException("Subprocess " + id + " failed");
        }
    }

    @Recover
    public void recover(RuntimeException e, int id) {
        logger.error("Subprocess " + id + " failed after retries. Performing recovery.");
        // 實現恢復邏輯
    }
}

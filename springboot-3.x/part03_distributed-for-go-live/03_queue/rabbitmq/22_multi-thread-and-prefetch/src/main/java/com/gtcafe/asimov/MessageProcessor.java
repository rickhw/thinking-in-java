package com.gtcafe.asimov;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageProcessor {

    public boolean processMessage(String message) {
        try {
            // 模擬複雜的處理邏輯
            int sleep = (int) (Math.random() * 1000);
            log.info("Processing: {}, sleep: [{}]", message, sleep);
            
            // 這裡可以模擬處理失敗的情況
            if (message.contains("error")) {
                throw new RuntimeException("Simulated processing error");
            }

            // 模擬處理耗時
            Thread.sleep(sleep);
            
            return true;
        } catch (Exception e) {
            log.error("Message processing failed: {}", e.getMessage());
            return false;
        }
    }
}
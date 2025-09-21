package com.gtcafe.asimov;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageHandler {

    public boolean run(String message) {
        try {
            // 模擬複雜的處理邏輯
            int sleep = (int) (Math.random() * 10000);
            // int sleep = 10000;
            log.info("start - message: [{}], sleep: [{}]", message, sleep);
            
            // 這裡可以模擬處理失敗的情況
            if (message.contains("error")) {
                throw new RuntimeException("Simulated processing error");
            }

            // 模擬處理耗時
            Thread.sleep(sleep);


            log.info("finish - message: [{}], sleep: [{}]", message, sleep);
            
            return true;
        } catch (Exception e) {
            log.info("failed - message: [{}]", e.getMessage());
            return false;
        }
    }
}
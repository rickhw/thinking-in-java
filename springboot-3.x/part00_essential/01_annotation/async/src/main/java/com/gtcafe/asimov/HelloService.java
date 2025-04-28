package com.gtcafe.asimov;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    @Async
    public void asyncHello() {
        System.out.println(Thread.currentThread().getName() + " - 開始執行 asyncHello");
        try {
            Thread.sleep(3000); // 模擬長時間工作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(Thread.currentThread().getName() + " - 結束執行 asyncHello");
    }
}

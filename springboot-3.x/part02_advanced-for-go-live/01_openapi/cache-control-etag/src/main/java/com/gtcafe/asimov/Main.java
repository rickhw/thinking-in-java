package com.gtcafe.asimov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // System.out.println("\n--- 第一次請求 ---");
        // RestClient.fetchUser();

        // Thread.sleep(1000);
        // System.out.println("\n--- 第二次（ETag / Last-Modified 快取）---");
        // RestClient.fetchUser();

        // Thread.sleep(1000);
        // System.out.println("\n--- 更新使用者資訊 ---");
        // RestClient.updateUser("Rick Hwang", "rick@gtcafe.com");

        // Thread.sleep(1000);
        // System.out.println("\n--- 第三次（資料已變更）---");
        // RestClient.fetchUser();
    }


    // @Override
    // public void run(String... args) throws Exception {
    //     System.out.println("\n--- 第一次請求 ---");
    //     RestClient.fetchUser();
    //     Thread.sleep(1000);

    //     System.out.println("\n--- 第二次請求（ETag 快取）---");
    //     RestClient.fetchUser();
    // }
}

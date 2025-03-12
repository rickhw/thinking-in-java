package com.gtcafe;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            rabbitTemplate.execute(channel -> true); // 檢查連線
            System.out.println("RabbitMQ connection successful.");
        } catch (Exception e) {
            System.err.println("Failed to connect to RabbitMQ: " + e.getMessage());
            System.exit(1); // 停止應用程式
        }
    }
}

package com.gtcafe.mq.sender;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner runner(RabbitTemplate rabbitTemplate, @Value("${message.file.path}") String messageFilePath) {
        return args -> {
            if (args.length > 0 && args[0].equals("--message")) {
                String filePath = args[1];
                String message = new String(Files.readAllBytes(Paths.get(filePath)));
                rabbitTemplate.convertAndSend("messageQueue", message);
                System.out.println("Message sent: " + message);
            } else {
                System.out.println("Usage: Sender --message <path_to_message_file>");
            }
        };
    }
}

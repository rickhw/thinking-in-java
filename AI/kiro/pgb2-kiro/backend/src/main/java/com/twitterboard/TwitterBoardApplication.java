package com.twitterboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TwitterBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitterBoardApplication.class, args);
    }

}
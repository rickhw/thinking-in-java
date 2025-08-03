
package com.example.twitterclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.twitterclone.domain")
public class TwitterCloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitterCloneApplication.class, args);
    }

}

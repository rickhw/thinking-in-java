package com.gtcafe.asimov;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MyAutoConfiguration {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
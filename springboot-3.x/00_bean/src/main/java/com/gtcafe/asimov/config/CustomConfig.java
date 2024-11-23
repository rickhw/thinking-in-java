package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gtcafe.asimov.bean.CustomBean;

@Configuration
public class CustomConfig {

    @Bean
    public CustomBean customBean() {
        // 你可以在這裡添加自定義邏輯來初始化 Bean
        return new CustomBean("CustomBean Initialized by CustomConfig");
    }
}
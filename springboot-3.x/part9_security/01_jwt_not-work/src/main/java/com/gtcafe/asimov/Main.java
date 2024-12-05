package com.gtcafe.asimov;

import java.security.PublicKey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.gtcafe.asimov.utils.JwtUtils;

@SpringBootApplication
public class Main {
   
    private final JwtUtils jwtUtils;

    public Main(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public static void main(String[] args) throws Exception {
        // 在启动前尝试生成密钥
        // JwtUtils tempUtils = new JwtUtils();
        // tempUtils.generateKeyPair();

        SpringApplication.run(Main.class, args);
    }

    @Bean
    public PublicKey publicKey() {
        return jwtUtils.getPublicKey();
    }
}

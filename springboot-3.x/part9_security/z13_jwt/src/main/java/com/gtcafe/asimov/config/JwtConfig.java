package com.gtcafe.asimov.config;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair keyPair() throws Exception {
        // 讀取 private_key.pem 和 public_key.pem
        String privateKeyPem = new String(Files.readAllBytes(Paths.get("src/main/resources/private_key.pem")))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        String publicKeyPem = new String(Files.readAllBytes(Paths.get("src/main/resources/public_key.pem")))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // 解析 PEM 格式
        byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(privateKeyPem);
        byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(publicKeyPem);

        // 建立 KeyPair
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        return new KeyPair(publicKey, privateKey);
    }
}

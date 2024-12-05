package com.gtcafe.asimov.utils;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {

    @Value("classpath:jwt-private.pem")
    private Resource privateKeyResource;

    // private PrivateKey loadPrivateKey() throws Exception {
    //     byte[] keyBytes = Files.readAllBytes(privateKeyResource.getFile().toPath());
    //     PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    //     KeyFactory kf = KeyFactory.getInstance("RSA");
    //     return kf.generatePrivate(spec);
    // }

    // private PrivateKey loadPrivateKey() throws Exception {
    //     try (InputStream inputStream = privateKeyResource.getInputStream()) {
    //         byte[] keyBytes = inputStream.readAllBytes();
    //         PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    //         KeyFactory kf = KeyFactory.getInstance("RSA");
    //         return kf.generatePrivate(spec);
    //     }
    // }

    private PrivateKey loadPrivateKey() throws Exception {
        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            String pem = new String(inputStream.readAllBytes());
            pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                     .replace("-----END PRIVATE KEY-----", "")
                     .replaceAll("\\s", "");
            byte[] keyBytes = java.util.Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }

    
    public String generateToken(String email, String name) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("name", name);

            return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24小時有效
                .signWith(loadPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public PublicKey getPublicKey() {
        try {
            PrivateKey privateKey = loadPrivateKey();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(
                new X509EncodedKeySpec(privateKey.getEncoded())
            );
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException("Public key extraction failed", e);
        }
    }
}
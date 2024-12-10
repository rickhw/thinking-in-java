package com.gtcafe.asimov.controller;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class JwtController {

    @Autowired
    private KeyPair keyPair;

    // 1. 產生 JWT Token
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 小時有效
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        return ResponseEntity.ok(response);
    }

    // 2. 提供 Public Key
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        PublicKey publicKey = keyPair.getPublic();
        String publicKeyString = "-----BEGIN PUBLIC KEY-----\n" +
                java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        return ResponseEntity.ok(publicKeyString);
    }

    // 3. 需要透過 Token 操作的 API
    @GetMapping("/validate")
    public ResponseEntity<String> secureData(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");

            log.info("token: [{}]", token);

            Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .build()
                    .parseClaimsJws(token); // 驗證 JWT

                    log.info("validate token pass");
            return ResponseEntity.ok("validate token pass.");
        } catch (Exception e) {
            log.info("validate token failure");
            log.info("ex: {}", e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(401).body("Invalid or expired token.");
        }
    }
}

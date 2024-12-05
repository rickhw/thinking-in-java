package com.gtcafe.asimov.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
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
    @GetMapping("/secure-data")
    public ResponseEntity<String> secureData(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .build()
                    .parseClaimsJws(token); // 驗證 JWT
            return ResponseEntity.ok("Secure data accessed.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }
    }
}

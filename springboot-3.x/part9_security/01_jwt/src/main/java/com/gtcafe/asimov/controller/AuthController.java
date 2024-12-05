package com.gtcafe.asimov.controller;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.utils.JwtUtils;

@RestController
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // 公開 public key 的 API
    @GetMapping("/public/key")
    public ResponseEntity<String> getPublicKey() {
        PublicKey publicKey = jwtUtils.getPublicKey();
        return ResponseEntity.ok(
            Base64.getEncoder().encodeToString(publicKey.getEncoded())
        );
    }

    // 生成 token 的 API
    @PostMapping("/auth/token")
    public ResponseEntity<Map<String, String>> createToken(
        @RequestBody Map<String, String> payload
    ) {
        String email = payload.get("email");
        String name = payload.get("name");

        if (email == null || name == null) {
            return ResponseEntity.badRequest().build();
        }

        String token = jwtUtils.generateToken(email, name);
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    // 需要授權的 API
    @GetMapping("/protected/hello")
    public ResponseEntity<String> protectedHello() {
        return ResponseEntity.ok("Hello, Authenticated User!");
    }
}
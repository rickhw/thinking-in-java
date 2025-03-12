
package com.example.security.controller;

import com.example.security.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> loginData, HttpServletResponse response) {
        String sessionId = authService.login(loginData.get("username"), loginData.get("password"));

        Cookie cookie = new Cookie("sessionId", sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(1800);
        response.addCookie(cookie);

        return "Login successful";
    }

    @PostMapping("/logout")
    public String logout(@CookieValue(name = "sessionId", required = false) String sessionId, HttpServletResponse response) {
        if (sessionId != null) {
            authService.logout(sessionId);

            Cookie cookie = new Cookie("sessionId", "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "Logged out";
    }
}

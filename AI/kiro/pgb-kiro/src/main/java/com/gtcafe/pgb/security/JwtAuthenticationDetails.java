package com.gtcafe.pgb.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT 認證詳細資訊
 * 擴展 WebAuthenticationDetails 以包含使用者 ID 等額外資訊
 */
public class JwtAuthenticationDetails extends WebAuthenticationDetails {

    private final Long userId;

    public JwtAuthenticationDetails(HttpServletRequest request, Long userId) {
        super(request);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "JwtAuthenticationDetails{" +
                "userId=" + userId +
                ", remoteAddress='" + getRemoteAddress() + '\'' +
                ", sessionId='" + getSessionId() + '\'' +
                '}';
    }
}
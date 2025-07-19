package com.messageboard.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

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
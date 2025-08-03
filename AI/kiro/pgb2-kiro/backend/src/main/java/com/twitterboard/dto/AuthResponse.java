package com.twitterboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for authentication operations
 */
public class AuthResponse {
    
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    @JsonProperty("user")
    private UserInfo user;
    
    @JsonProperty("expiresIn")
    private long expiresIn;
    
    public AuthResponse() {}
    
    public AuthResponse(String accessToken, String refreshToken, UserInfo user, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = expiresIn;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    @Override
    public String toString() {
        return "AuthResponse{" +
                "accessToken='" + (accessToken != null ? "[PROTECTED]" : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[PROTECTED]" : null) + '\'' +
                ", user=" + user +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
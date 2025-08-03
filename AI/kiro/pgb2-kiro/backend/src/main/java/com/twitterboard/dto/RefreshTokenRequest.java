package com.twitterboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refresh token operations
 */
public class RefreshTokenRequest {
    
    @JsonProperty("refreshToken")
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
    
    public RefreshTokenRequest() {}
    
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='" + (refreshToken != null ? "[PROTECTED]" : null) + '\'' +
                '}';
    }
}
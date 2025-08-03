package com.twitterboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for authentication operations
 */
public class AuthRequest {
    
    @JsonProperty("authCode")
    @NotBlank(message = "Authorization code cannot be blank")
    private String authCode;
    
    public AuthRequest() {}
    
    public AuthRequest(String authCode) {
        this.authCode = authCode;
    }
    
    public String getAuthCode() {
        return authCode;
    }
    
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
    
    @Override
    public String toString() {
        return "AuthRequest{" +
                "authCode='" + (authCode != null ? "[PROTECTED]" : null) + '\'' +
                '}';
    }
}
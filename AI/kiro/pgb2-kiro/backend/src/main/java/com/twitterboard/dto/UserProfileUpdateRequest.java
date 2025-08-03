package com.twitterboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user profile updates
 */
public class UserProfileUpdateRequest {
    
    @JsonProperty("name")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    
    public UserProfileUpdateRequest() {}
    
    public UserProfileUpdateRequest(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    @Override
    public String toString() {
        return "UserProfileUpdateRequest{" +
                "name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
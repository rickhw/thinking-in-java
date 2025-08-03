package com.twitterboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twitterboard.entity.User;

import java.time.LocalDateTime;

/**
 * DTO for user information in API responses
 */
public class UserInfo {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("googleId")
    private String googleId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    public UserInfo() {}
    
    public UserInfo(Long id, String googleId, String email, String name, String avatarUrl, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Create UserInfo from User entity
     * @param user User entity
     * @return UserInfo DTO
     */
    public static UserInfo fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserInfo(
            user.getId(),
            user.getGoogleId(),
            user.getEmail(),
            user.getName(),
            user.getAvatarUrl(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGoogleId() {
        return googleId;
    }
    
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
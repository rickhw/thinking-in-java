package com.gtcafe.pgb.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity representing a user in the message board system
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sso_id", unique = true, nullable = false)
    @NotBlank(message = "SSO ID cannot be blank")
    @Size(max = 255, message = "SSO ID cannot exceed 255 characters")
    private String ssoId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(name = "email", nullable = false)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Column(name = "display_name", length = 100)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Constructor for creating a new user with minimal required fields
     */
    public User(String ssoId, String username, String email) {
        this.ssoId = ssoId;
        this.username = username;
        this.email = email;
        this.isActive = true;
    }

    /**
     * Check if the user is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Get display name or fallback to username
     */
    public String getEffectiveDisplayName() {
        return displayName != null && !displayName.trim().isEmpty() ? displayName : username;
    }
}
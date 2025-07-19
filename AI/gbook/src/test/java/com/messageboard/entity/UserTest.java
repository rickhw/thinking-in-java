package com.messageboard.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Tests")
class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create user with valid data")
    void shouldCreateUserWithValidData() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
        assertThat(user.getSsoId()).isEqualTo("sso123");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getDisplayName()).isEqualTo("Test User");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should create user with minimal constructor")
    void shouldCreateUserWithMinimalConstructor() {
        // Given & When
        User user = new User("sso123", "testuser", "test@example.com");

        // Then
        assertThat(user.getSsoId()).isEqualTo("sso123");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getDisplayName()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
    }

    @Test
    @DisplayName("Should validate SSO ID is not blank")
    void shouldValidateSsoIdNotBlank() {
        // Given
        User user = User.builder()
                .ssoId("")
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("SSO ID cannot be blank");
    }

    @Test
    @DisplayName("Should validate SSO ID length")
    void shouldValidateSsoIdLength() {
        // Given
        String longSsoId = "a".repeat(256);
        User user = User.builder()
                .ssoId(longSsoId)
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("SSO ID cannot exceed 255 characters");
    }

    @Test
    @DisplayName("Should validate username is not blank")
    void shouldValidateUsernameNotBlank() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("")
                .email("test@example.com")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        boolean hasUsernameBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username cannot be blank") || 
                              v.getMessage().contains("Username must be between 3 and 50 characters"));
        assertThat(hasUsernameBlankViolation).isTrue();
    }

    @Test
    @DisplayName("Should validate username length constraints")
    void shouldValidateUsernameLength() {
        // Given - username too short
        User userTooShort = User.builder()
                .ssoId("sso123")
                .username("ab")
                .email("test@example.com")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(userTooShort);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");

        // Given - username too long
        User userTooLong = User.builder()
                .ssoId("sso123")
                .username("a".repeat(51))
                .email("test@example.com")
                .build();

        // When
        violations = validator.validate(userTooLong);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("invalid-email")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email should be valid");
    }

    @Test
    @DisplayName("Should validate email is not blank")
    void shouldValidateEmailNotBlank() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("")
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email cannot be blank");
    }

    @Test
    @DisplayName("Should validate display name length")
    void shouldValidateDisplayNameLength() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName("a".repeat(101))
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Display name cannot exceed 100 characters");
    }

    @Test
    @DisplayName("Should validate avatar URL length")
    void shouldValidateAvatarUrlLength() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .avatarUrl("https://example.com/" + "a".repeat(500))
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Avatar URL cannot exceed 500 characters");
    }

    @Test
    @DisplayName("Should return true for active user")
    void shouldReturnTrueForActiveUser() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .isActive(true)
                .build();

        // When & Then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return false for inactive user")
    void shouldReturnFalseForInactiveUser() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .isActive(false)
                .build();

        // When & Then
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle null isActive as false")
    void shouldHandleNullIsActiveAsFalse() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .isActive(null)
                .build();

        // When & Then
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return display name when available")
    void shouldReturnDisplayNameWhenAvailable() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .build();

        // When & Then
        assertThat(user.getEffectiveDisplayName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should return username when display name is null")
    void shouldReturnUsernameWhenDisplayNameIsNull() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName(null)
                .build();

        // When & Then
        assertThat(user.getEffectiveDisplayName()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return username when display name is empty")
    void shouldReturnUsernameWhenDisplayNameIsEmpty() {
        // Given
        User user = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName("   ")
                .build();

        // When & Then
        assertThat(user.getEffectiveDisplayName()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .build();

        User user3 = User.builder()
                .id(2L)
                .ssoId("sso456")
                .username("testuser2")
                .email("test2@example.com")
                .build();

        // When & Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void shouldTestToStringMethod() {
        // Given
        User user = User.builder()
                .id(1L)
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .build();

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("testuser");
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("Test User");
    }
}
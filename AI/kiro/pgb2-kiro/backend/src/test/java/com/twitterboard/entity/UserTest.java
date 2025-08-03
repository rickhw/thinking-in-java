package com.twitterboard.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidUser() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals("google123", user.getGoogleId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertEquals("http://avatar.url", user.getAvatarUrl());
        assertNotNull(user.getPosts());
    }
    
    @Test
    void testUserWithBlankGoogleId() {
        // Given
        User user = new User("", "test@example.com", "Test User", "http://avatar.url");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Google ID cannot be blank")));
    }
    
    @Test
    void testUserWithInvalidEmail() {
        // Given
        User user = new User("google123", "invalid-email", "Test User", "http://avatar.url");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }
    
    @Test
    void testUserWithBlankName() {
        // Given
        User user = new User("google123", "test@example.com", "", "http://avatar.url");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Name cannot be blank")));
    }
    
    @Test
    void testUserWithTooLongName() {
        // Given
        String longName = "a".repeat(101);
        User user = new User("google123", "test@example.com", longName, "http://avatar.url");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Name must be between 1 and 100 characters")));
    }
    
    @Test
    void testUserWithNullAvatarUrl() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", null);
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertTrue(violations.isEmpty());
        assertNull(user.getAvatarUrl());
    }
    
    @Test
    void testAddPost() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        Post post = new Post(user, "Test post content");
        
        // When
        user.addPost(post);
        
        // Then
        assertTrue(user.getPosts().contains(post));
        assertEquals(user, post.getAuthor());
    }
    
    @Test
    void testRemovePost() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        Post post = new Post(user, "Test post content");
        user.addPost(post);
        
        // When
        user.removePost(post);
        
        // Then
        assertFalse(user.getPosts().contains(post));
        assertNull(post.getAuthor());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        User user1 = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        User user2 = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        User user3 = new User("google456", "test2@example.com", "Test User 2", "http://avatar2.url");
        
        user1.setId(1L);
        user2.setId(1L);
        user3.setId(2L);
        
        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        user.setId(1L);
        
        // When
        String toString = user.toString();
        
        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("googleId='google123'"));
        assertTrue(toString.contains("email='test@example.com'"));
        assertTrue(toString.contains("name='Test User'"));
    }
}
package com.twitterboard.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {
    
    private Validator validator;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testUser = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        testUser.setId(1L);
    }
    
    @Test
    void testValidPost() {
        // Given
        Post post = new Post(testUser, "This is a valid post content");
        
        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(testUser, post.getAuthor());
        assertEquals("This is a valid post content", post.getContent());
        assertFalse(post.getDeleted());
        assertFalse(post.isDeleted());
    }
    
    @Test
    void testPostWithBlankContent() {
        // Given
        Post post = new Post(testUser, "");
        
        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Content cannot be blank")));
    }
    
    @Test
    void testPostWithTooLongContent() {
        // Given
        String longContent = "a".repeat(281);
        Post post = new Post(testUser, longContent);
        
        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Content must be between 1 and 280 characters")));
    }
    
    @Test
    void testPostWithMaxLengthContent() {
        // Given
        String maxContent = "a".repeat(280);
        Post post = new Post(testUser, maxContent);
        
        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(maxContent, post.getContent());
    }
    
    @Test
    void testPostWithNullAuthor() {
        // Given
        Post post = new Post(null, "Test content");
        
        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Author cannot be null")));
    }
    
    @Test
    void testMarkAsDeleted() {
        // Given
        Post post = new Post(testUser, "Test content");
        
        // When
        post.markAsDeleted();
        
        // Then
        assertTrue(post.getDeleted());
        assertTrue(post.isDeleted());
    }
    
    @Test
    void testRestore() {
        // Given
        Post post = new Post(testUser, "Test content");
        post.markAsDeleted();
        
        // When
        post.restore();
        
        // Then
        assertFalse(post.getDeleted());
        assertFalse(post.isDeleted());
    }
    
    @Test
    void testIsOwnedByUser() {
        // Given
        Post post = new Post(testUser, "Test content");
        User anotherUser = new User("google456", "test2@example.com", "Another User", null);
        anotherUser.setId(2L);
        
        // Then
        assertTrue(post.isOwnedBy(testUser));
        assertFalse(post.isOwnedBy(anotherUser));
    }
    
    @Test
    void testIsOwnedByUserId() {
        // Given
        Post post = new Post(testUser, "Test content");
        
        // Then
        assertTrue(post.isOwnedBy(1L));
        assertFalse(post.isOwnedBy(2L));
    }
    
    @Test
    void testIsOwnedByWithNullAuthor() {
        // Given
        Post post = new Post();
        post.setContent("Test content");
        
        // Then
        assertFalse(post.isOwnedBy(testUser));
        assertFalse(post.isOwnedBy(1L));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        Post post1 = new Post(testUser, "Test content");
        Post post2 = new Post(testUser, "Test content");
        Post post3 = new Post(testUser, "Different content");
        
        post1.setId(1L);
        post2.setId(1L);
        post3.setId(2L);
        
        // Then
        assertEquals(post1, post2);
        assertNotEquals(post1, post3);
        assertEquals(post1.hashCode(), post2.hashCode());
        assertNotEquals(post1.hashCode(), post3.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        Post post = new Post(testUser, "Test content");
        post.setId(1L);
        
        // When
        String toString = post.toString();
        
        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("authorId=1"));
        assertTrue(toString.contains("content='Test content'"));
        assertTrue(toString.contains("deleted=false"));
    }
    
    @Test
    void testDefaultConstructor() {
        // Given & When
        Post post = new Post();
        
        // Then
        assertNull(post.getId());
        assertNull(post.getAuthor());
        assertNull(post.getContent());
        assertEquals(false, post.getDeleted());
        assertNull(post.getCreatedAt());
        assertNull(post.getUpdatedAt());
    }
}
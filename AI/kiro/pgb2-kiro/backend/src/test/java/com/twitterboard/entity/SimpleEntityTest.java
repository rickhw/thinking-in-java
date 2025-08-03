package com.twitterboard.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify entity classes are properly structured
 */
class SimpleEntityTest {
    
    @Test
    void testUserEntityCreation() {
        // Given
        String googleId = "google123";
        String email = "test@example.com";
        String name = "Test User";
        String avatarUrl = "http://avatar.url";
        
        // When
        User user = new User(googleId, email, name, avatarUrl);
        
        // Then
        assertNotNull(user);
        assertEquals(googleId, user.getGoogleId());
        assertEquals(email, user.getEmail());
        assertEquals(name, user.getName());
        assertEquals(avatarUrl, user.getAvatarUrl());
        assertNotNull(user.getPosts());
        assertTrue(user.getPosts().isEmpty());
    }
    
    @Test
    void testPostEntityCreation() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", null);
        String content = "This is a test post";
        
        // When
        Post post = new Post(user, content);
        
        // Then
        assertNotNull(post);
        assertEquals(user, post.getAuthor());
        assertEquals(content, post.getContent());
        assertFalse(post.getDeleted());
        assertFalse(post.isDeleted());
    }
    
    @Test
    void testUserPostRelationship() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", null);
        Post post = new Post(user, "Test content");
        
        // When
        user.addPost(post);
        
        // Then
        assertTrue(user.getPosts().contains(post));
        assertEquals(user, post.getAuthor());
        assertTrue(post.isOwnedBy(user));
    }
    
    @Test
    void testPostSoftDelete() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", null);
        Post post = new Post(user, "Test content");
        
        // When
        post.markAsDeleted();
        
        // Then
        assertTrue(post.isDeleted());
        assertTrue(post.getDeleted());
        
        // When restored
        post.restore();
        
        // Then
        assertFalse(post.isDeleted());
        assertFalse(post.getDeleted());
    }
}
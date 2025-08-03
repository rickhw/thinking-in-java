package com.twitterboard.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EntityMappingTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void testUserEntityMapping() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        
        // When
        User savedUser = entityManager.persistAndFlush(user);
        
        // Then
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
        assertEquals("google123", savedUser.getGoogleId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Test User", savedUser.getName());
        assertEquals("http://avatar.url", savedUser.getAvatarUrl());
    }
    
    @Test
    void testPostEntityMapping() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        User savedUser = entityManager.persistAndFlush(user);
        
        Post post = new Post(savedUser, "This is a test post content");
        
        // When
        Post savedPost = entityManager.persistAndFlush(post);
        
        // Then
        assertNotNull(savedPost.getId());
        assertNotNull(savedPost.getCreatedAt());
        assertNotNull(savedPost.getUpdatedAt());
        assertEquals(savedUser.getId(), savedPost.getAuthor().getId());
        assertEquals("This is a test post content", savedPost.getContent());
        assertFalse(savedPost.getDeleted());
    }
    
    @Test
    void testUserPostRelationship() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        User savedUser = entityManager.persistAndFlush(user);
        
        Post post1 = new Post(savedUser, "First post");
        Post post2 = new Post(savedUser, "Second post");
        
        savedUser.addPost(post1);
        savedUser.addPost(post2);
        
        // When
        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.clear();
        
        User foundUser = entityManager.find(User.class, savedUser.getId());
        
        // Then
        assertNotNull(foundUser);
        assertEquals(2, foundUser.getPosts().size());
        assertTrue(foundUser.getPosts().stream()
                .anyMatch(p -> p.getContent().equals("First post")));
        assertTrue(foundUser.getPosts().stream()
                .anyMatch(p -> p.getContent().equals("Second post")));
    }
    
    @Test
    void testSoftDeletePost() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        User savedUser = entityManager.persistAndFlush(user);
        
        Post post = new Post(savedUser, "This post will be deleted");
        Post savedPost = entityManager.persistAndFlush(post);
        Long postId = savedPost.getId();
        
        // When
        savedPost.markAsDeleted();
        entityManager.persistAndFlush(savedPost);
        entityManager.clear();
        
        // Then
        Post foundPost = entityManager.find(Post.class, postId);
        assertNotNull(foundPost); // Entity still exists in database
        assertTrue(foundPost.isDeleted()); // But marked as deleted
    }
    
    @Test
    void testUniqueGoogleId() {
        // Given
        User user1 = new User("google123", "test1@example.com", "Test User 1", null);
        User user2 = new User("google123", "test2@example.com", "Test User 2", null);
        
        // When & Then
        entityManager.persistAndFlush(user1);
        
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }
    
    @Test
    void testCascadeDeletePosts() {
        // Given
        User user = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        User savedUser = entityManager.persistAndFlush(user);
        
        Post post = new Post(savedUser, "This post will be cascade deleted");
        entityManager.persistAndFlush(post);
        Long postId = post.getId();
        
        // When
        entityManager.remove(savedUser);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        Post foundPost = entityManager.find(Post.class, postId);
        assertNull(foundPost); // Post should be deleted due to cascade
    }
}
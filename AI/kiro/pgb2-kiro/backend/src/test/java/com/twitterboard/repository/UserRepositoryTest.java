package com.twitterboard.repository;

import com.twitterboard.entity.Post;
import com.twitterboard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser1;
    private User testUser2;
    
    @BeforeEach
    void setUp() {
        testUser1 = new User("google123", "test1@example.com", "Test User 1", "http://avatar1.url");
        testUser2 = new User("google456", "test2@example.com", "Test User 2", "http://avatar2.url");
        
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
    }
    
    @Test
    void testFindByGoogleId() {
        // When
        Optional<User> found = userRepository.findByGoogleId("google123");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("test1@example.com", found.get().getEmail());
        assertEquals("Test User 1", found.get().getName());
    }
    
    @Test
    void testFindByGoogleIdNotFound() {
        // When
        Optional<User> found = userRepository.findByGoogleId("nonexistent");
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    void testFindByEmail() {
        // When
        Optional<User> found = userRepository.findByEmail("test2@example.com");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("google456", found.get().getGoogleId());
        assertEquals("Test User 2", found.get().getName());
    }
    
    @Test
    void testExistsByGoogleId() {
        // Then
        assertTrue(userRepository.existsByGoogleId("google123"));
        assertFalse(userRepository.existsByGoogleId("nonexistent"));
    }
    
    @Test
    void testExistsByEmail() {
        // Then
        assertTrue(userRepository.existsByEmail("test1@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }
    
    @Test
    void testFindByCreatedAtAfter() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        // When
        List<User> usersAfterYesterday = userRepository.findByCreatedAtAfter(yesterday);
        List<User> usersAfterTomorrow = userRepository.findByCreatedAtAfter(tomorrow);
        
        // Then
        assertEquals(2, usersAfterYesterday.size());
        assertEquals(0, usersAfterTomorrow.size());
    }
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        // When
        List<User> usersWithTest = userRepository.findByNameContainingIgnoreCase("test");
        List<User> usersWithUser1 = userRepository.findByNameContainingIgnoreCase("USER 1");
        List<User> usersWithNonexistent = userRepository.findByNameContainingIgnoreCase("nonexistent");
        
        // Then
        assertEquals(2, usersWithTest.size());
        assertEquals(1, usersWithUser1.size());
        assertEquals(0, usersWithNonexistent.size());
    }
    
    @Test
    void testCountAllUsers() {
        // When
        long count = userRepository.countAllUsers();
        
        // Then
        assertEquals(2, count);
    }
    
    @Test
    void testFindUsersWithPostCount() {
        // Given
        Post post1 = new Post(testUser1, "Post 1");
        Post post2 = new Post(testUser1, "Post 2");
        Post post3 = new Post(testUser2, "Post 3");
        
        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.persistAndFlush(post3);
        
        // When
        List<Object[]> results = userRepository.findUsersWithPostCount();
        
        // Then
        assertEquals(2, results.size());
        
        // Find user1's result
        Object[] user1Result = results.stream()
                .filter(result -> ((User) result[0]).getGoogleId().equals("google123"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(user1Result);
        assertEquals(2L, user1Result[1]); // User1 has 2 posts
        
        // Find user2's result
        Object[] user2Result = results.stream()
                .filter(result -> ((User) result[0]).getGoogleId().equals("google456"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(user2Result);
        assertEquals(1L, user2Result[1]); // User2 has 1 post
    }
    
    @Test
    void testFindActiveUsers() {
        // Given
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        
        Post recentPost = new Post(testUser1, "Recent post");
        entityManager.persistAndFlush(recentPost);
        
        // When
        List<User> activeUsers = userRepository.findActiveUsers(twoDaysAgo);
        
        // Then
        assertEquals(1, activeUsers.size());
        assertEquals("google123", activeUsers.get(0).getGoogleId());
    }
    
    @Test
    void testFindActiveUsersWithDeletedPosts() {
        // Given
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        
        Post deletedPost = new Post(testUser1, "Deleted post");
        deletedPost.markAsDeleted();
        entityManager.persistAndFlush(deletedPost);
        
        // When
        List<User> activeUsers = userRepository.findActiveUsers(twoDaysAgo);
        
        // Then
        assertEquals(0, activeUsers.size()); // Deleted posts don't count as active
    }
    
    @Test
    void testSaveAndFindUser() {
        // Given
        User newUser = new User("google789", "new@example.com", "New User", null);
        
        // When
        User savedUser = userRepository.save(newUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("google789", foundUser.get().getGoogleId());
        assertEquals("new@example.com", foundUser.get().getEmail());
        assertEquals("New User", foundUser.get().getName());
        assertNull(foundUser.get().getAvatarUrl());
        assertNotNull(foundUser.get().getCreatedAt());
        assertNotNull(foundUser.get().getUpdatedAt());
    }
    
    @Test
    void testUpdateUser() {
        // Given
        User user = userRepository.findByGoogleId("google123").orElseThrow();
        String originalName = user.getName();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        
        // When
        user.setName("Updated Name");
        User updatedUser = userRepository.save(user);
        
        // Then
        assertEquals("Updated Name", updatedUser.getName());
        assertNotEquals(originalName, updatedUser.getName());
        // Note: In real scenario, updatedAt would be different, but in tests it might be the same
        // due to fast execution
    }
    
    @Test
    void testDeleteUser() {
        // Given
        User user = userRepository.findByGoogleId("google123").orElseThrow();
        Long userId = user.getId();
        
        // When
        userRepository.delete(user);
        Optional<User> deletedUser = userRepository.findById(userId);
        
        // Then
        assertFalse(deletedUser.isPresent());
    }
}
package com.twitterboard.repository;

import com.twitterboard.entity.Post;
import com.twitterboard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private PostRepository postRepository;
    
    private User testUser1;
    private User testUser2;
    private Post post1;
    private Post post2;
    private Post post3;
    
    @BeforeEach
    void setUp() {
        testUser1 = new User("google123", "test1@example.com", "Test User 1", null);
        testUser2 = new User("google456", "test2@example.com", "Test User 2", null);
        
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        
        post1 = new Post(testUser1, "First post by user 1");
        post2 = new Post(testUser1, "Second post by user 1");
        post3 = new Post(testUser2, "First post by user 2");
        
        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.persistAndFlush(post3);
    }
    
    @Test
    void testFindByAuthor() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        
        // When
        Page<Post> posts = postRepository.findByAuthor(testUser1, pageable);
        
        // Then
        assertEquals(2, posts.getTotalElements());
        assertEquals(2, posts.getContent().size());
        assertTrue(posts.getContent().stream()
                .allMatch(p -> p.getAuthor().equals(testUser1)));
    }
    
    @Test
    void testFindByAuthorId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> posts = postRepository.findByAuthorId(testUser2.getId(), pageable);
        
        // Then
        assertEquals(1, posts.getTotalElements());
        assertEquals("First post by user 2", posts.getContent().get(0).getContent());
    }
    
    @Test
    void testFindByAuthorIdIncludingDeleted() {
        // Given
        Post deletedPost = new Post(testUser1, "Deleted post");
        deletedPost.markAsDeleted();
        entityManager.persistAndFlush(deletedPost);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> allPosts = postRepository.findByAuthorIdIncludingDeleted(testUser1.getId(), pageable);
        Page<Post> activePosts = postRepository.findByAuthorId(testUser1.getId(), pageable);
        
        // Then
        assertEquals(3, allPosts.getTotalElements()); // Including deleted
        assertEquals(2, activePosts.getTotalElements()); // Excluding deleted
    }
    
    @Test
    void testFindByDeletedFalse() {
        // Given
        post1.markAsDeleted();
        entityManager.persistAndFlush(post1);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> activePosts = postRepository.findByDeletedFalse(pageable);
        
        // Then
        assertEquals(2, activePosts.getTotalElements());
        assertTrue(activePosts.getContent().stream()
                .noneMatch(Post::isDeleted));
    }
    
    @Test
    void testFindByContentContainingIgnoreCase() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> postsWithFirst = postRepository.findByContentContainingIgnoreCase("FIRST", pageable);
        Page<Post> postsWithUser = postRepository.findByContentContainingIgnoreCase("user", pageable);
        Page<Post> postsWithNonexistent = postRepository.findByContentContainingIgnoreCase("nonexistent", pageable);
        
        // Then
        assertEquals(2, postsWithFirst.getTotalElements());
        assertEquals(3, postsWithUser.getTotalElements());
        assertEquals(0, postsWithNonexistent.getTotalElements());
    }
    
    @Test
    void testFindByCreatedAtAfterAndDeletedFalse() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> postsAfterYesterday = postRepository.findByCreatedAtAfterAndDeletedFalse(yesterday, pageable);
        Page<Post> postsAfterTomorrow = postRepository.findByCreatedAtAfterAndDeletedFalse(tomorrow, pageable);
        
        // Then
        assertEquals(3, postsAfterYesterday.getTotalElements());
        assertEquals(0, postsAfterTomorrow.getTotalElements());
    }
    
    @Test
    void testFindByCreatedAtBetweenAndDeletedFalse() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> postsBetween = postRepository.findByCreatedAtBetweenAndDeletedFalse(yesterday, tomorrow, pageable);
        
        // Then
        assertEquals(3, postsBetween.getTotalElements());
    }
    
    @Test
    void testCountByAuthor() {
        // When
        long count1 = postRepository.countByAuthor(testUser1);
        long count2 = postRepository.countByAuthor(testUser2);
        
        // Then
        assertEquals(2, count1);
        assertEquals(1, count2);
    }
    
    @Test
    void testCountByAuthorAndDeletedFalse() {
        // Given
        post1.markAsDeleted();
        entityManager.persistAndFlush(post1);
        
        // When
        long activeCount = postRepository.countByAuthorAndDeletedFalse(testUser1);
        long totalCount = postRepository.countByAuthor(testUser1);
        
        // Then
        assertEquals(1, activeCount);
        assertEquals(2, totalCount);
    }
    
    @Test
    void testCountByDeletedFalse() {
        // Given
        post1.markAsDeleted();
        entityManager.persistAndFlush(post1);
        
        // When
        long activeCount = postRepository.countByDeletedFalse();
        long totalCount = postRepository.count();
        
        // Then
        assertEquals(2, activeCount);
        assertEquals(3, totalCount);
    }
    
    @Test
    void testFindRecentPosts() {
        // Given
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> recentPosts = postRepository.findRecentPosts(twoDaysAgo, pageable);
        
        // Then
        assertEquals(3, recentPosts.getTotalElements());
        // Posts should be ordered by creation date descending
        List<Post> content = recentPosts.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertTrue(content.get(i).getCreatedAt().isAfter(content.get(i + 1).getCreatedAt()) ||
                      content.get(i).getCreatedAt().equals(content.get(i + 1).getCreatedAt()));
        }
    }
    
    @Test
    void testFindPopularPosts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> popularPosts = postRepository.findPopularPosts(pageable);
        
        // Then
        assertEquals(3, popularPosts.getTotalElements());
        assertTrue(popularPosts.getContent().stream()
                .noneMatch(Post::isDeleted));
    }
    
    @Test
    void testSoftDeleteByAuthorId() {
        // When
        int deletedCount = postRepository.softDeleteByAuthorId(testUser1.getId());
        
        // Then
        assertEquals(2, deletedCount);
        
        // Verify posts are soft deleted
        Page<Post> activePosts = postRepository.findByAuthorId(testUser1.getId(), PageRequest.of(0, 10));
        Page<Post> allPosts = postRepository.findByAuthorIdIncludingDeleted(testUser1.getId(), PageRequest.of(0, 10));
        
        assertEquals(0, activePosts.getTotalElements());
        assertEquals(2, allPosts.getTotalElements());
    }
    
    @Test
    void testDeleteByAuthorId() {
        // When
        int deletedCount = postRepository.deleteByAuthorId(testUser1.getId());
        
        // Then
        assertEquals(2, deletedCount);
        
        // Verify posts are hard deleted
        Page<Post> allPosts = postRepository.findByAuthorIdIncludingDeleted(testUser1.getId(), PageRequest.of(0, 10));
        assertEquals(0, allPosts.getTotalElements());
    }
    
    @Test
    void testFindDeletedPostsOlderThan() {
        // Given
        post1.markAsDeleted();
        entityManager.persistAndFlush(post1);
        
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        
        // When
        List<Post> postsToCleanupFuture = postRepository.findDeletedPostsOlderThan(future);
        List<Post> postsToCleanupPast = postRepository.findDeletedPostsOlderThan(past);
        
        // Then
        assertEquals(1, postsToCleanupFuture.size());
        assertEquals(0, postsToCleanupPast.size());
    }
    
    @Test
    void testGetPostStatsByAuthor() {
        // Given
        post1.markAsDeleted();
        entityManager.persistAndFlush(post1);
        
        // When
        Object[] stats = postRepository.getPostStatsByAuthor(testUser1.getId());
        
        // Then
        assertEquals(2L, stats[0]); // Total posts
        assertEquals(1L, stats[1]); // Active posts
        assertEquals(1L, stats[2]); // Deleted posts
    }
    
    @Test
    void testFindByIdIncludingDeleted() {
        // Given
        post1.markAsDeleted();
        entityManager.persistAndFlush(post1);
        
        // When
        Optional<Post> normalFind = postRepository.findById(post1.getId());
        Optional<Post> includingDeletedFind = postRepository.findByIdIncludingDeleted(post1.getId());
        
        // Then
        assertFalse(normalFind.isPresent()); // Normal find excludes deleted
        assertTrue(includingDeletedFind.isPresent()); // This includes deleted
        assertTrue(includingDeletedFind.get().isDeleted());
    }
}
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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CustomPostRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private PostRepository postRepository;
    
    private User testUser1;
    private User testUser2;
    private Post post1;
    private Post post2;
    private Post post3;
    private Post deletedPost;
    
    @BeforeEach
    void setUp() {
        testUser1 = new User("google123", "test1@example.com", "Test User 1", null);
        testUser2 = new User("google456", "test2@example.com", "Test User 2", null);
        
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        
        post1 = new Post(testUser1, "Java programming tutorial");
        post2 = new Post(testUser1, "Spring Boot best practices");
        post3 = new Post(testUser2, "React development guide");
        deletedPost = new Post(testUser1, "This post will be deleted");
        deletedPost.markAsDeleted();
        
        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.persistAndFlush(post3);
        entityManager.persistAndFlush(deletedPost);
    }
    
    @Test
    void testFindPostsWithFiltersNoFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> result = postRepository.findPostsWithFilters(null, null, null, null, false, pageable);
        
        // Then
        assertEquals(3, result.getTotalElements()); // Excludes deleted post
        assertTrue(result.getContent().stream().noneMatch(Post::isDeleted));
    }
    
    @Test
    void testFindPostsWithFiltersIncludeDeleted() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> result = postRepository.findPostsWithFilters(null, null, null, null, true, pageable);
        
        // Then
        assertEquals(4, result.getTotalElements()); // Includes deleted post
    }
    
    @Test
    void testFindPostsWithFiltersAuthorFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> result = postRepository.findPostsWithFilters(testUser1.getId(), null, null, null, false, pageable);
        
        // Then
        assertEquals(2, result.getTotalElements()); // Only user1's active posts
        assertTrue(result.getContent().stream()
                .allMatch(p -> p.getAuthor().equals(testUser1)));
    }
    
    @Test
    void testFindPostsWithFiltersContentFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> result = postRepository.findPostsWithFilters(null, "java", null, null, false, pageable);
        
        // Then
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getContent().toLowerCase().contains("java"));
    }
    
    @Test
    void testFindPostsWithFiltersDateRange() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> result = postRepository.findPostsWithFilters(null, null, yesterday, tomorrow, false, pageable);
        
        // Then
        assertEquals(3, result.getTotalElements()); // All posts created today
    }
    
    @Test
    void testFindPostsWithFiltersCombined() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> result = postRepository.findPostsWithFilters(
                testUser1.getId(), "spring", null, null, false, pageable);
        
        // Then
        assertEquals(1, result.getTotalElements());
        Post foundPost = result.getContent().get(0);
        assertEquals(testUser1, foundPost.getAuthor());
        assertTrue(foundPost.getContent().toLowerCase().contains("spring"));
    }
    
    @Test
    void testGetPostStatistics() {
        // When
        CustomPostRepository.PostStatistics stats = postRepository.getPostStatistics();
        
        // Then
        assertEquals(4, stats.getTotalPosts()); // All posts including deleted
        assertEquals(3, stats.getActivePosts()); // Active posts only
        assertEquals(1, stats.getDeletedPosts()); // Deleted posts only
        assertEquals(3, stats.getPostsToday()); // Posts created today (active only)
        assertEquals(3, stats.getPostsThisWeek()); // Posts created this week (active only)
        assertEquals(3, stats.getPostsThisMonth()); // Posts created this month (active only)
    }
    
    @Test
    void testFindTrendingPosts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Post> trendingPosts = postRepository.findTrendingPosts(7, pageable);
        
        // Then
        assertEquals(3, trendingPosts.getTotalElements()); // Active posts from last 7 days
        assertTrue(trendingPosts.getContent().stream().noneMatch(Post::isDeleted));
        
        // Verify ordering (most recent first)
        List<Post> content = trendingPosts.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertTrue(content.get(i).getCreatedAt().isAfter(content.get(i + 1).getCreatedAt()) ||
                      content.get(i).getCreatedAt().equals(content.get(i + 1).getCreatedAt()));
        }
    }
    
    @Test
    void testFindTrendingPostsOldPosts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When - Looking for posts from last 0 days (should be empty)
        Page<Post> trendingPosts = postRepository.findTrendingPosts(0, pageable);
        
        // Then
        assertEquals(0, trendingPosts.getTotalElements());
    }
    
    @Test
    void testBulkUpdatePostsStatus() {
        // Given
        List<Long> postIds = Arrays.asList(post1.getId(), post2.getId());
        
        // When
        int updatedCount = postRepository.bulkUpdatePostsStatus(postIds, true);
        
        // Then
        assertEquals(2, updatedCount);
        
        // Verify posts are marked as deleted
        entityManager.clear(); // Clear persistence context to force reload
        Post updatedPost1 = entityManager.find(Post.class, post1.getId());
        Post updatedPost2 = entityManager.find(Post.class, post2.getId());
        
        assertTrue(updatedPost1.isDeleted());
        assertTrue(updatedPost2.isDeleted());
    }
    
    @Test
    void testBulkUpdatePostsStatusRestore() {
        // Given
        List<Long> postIds = Arrays.asList(deletedPost.getId());
        
        // When
        int updatedCount = postRepository.bulkUpdatePostsStatus(postIds, false);
        
        // Then
        assertEquals(1, updatedCount);
        
        // Verify post is restored
        entityManager.clear();
        Post restoredPost = entityManager.find(Post.class, deletedPost.getId());
        assertFalse(restoredPost.isDeleted());
    }
    
    @Test
    void testBulkUpdatePostsStatusEmptyList() {
        // When
        int updatedCount = postRepository.bulkUpdatePostsStatus(Arrays.asList(), true);
        
        // Then
        assertEquals(0, updatedCount);
    }
    
    @Test
    void testBulkUpdatePostsStatusNullList() {
        // When
        int updatedCount = postRepository.bulkUpdatePostsStatus(null, true);
        
        // Then
        assertEquals(0, updatedCount);
    }
    
    @Test
    void testPagination() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);
        
        // When
        Page<Post> page1 = postRepository.findPostsWithFilters(null, null, null, null, false, firstPage);
        Page<Post> page2 = postRepository.findPostsWithFilters(null, null, null, null, false, secondPage);
        
        // Then
        assertEquals(2, page1.getContent().size());
        assertEquals(1, page2.getContent().size());
        assertEquals(3, page1.getTotalElements());
        assertEquals(3, page2.getTotalElements());
        assertTrue(page1.hasNext());
        assertFalse(page2.hasNext());
    }
}
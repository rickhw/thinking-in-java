package com.twitterboard.repository;

import com.twitterboard.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom repository interface for complex Post queries
 */
public interface CustomPostRepository {
    
    /**
     * Find posts with advanced filtering
     * @param authorId Author ID (optional)
     * @param content Content filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @param includeDeleted Whether to include deleted posts
     * @param pageable Pagination information
     * @return Page of filtered posts
     */
    Page<Post> findPostsWithFilters(Long authorId, String content, 
                                   LocalDateTime startDate, LocalDateTime endDate,
                                   boolean includeDeleted, Pageable pageable);
    
    /**
     * Get post statistics for dashboard
     * @return Post statistics
     */
    PostStatistics getPostStatistics();
    
    /**
     * Find trending posts (posts with high activity in recent period)
     * @param days Number of days to look back
     * @param pageable Pagination information
     * @return Page of trending posts
     */
    Page<Post> findTrendingPosts(int days, Pageable pageable);
    
    /**
     * Bulk update posts status
     * @param postIds List of post IDs
     * @param deleted New deleted status
     * @return Number of updated posts
     */
    int bulkUpdatePostsStatus(List<Long> postIds, boolean deleted);
    
    /**
     * Post statistics data class
     */
    class PostStatistics {
        private final long totalPosts;
        private final long activePosts;
        private final long deletedPosts;
        private final long postsToday;
        private final long postsThisWeek;
        private final long postsThisMonth;
        
        public PostStatistics(long totalPosts, long activePosts, long deletedPosts,
                            long postsToday, long postsThisWeek, long postsThisMonth) {
            this.totalPosts = totalPosts;
            this.activePosts = activePosts;
            this.deletedPosts = deletedPosts;
            this.postsToday = postsToday;
            this.postsThisWeek = postsThisWeek;
            this.postsThisMonth = postsThisMonth;
        }
        
        // Getters
        public long getTotalPosts() { return totalPosts; }
        public long getActivePosts() { return activePosts; }
        public long getDeletedPosts() { return deletedPosts; }
        public long getPostsToday() { return postsToday; }
        public long getPostsThisWeek() { return postsThisWeek; }
        public long getPostsThisMonth() { return postsThisMonth; }
    }
}
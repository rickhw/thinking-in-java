package com.twitterboard.repository;

import com.twitterboard.entity.Post;
import com.twitterboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {
    
    /**
     * Find all posts by author (paginated)
     * @param author Post author
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<Post> findByAuthor(User author, Pageable pageable);
    
    /**
     * Find all posts by author ID (paginated)
     * @param authorId Author ID
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * Find all posts by author including deleted ones (paginated)
     * @param authorId Author ID
     * @param pageable Pagination information
     * @return Page of posts including deleted
     */
    @Query("SELECT p FROM Post p WHERE p.author.id = :authorId")
    Page<Post> findByAuthorIdIncludingDeleted(@Param("authorId") Long authorId, Pageable pageable);
    
    /**
     * Find all non-deleted posts (paginated)
     * @param pageable Pagination information
     * @return Page of non-deleted posts
     */
    Page<Post> findByDeletedFalse(Pageable pageable);
    
    /**
     * Find posts by content containing (case insensitive)
     * @param content Content pattern
     * @param pageable Pagination information
     * @return Page of posts
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%')) AND p.deleted = false")
    Page<Post> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);
    
    /**
     * Find posts created after specific date
     * @param date Creation date threshold
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<Post> findByCreatedAtAfterAndDeletedFalse(LocalDateTime date, Pageable pageable);
    
    /**
     * Find posts created between dates
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<Post> findByCreatedAtBetweenAndDeletedFalse(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Count posts by author
     * @param author Post author
     * @return Number of posts
     */
    long countByAuthor(User author);
    
    /**
     * Count non-deleted posts by author
     * @param author Post author
     * @return Number of non-deleted posts
     */
    long countByAuthorAndDeletedFalse(User author);
    
    /**
     * Count all non-deleted posts
     * @return Total number of non-deleted posts
     */
    long countByDeletedFalse();
    
    /**
     * Find recent posts (last N days)
     * @param since Date threshold
     * @param pageable Pagination information
     * @return Page of recent posts
     */
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findRecentPosts(@Param("since") LocalDateTime since, Pageable pageable);
    
    /**
     * Find popular posts (posts with recent activity - for future use)
     * Currently just returns recent posts ordered by creation date
     * @param pageable Pagination information
     * @return Page of popular posts
     */
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findPopularPosts(Pageable pageable);
    
    /**
     * Soft delete posts by author (for user deletion)
     * @param authorId Author ID
     * @return Number of affected posts
     */
    @Modifying
    @Query("UPDATE Post p SET p.deleted = true WHERE p.author.id = :authorId AND p.deleted = false")
    int softDeleteByAuthorId(@Param("authorId") Long authorId);
    
    /**
     * Hard delete posts by author (for cleanup)
     * @param authorId Author ID
     * @return Number of deleted posts
     */
    @Modifying
    @Query("DELETE FROM Post p WHERE p.author.id = :authorId")
    int deleteByAuthorId(@Param("authorId") Long authorId);
    
    /**
     * Find posts that need cleanup (deleted posts older than specified days)
     * @param cutoffDate Cutoff date for cleanup
     * @return List of posts to be cleaned up
     */
    @Query("SELECT p FROM Post p WHERE p.deleted = true AND p.updatedAt < :cutoffDate")
    List<Post> findDeletedPostsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get post statistics by author
     * @param authorId Author ID
     * @return Array containing [totalPosts, activePosts, deletedPosts]
     */
    @Query("SELECT COUNT(p), " +
           "SUM(CASE WHEN p.deleted = false THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN p.deleted = true THEN 1 ELSE 0 END) " +
           "FROM Post p WHERE p.author.id = :authorId")
    Object[] getPostStatsByAuthor(@Param("authorId") Long authorId);
    
    /**
     * Find post by ID including deleted posts (for admin purposes)
     * @param id Post ID
     * @return Optional Post
     */
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdIncludingDeleted(@Param("id") Long id);
}
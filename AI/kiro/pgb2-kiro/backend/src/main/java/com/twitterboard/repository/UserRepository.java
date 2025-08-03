package com.twitterboard.repository;

import com.twitterboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Google ID
     * @param googleId Google OAuth ID
     * @return Optional User
     */
    Optional<User> findByGoogleId(String googleId);
    
    /**
     * Find user by email
     * @param email User email
     * @return Optional User
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by Google ID
     * @param googleId Google OAuth ID
     * @return true if exists
     */
    boolean existsByGoogleId(String googleId);
    
    /**
     * Check if user exists by email
     * @param email User email
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users created after specific date
     * @param date Creation date threshold
     * @return List of users
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find users by name containing (case insensitive)
     * @param name Name pattern
     * @return List of users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Count total number of users
     * @return Total user count
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
    
    /**
     * Find users with posts count
     * @return List of users with their post counts
     */
    @Query("SELECT u, COUNT(p) FROM User u LEFT JOIN u.posts p GROUP BY u")
    List<Object[]> findUsersWithPostCount();
    
    /**
     * Find active users (users who have created posts in the last N days)
     * @param days Number of days to look back
     * @return List of active users
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.posts p WHERE p.createdAt >= :since AND p.deleted = false")
    List<User> findActiveUsers(@Param("since") LocalDateTime since);
}
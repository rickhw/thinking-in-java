package com.messageboard.repository;

import com.messageboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by SSO ID
     */
    Optional<User> findBySsoId(String ssoId);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by SSO ID
     */
    boolean existsBySsoId(String ssoId);

    /**
     * Check if user exists by username
     */
    boolean existsByUsername(String username);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find active users only
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * Search users by username or display name (case insensitive)
     * Only returns active users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchActiveUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find users by username containing keyword (case insensitive)
     * Only returns active users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<User> findActiveUsersByUsernameContaining(@Param("username") String username, Pageable pageable);
}
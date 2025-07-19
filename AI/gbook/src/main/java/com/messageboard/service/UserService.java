package com.messageboard.service;

import com.messageboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for User operations
 */
public interface UserService {

    /**
     * Create a new user
     * @param user the user to create
     * @return the created user
     * @throws IllegalArgumentException if user data is invalid
     */
    User createUser(User user);

    /**
     * Update an existing user
     * @param id the user ID
     * @param user the updated user data
     * @return the updated user
     * @throws IllegalArgumentException if user data is invalid
     * @throws RuntimeException if user not found
     */
    User updateUser(Long id, User user);

    /**
     * Find user by ID
     * @param id the user ID
     * @return the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Find user by SSO ID
     * @param ssoId the SSO ID
     * @return the user if found
     */
    Optional<User> findBySsoId(String ssoId);

    /**
     * Find user by username
     * @param username the username
     * @return the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email the email
     * @return the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Get all active users with pagination
     * @param pageable pagination information
     * @return page of active users
     */
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * Search users by keyword (username or display name)
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return page of matching active users
     */
    Page<User> searchUsers(String keyword, Pageable pageable);

    /**
     * Search users by username
     * @param username the username to search
     * @param pageable pagination information
     * @return page of matching active users
     */
    Page<User> searchUsersByUsername(String username, Pageable pageable);

    /**
     * Deactivate a user (soft delete)
     * @param id the user ID
     * @throws RuntimeException if user not found
     */
    void deactivateUser(Long id);

    /**
     * Activate a user
     * @param id the user ID
     * @throws RuntimeException if user not found
     */
    void activateUser(Long id);

    /**
     * Check if username is available
     * @param username the username to check
     * @return true if available, false otherwise
     */
    boolean isUsernameAvailable(String username);

    /**
     * Check if email is available
     * @param email the email to check
     * @return true if available, false otherwise
     */
    boolean isEmailAvailable(String email);

    /**
     * Check if SSO ID is available
     * @param ssoId the SSO ID to check
     * @return true if available, false otherwise
     */
    boolean isSsoIdAvailable(String ssoId);
}
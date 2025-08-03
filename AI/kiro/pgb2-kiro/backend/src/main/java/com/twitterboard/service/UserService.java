package com.twitterboard.service;

import com.twitterboard.dto.GoogleUserInfo;
import com.twitterboard.dto.UserInfo;
import com.twitterboard.dto.UserProfileUpdateRequest;
import com.twitterboard.entity.User;
import com.twitterboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing user data and operations
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Find existing user or create new user from Google OAuth info
     * @param googleUserInfo Google user information
     * @return User entity
     */
    public User findOrCreateUser(GoogleUserInfo googleUserInfo) {
        logger.info("Finding or creating user for Google ID: {}", googleUserInfo.getId());
        
        // Try to find existing user by Google ID
        Optional<User> existingUser = userRepository.findByGoogleId(googleUserInfo.getId());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            logger.info("Found existing user: {}", user.getId());
            
            // Update user info if changed
            boolean updated = false;
            
            if (!user.getEmail().equals(googleUserInfo.getEmail())) {
                user.setEmail(googleUserInfo.getEmail());
                updated = true;
            }
            
            if (!user.getName().equals(googleUserInfo.getName())) {
                user.setName(googleUserInfo.getName());
                updated = true;
            }
            
            if (googleUserInfo.getPicture() != null && 
                !googleUserInfo.getPicture().equals(user.getAvatarUrl())) {
                user.setAvatarUrl(googleUserInfo.getPicture());
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
                logger.info("Updated existing user: {}", user.getId());
            }
            
            return user;
        } else {
            // Create new user
            User newUser = new User(
                googleUserInfo.getId(),
                googleUserInfo.getEmail(),
                googleUserInfo.getName(),
                googleUserInfo.getPicture()
            );
            
            newUser = userRepository.save(newUser);
            logger.info("Created new user: {}", newUser.getId());
            
            return newUser;
        }
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User entity or null if not found
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        logger.debug("Getting user by ID: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            logger.debug("Found user: {}", userId);
            return user.get();
        } else {
            logger.warn("User not found: {}", userId);
            return null;
        }
    }
    
    /**
     * Get user by Google ID
     * @param googleId Google OAuth ID
     * @return User entity or null if not found
     */
    @Transactional(readOnly = true)
    public User getUserByGoogleId(String googleId) {
        logger.debug("Getting user by Google ID: {}", googleId);
        
        Optional<User> user = userRepository.findByGoogleId(googleId);
        if (user.isPresent()) {
            logger.debug("Found user by Google ID: {}", googleId);
            return user.get();
        } else {
            logger.warn("User not found by Google ID: {}", googleId);
            return null;
        }
    }
    
    /**
     * Get user by email
     * @param email User email
     * @return User entity or null if not found
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);
        
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.debug("Found user by email: {}", email);
            return user.get();
        } else {
            logger.warn("User not found by email: {}", email);
            return null;
        }
    }
    
    /**
     * Update user profile
     * @param userId User ID
     * @param updateRequest Profile update request
     * @return Updated user info
     * @throws Exception if user not found or update fails
     */
    public UserInfo updateUserProfile(Long userId, UserProfileUpdateRequest updateRequest) throws Exception {
        logger.info("Updating profile for user: {}", userId);
        
        User user = getUserById(userId);
        if (user == null) {
            throw new Exception("User not found: " + userId);
        }
        
        boolean updated = false;
        
        // Update name if provided
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
            String newName = updateRequest.getName().trim();
            if (!newName.equals(user.getName())) {
                user.setName(newName);
                updated = true;
            }
        }
        
        // Update avatar URL if provided
        if (updateRequest.getAvatarUrl() != null) {
            String newAvatarUrl = updateRequest.getAvatarUrl().trim();
            if (newAvatarUrl.isEmpty()) {
                newAvatarUrl = null;
            }
            
            if ((newAvatarUrl == null && user.getAvatarUrl() != null) ||
                (newAvatarUrl != null && !newAvatarUrl.equals(user.getAvatarUrl()))) {
                user.setAvatarUrl(newAvatarUrl);
                updated = true;
            }
        }
        
        if (updated) {
            user = userRepository.save(user);
            logger.info("Profile updated for user: {}", userId);
        } else {
            logger.info("No changes needed for user profile: {}", userId);
        }
        
        return UserInfo.fromUser(user);
    }
    
    /**
     * Check if user exists by Google ID
     * @param googleId Google OAuth ID
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean existsByGoogleId(String googleId) {
        return userRepository.existsByGoogleId(googleId);
    }
    
    /**
     * Check if user exists by email
     * @param email User email
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Get all users (admin function)
     * @return List of user info
     */
    @Transactional(readOnly = true)
    public List<UserInfo> getAllUsers() {
        logger.info("Getting all users");
        
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserInfo::fromUser)
                .collect(Collectors.toList());
    }
    
    /**
     * Search users by name
     * @param name Name pattern to search
     * @return List of matching users
     */
    @Transactional(readOnly = true)
    public List<UserInfo> searchUsersByName(String name) {
        logger.info("Searching users by name: {}", name);
        
        List<User> users = userRepository.findByNameContainingIgnoreCase(name);
        return users.stream()
                .map(UserInfo::fromUser)
                .collect(Collectors.toList());
    }
    
    /**
     * Get users created after specific date
     * @param date Date threshold
     * @return List of users created after the date
     */
    @Transactional(readOnly = true)
    public List<UserInfo> getUsersCreatedAfter(LocalDateTime date) {
        logger.info("Getting users created after: {}", date);
        
        List<User> users = userRepository.findByCreatedAtAfter(date);
        return users.stream()
                .map(UserInfo::fromUser)
                .collect(Collectors.toList());
    }
    
    /**
     * Get total user count
     * @return Total number of users
     */
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.countAllUsers();
    }
    
    /**
     * Get active users (users who have created posts recently)
     * @param days Number of days to look back
     * @return List of active users
     */
    @Transactional(readOnly = true)
    public List<UserInfo> getActiveUsers(int days) {
        logger.info("Getting active users from last {} days", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<User> users = userRepository.findActiveUsers(since);
        
        return users.stream()
                .map(UserInfo::fromUser)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete user (admin function - soft delete by marking as inactive)
     * Note: This is a placeholder for future implementation
     * @param userId User ID to delete
     * @throws Exception if user not found
     */
    public void deleteUser(Long userId) throws Exception {
        logger.warn("Delete user requested for: {} - Not implemented yet", userId);
        throw new Exception("User deletion not implemented yet");
    }
}
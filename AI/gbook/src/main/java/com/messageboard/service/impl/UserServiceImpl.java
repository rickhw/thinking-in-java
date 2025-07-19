package com.messageboard.service.impl;

import com.messageboard.entity.User;
import com.messageboard.repository.UserRepository;
import com.messageboard.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Implementation of UserService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        validateUserForCreation(user);
        
        log.debug("Creating new user with username: {}", user.getUsername());
        
        // Ensure user is active by default
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        
        User savedUser = userRepository.save(user);
        log.info("Created user with ID: {} and username: {}", savedUser.getId(), savedUser.getUsername());
        
        return savedUser;
    }

    @Override
    public User updateUser(Long id, User user) {
        log.debug("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        validateUserForUpdate(user, existingUser);
        
        // Update fields
        if (StringUtils.hasText(user.getUsername()) && !user.getUsername().equals(existingUser.getUsername())) {
            if (!isUsernameAvailable(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }
            existingUser.setUsername(user.getUsername());
        }
        
        if (StringUtils.hasText(user.getEmail()) && !user.getEmail().equals(existingUser.getEmail())) {
            if (!isEmailAvailable(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
            existingUser.setEmail(user.getEmail());
        }
        
        if (user.getDisplayName() != null) {
            existingUser.setDisplayName(user.getDisplayName());
        }
        
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("Updated user with ID: {}", updatedUser.getId());
        
        return updatedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findBySsoId(String ssoId) {
        log.debug("Finding user by SSO ID: {}", ssoId);
        if (!StringUtils.hasText(ssoId)) {
            return Optional.empty();
        }
        return userRepository.findBySsoId(ssoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        if (!StringUtils.hasText(username)) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        if (!StringUtils.hasText(email)) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findActiveUsers(Pageable pageable) {
        log.debug("Finding active users with pagination: {}", pageable);
        return userRepository.findActiveUsers(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users with keyword: {} and pagination: {}", keyword, pageable);
        
        if (!StringUtils.hasText(keyword)) {
            return findActiveUsers(pageable);
        }
        
        String trimmedKeyword = keyword.trim();
        return userRepository.searchActiveUsersByKeyword(trimmedKeyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsersByUsername(String username, Pageable pageable) {
        log.debug("Searching users by username: {} with pagination: {}", username, pageable);
        
        if (!StringUtils.hasText(username)) {
            return findActiveUsers(pageable);
        }
        
        String trimmedUsername = username.trim();
        return userRepository.findActiveUsersByUsernameContaining(trimmedUsername, pageable);
    }

    @Override
    public void deactivateUser(Long id) {
        log.debug("Deactivating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("Deactivated user with ID: {}", id);
    }

    @Override
    public void activateUser(Long id) {
        log.debug("Activating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        log.info("Activated user with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        return !userRepository.existsByUsername(username.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return !userRepository.existsByEmail(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSsoIdAvailable(String ssoId) {
        if (!StringUtils.hasText(ssoId)) {
            return false;
        }
        return !userRepository.existsBySsoId(ssoId.trim());
    }

    /**
     * Validate user data for creation
     */
    private void validateUserForCreation(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (!StringUtils.hasText(user.getSsoId())) {
            throw new IllegalArgumentException("SSO ID cannot be blank");
        }
        
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        
        // Check uniqueness
        if (!isSsoIdAvailable(user.getSsoId())) {
            throw new IllegalArgumentException("SSO ID already exists: " + user.getSsoId());
        }
        
        if (!isUsernameAvailable(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (!isEmailAvailable(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Validate field lengths and formats
        validateUserFields(user);
    }

    /**
     * Validate user data for update
     */
    private void validateUserForUpdate(User user, User existingUser) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Validate field lengths and formats for non-null fields
        validateUserFields(user);
    }

    /**
     * Validate user field constraints
     */
    private void validateUserFields(User user) {
        if (user.getSsoId() != null && user.getSsoId().length() > 255) {
            throw new IllegalArgumentException("SSO ID cannot exceed 255 characters");
        }
        
        if (user.getUsername() != null) {
            String username = user.getUsername().trim();
            if (username.length() < 3 || username.length() > 50) {
                throw new IllegalArgumentException("Username must be between 3 and 50 characters");
            }
        }
        
        if (user.getEmail() != null && user.getEmail().length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }
        
        if (user.getDisplayName() != null && user.getDisplayName().length() > 100) {
            throw new IllegalArgumentException("Display name cannot exceed 100 characters");
        }
        
        if (user.getAvatarUrl() != null && user.getAvatarUrl().length() > 500) {
            throw new IllegalArgumentException("Avatar URL cannot exceed 500 characters");
        }
    }
}
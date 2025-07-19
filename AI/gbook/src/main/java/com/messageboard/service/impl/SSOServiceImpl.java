package com.messageboard.service.impl;

import com.messageboard.entity.User;
import com.messageboard.repository.UserRepository;
import com.messageboard.service.SSOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of SSO service for OAuth2 integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SSOServiceImpl implements SSOService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public User processOAuth2User(OAuth2User oauth2User, String registrationId) {
        log.debug("Processing OAuth2 user from provider: {}", registrationId);
        
        String ssoId = extractSsoId(oauth2User, registrationId);
        String ssoIdWithProvider = registrationId + ":" + ssoId;
        
        Optional<User> existingUser = userRepository.findBySsoId(ssoIdWithProvider);
        
        if (existingUser.isPresent()) {
            log.debug("Found existing user with SSO ID: {}", ssoIdWithProvider);
            return synchronizeUserData(oauth2User, existingUser.get(), registrationId);
        } else {
            log.debug("Creating new user for SSO ID: {}", ssoIdWithProvider);
            return createUserFromOAuth2(oauth2User, registrationId);
        }
    }
    
    @Override
    @Transactional
    public User synchronizeUserData(OAuth2User oauth2User, User existingUser, String registrationId) {
        log.debug("Synchronizing user data for user ID: {}", existingUser.getId());
        
        boolean updated = false;
        
        // Update email if changed
        String email = extractEmail(oauth2User, registrationId);
        if (StringUtils.hasText(email) && !email.equals(existingUser.getEmail())) {
            existingUser.setEmail(email);
            updated = true;
        }
        
        // Update display name if changed
        String displayName = extractDisplayName(oauth2User, registrationId);
        if (StringUtils.hasText(displayName) && !displayName.equals(existingUser.getDisplayName())) {
            existingUser.setDisplayName(displayName);
            updated = true;
        }
        
        // Update avatar URL if changed
        String avatarUrl = extractAvatarUrl(oauth2User, registrationId);
        if (StringUtils.hasText(avatarUrl) && !avatarUrl.equals(existingUser.getAvatarUrl())) {
            existingUser.setAvatarUrl(avatarUrl);
            updated = true;
        }
        
        if (updated) {
            existingUser.setUpdatedAt(LocalDateTime.now());
            existingUser = userRepository.save(existingUser);
            log.debug("Updated user data for user ID: {}", existingUser.getId());
        }
        
        return existingUser;
    }
    
    @Override
    @Transactional
    public User createUserFromOAuth2(OAuth2User oauth2User, String registrationId) {
        String ssoId = extractSsoId(oauth2User, registrationId);
        String ssoIdWithProvider = registrationId + ":" + ssoId;
        
        User newUser = new User();
        newUser.setSsoId(ssoIdWithProvider);
        newUser.setEmail(extractEmail(oauth2User, registrationId));
        newUser.setDisplayName(extractDisplayName(oauth2User, registrationId));
        newUser.setAvatarUrl(extractAvatarUrl(oauth2User, registrationId));
        
        // Generate username from email or display name
        String username = generateUsername(oauth2User, registrationId);
        newUser.setUsername(ensureUniqueUsername(username));
        
        newUser.setIsActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        newUser = userRepository.save(newUser);
        log.info("Created new user with ID: {} from SSO provider: {}", newUser.getId(), registrationId);
        
        return newUser;
    }
    
    @Override
    public String extractSsoId(OAuth2User oauth2User, String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return oauth2User.getAttribute("sub");
            case "github":
                Integer githubId = oauth2User.getAttribute("id");
                return githubId != null ? githubId.toString() : null;
            default:
                // Fallback to common attributes
                String id = oauth2User.getAttribute("id");
                if (id == null) {
                    id = oauth2User.getAttribute("sub");
                }
                return id;
        }
    }
    
    private String extractEmail(OAuth2User oauth2User, String registrationId) {
        return oauth2User.getAttribute("email");
    }
    
    private String extractDisplayName(OAuth2User oauth2User, String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return oauth2User.getAttribute("name");
            case "github":
                String name = oauth2User.getAttribute("name");
                if (!StringUtils.hasText(name)) {
                    name = oauth2User.getAttribute("login");
                }
                return name;
            default:
                String displayName = oauth2User.getAttribute("name");
                if (!StringUtils.hasText(displayName)) {
                    displayName = oauth2User.getAttribute("login");
                }
                if (!StringUtils.hasText(displayName)) {
                    displayName = oauth2User.getAttribute("username");
                }
                return displayName;
        }
    }
    
    private String extractAvatarUrl(OAuth2User oauth2User, String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return oauth2User.getAttribute("picture");
            case "github":
                return oauth2User.getAttribute("avatar_url");
            default:
                String avatarUrl = oauth2User.getAttribute("picture");
                if (!StringUtils.hasText(avatarUrl)) {
                    avatarUrl = oauth2User.getAttribute("avatar_url");
                }
                return avatarUrl;
        }
    }
    
    private String generateUsername(OAuth2User oauth2User, String registrationId) {
        // Try to get username from OAuth2 attributes
        String username = oauth2User.getAttribute("login"); // GitHub
        if (!StringUtils.hasText(username)) {
            username = oauth2User.getAttribute("preferred_username"); // Some providers
        }
        if (!StringUtils.hasText(username)) {
            // Generate from email
            String email = oauth2User.getAttribute("email");
            if (StringUtils.hasText(email) && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            }
        }
        if (!StringUtils.hasText(username)) {
            // Generate from name
            String name = oauth2User.getAttribute("name");
            if (StringUtils.hasText(name)) {
                username = name.toLowerCase().replaceAll("[^a-z0-9]", "");
            }
        }
        if (!StringUtils.hasText(username)) {
            // Fallback to SSO ID
            username = "user" + extractSsoId(oauth2User, registrationId);
        }
        
        return username.toLowerCase();
    }
    
    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
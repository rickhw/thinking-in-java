package com.messageboard.service;

import com.messageboard.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Service interface for SSO integration and user synchronization
 */
public interface SSOService {
    
    /**
     * Process OAuth2 user and create or update local user
     * 
     * @param oauth2User OAuth2 user from SSO provider
     * @param registrationId OAuth2 registration ID (google, github, etc.)
     * @return Local user entity
     */
    User processOAuth2User(OAuth2User oauth2User, String registrationId);
    
    /**
     * Synchronize user data from SSO provider
     * 
     * @param oauth2User OAuth2 user from SSO provider
     * @param existingUser Existing local user
     * @param registrationId OAuth2 registration ID
     * @return Updated user entity
     */
    User synchronizeUserData(OAuth2User oauth2User, User existingUser, String registrationId);
    
    /**
     * Create new user from OAuth2 data
     * 
     * @param oauth2User OAuth2 user from SSO provider
     * @param registrationId OAuth2 registration ID
     * @return New user entity
     */
    User createUserFromOAuth2(OAuth2User oauth2User, String registrationId);
    
    /**
     * Extract SSO ID from OAuth2 user based on provider
     * 
     * @param oauth2User OAuth2 user from SSO provider
     * @param registrationId OAuth2 registration ID
     * @return SSO ID string
     */
    String extractSsoId(OAuth2User oauth2User, String registrationId);
}
package com.messageboard.security;

import com.messageboard.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Custom OAuth2User implementation that includes local user data
 */
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    
    private final OAuth2User oauth2User;
    private final User localUser;
    
    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, all users have USER role
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getName() {
        return localUser.getUsername();
    }
    
    /**
     * Get the local user entity
     * 
     * @return Local user entity
     */
    public User getLocalUser() {
        return localUser;
    }
    
    /**
     * Get the original OAuth2 user
     * 
     * @return Original OAuth2 user
     */
    public OAuth2User getOAuth2User() {
        return oauth2User;
    }
}
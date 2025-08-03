package com.twitterboard.security;

import com.twitterboard.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class CustomUserPrincipal implements UserDetails {
    
    private final User user;
    
    public CustomUserPrincipal(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    public Long getId() {
        return user.getId();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public String getName() {
        return user.getName();
    }
    
    public String getGoogleId() {
        return user.getGoogleId();
    }
    
    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, all users have the same role
        // In the future, this could be extended to support different roles
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getPassword() {
        // OAuth users don't have passwords
        return null;
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserPrincipal that = (CustomUserPrincipal) o;
        return Objects.equals(user.getId(), that.user.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }
    
    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "id=" + user.getId() +
                ", email='" + user.getEmail() + '\'' +
                ", name='" + user.getName() + '\'' +
                '}';
    }
}
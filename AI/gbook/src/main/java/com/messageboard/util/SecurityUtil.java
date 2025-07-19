package com.messageboard.util;

import com.messageboard.security.JwtAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具類別
 * 提供從 Spring Security 上下文中提取使用者資訊的便利方法
 */
public class SecurityUtil {
    
    private SecurityUtil() {
        // 工具類別，不允許實例化
    }
    
    /**
     * 獲取當前認證的使用者名稱
     * 
     * @return 使用者名稱，如果未認證則返回 null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        
        return null;
    }
    
    /**
     * 獲取當前認證的使用者 ID
     * 
     * @return 使用者 ID，如果未認證或無法取得則返回 null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object details = authentication.getDetails();
            
            if (details instanceof JwtAuthenticationDetails) {
                return ((JwtAuthenticationDetails) details).getUserId();
            }
        }
        
        return null;
    }
    
    /**
     * 檢查當前使用者是否已認證
     * 
     * @return true 如果已認證，false 如果未認證
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getName());
    }
    
    /**
     * 獲取當前認證物件
     * 
     * @return Authentication 物件，如果未認證則返回 null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * 檢查當前使用者是否為指定的使用者
     * 
     * @param userId 要檢查的使用者 ID
     * @return true 如果是指定使用者，false 如果不是
     */
    public static boolean isCurrentUser(Long userId) {
        if (userId == null) {
            return false;
        }
        
        Long currentUserId = getCurrentUserId();
        return userId.equals(currentUserId);
    }
    
    /**
     * 檢查當前使用者是否為指定的使用者
     * 
     * @param username 要檢查的使用者名稱
     * @return true 如果是指定使用者，false 如果不是
     */
    public static boolean isCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String currentUsername = getCurrentUsername();
        return username.equals(currentUsername);
    }
}
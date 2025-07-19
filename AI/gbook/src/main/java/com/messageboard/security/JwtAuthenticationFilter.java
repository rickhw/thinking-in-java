package com.messageboard.security;

import com.messageboard.service.TokenBlacklistService;
import com.messageboard.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 認證過濾器
 * 攔截 HTTP 請求，驗證 JWT token 並設定 Spring Security 認證上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (validateToken(token)) {
                    setAuthenticationContext(token, request);
                } else {
                    logger.debug("Invalid JWT token for request: {}", request.getRequestURI());
                }
            }
            
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
            // 清除可能存在的認證上下文
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 從請求中提取 JWT token
     * 
     * @param request HTTP 請求
     * @return JWT token 或 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        
        return null;
    }
    
    /**
     * 驗證 JWT token
     * 
     * @param token JWT token
     * @return true 如果 token 有效，false 如果無效
     */
    private boolean validateToken(String token) {
        try {
            // 檢查 token 是否有效
            if (!jwtUtil.validateToken(token)) {
                logger.debug("JWT token validation failed");
                return false;
            }
            
            // 檢查 token 是否為 Access Token
            if (!jwtUtil.isAccessToken(token)) {
                logger.debug("Token is not an access token");
                return false;
            }
            
            // 檢查 token 是否在黑名單中
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.debug("JWT token is blacklisted");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.debug("Token validation error: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 設定 Spring Security 認證上下文
     * 
     * @param token JWT token
     * @param request HTTP 請求
     */
    private void setAuthenticationContext(String token, HttpServletRequest request) {
        try {
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            
            // 建立 UserDetails 物件
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("") // JWT 認證不需要密碼
                    .authorities(new ArrayList<>()) // 暫時不實作角色權限
                    .build();
            
            // 建立認證物件
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // 設定請求詳細資訊
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 將使用者 ID 加入認證物件的詳細資訊中
            JwtAuthenticationDetails details = new JwtAuthenticationDetails(request, userId);
            authentication.setDetails(details);
            
            // 設定認證上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("JWT authentication successful for user: {} (ID: {})", username, userId);
            
        } catch (Exception e) {
            logger.error("Failed to set authentication context: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
    
    /**
     * 判斷是否應該跳過此過濾器
     * 對於某些端點（如登入、健康檢查等），不需要進行 JWT 驗證
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳過公開端點
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/h2-console") ||
               path.equals("/actuator/health");
    }
}
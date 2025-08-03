package com.twitterboard.service;

import com.twitterboard.config.JwtProperties;
import com.twitterboard.dto.AuthResponse;
import com.twitterboard.dto.GoogleUserInfo;
import com.twitterboard.dto.UserInfo;
import com.twitterboard.entity.User;
import com.twitterboard.repository.UserRepository;
import com.twitterboard.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication operations
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final GoogleOAuthService googleOAuthService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    
    @Autowired
    public AuthService(GoogleOAuthService googleOAuthService, 
                      UserService userService,
                      JwtTokenProvider jwtTokenProvider,
                      JwtProperties jwtProperties) {
        this.googleOAuthService = googleOAuthService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }
    
    /**
     * Authenticate user with Google OAuth authorization code
     * @param authCode Google OAuth authorization code
     * @return Authentication response with tokens and user info
     * @throws Exception if authentication fails
     */
    public AuthResponse authenticateWithGoogle(String authCode) throws Exception {
        logger.info("Starting Google OAuth authentication process");
        
        try {
            // Step 1: Exchange authorization code for user info
            GoogleUserInfo googleUserInfo = googleOAuthService.getUserInfo(authCode);
            logger.info("Successfully retrieved Google user info for ID: {}", googleUserInfo.getId());
            
            // Step 2: Find or create user
            User user = userService.findOrCreateUser(googleUserInfo);
            logger.info("User processed successfully: {}", user.getId());
            
            // Step 3: Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getGoogleId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            
            // Step 4: Create response
            UserInfo userInfo = UserInfo.fromUser(user);
            long expiresIn = jwtProperties.getExpiration() / 1000; // Convert to seconds
            
            AuthResponse response = new AuthResponse(accessToken, refreshToken, userInfo, expiresIn);
            
            logger.info("Authentication successful for user: {}", user.getId());
            return response;
            
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage(), e);
            throw new Exception("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token using refresh token
     * @param refreshToken Refresh token
     * @return New authentication response with fresh tokens
     * @throws Exception if refresh fails
     */
    public AuthResponse refreshToken(String refreshToken) throws Exception {
        logger.info("Starting token refresh process");
        
        try {
            // Step 1: Validate refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new Exception("Invalid refresh token");
            }
            
            if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new Exception("Token is not a refresh token");
            }
            
            if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                throw new Exception("Refresh token has expired");
            }
            
            // Step 2: Extract user ID and get user
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = userService.getUserById(userId);
            
            if (user == null) {
                throw new Exception("User not found");
            }
            
            // Step 3: Generate new tokens
            String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getGoogleId());
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            
            // Step 4: Create response
            UserInfo userInfo = UserInfo.fromUser(user);
            long expiresIn = jwtProperties.getExpiration() / 1000; // Convert to seconds
            
            AuthResponse response = new AuthResponse(newAccessToken, newRefreshToken, userInfo, expiresIn);
            
            logger.info("Token refresh successful for user: {}", user.getId());
            return response;
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage(), e);
            throw new Exception("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate access token and return user info
     * @param accessToken Access token
     * @return User information
     * @throws Exception if validation fails
     */
    @Transactional(readOnly = true)
    public UserInfo validateToken(String accessToken) throws Exception {
        logger.debug("Validating access token");
        
        try {
            // Step 1: Validate token format and signature
            if (!jwtTokenProvider.validateToken(accessToken)) {
                throw new Exception("Invalid access token");
            }
            
            if (!jwtTokenProvider.isAccessToken(accessToken)) {
                throw new Exception("Token is not an access token");
            }
            
            if (jwtTokenProvider.isTokenExpired(accessToken)) {
                throw new Exception("Access token has expired");
            }
            
            // Step 2: Extract user ID and get user
            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            User user = userService.getUserById(userId);
            
            if (user == null) {
                throw new Exception("User not found");
            }
            
            // Step 3: Return user info
            UserInfo userInfo = UserInfo.fromUser(user);
            logger.debug("Token validation successful for user: {}", user.getId());
            
            return userInfo;
            
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            throw new Exception("Token validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Get remaining time until token expires
     * @param token JWT token
     * @return Remaining time in seconds
     */
    public long getTokenRemainingTime(String token) {
        try {
            return jwtTokenProvider.getRemainingTime(token) / 1000; // Convert to seconds
        } catch (Exception e) {
            logger.error("Error getting token remaining time: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Check if token is about to expire (within 5 minutes)
     * @param token JWT token
     * @return true if token expires soon
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            long remainingTime = jwtTokenProvider.getRemainingTime(token);
            return remainingTime < 300000; // 5 minutes in milliseconds
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true; // Assume expired if we can't check
        }
    }
}
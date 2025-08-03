package com.twitterboard.security;

import com.twitterboard.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    
    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generate JWT access token
     * @param userId User ID
     * @param email User email
     * @param googleId Google ID
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String email, String googleId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("googleId", googleId);
        claims.put("type", "access");
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Generate JWT refresh token
     * @param userId User ID
     * @return Refresh token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Get user ID from JWT token
     * @param token JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * Get email from JWT token
     * @param token JWT token
     * @return Email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }
    
    /**
     * Get Google ID from JWT token
     * @param token JWT token
     * @return Google ID
     */
    public String getGoogleIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("googleId", String.class);
    }
    
    /**
     * Get token type from JWT token
     * @param token JWT token
     * @return Token type (access/refresh)
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }
    
    /**
     * Get expiration date from JWT token
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * Validate JWT token
     * @param token JWT token
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Check if token is access token
     * @param token JWT token
     * @return true if access token
     */
    public boolean isAccessToken(String token) {
        try {
            String type = getTokenTypeFromToken(token);
            return "access".equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error checking token type: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is refresh token
     * @param token JWT token
     * @return true if refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            String type = getTokenTypeFromToken(token);
            return "refresh".equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error checking token type: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get claims from JWT token
     * @param token JWT token
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Get remaining time until token expires (in milliseconds)
     * @param token JWT token
     * @return Remaining time in milliseconds
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error getting remaining time: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Extract token from Authorization header
     * @param authHeader Authorization header value
     * @return JWT token or null if invalid format
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
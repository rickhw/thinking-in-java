package com.messageboard.util;

import com.messageboard.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具類別
 * 提供 JWT token 的生成、驗證和解析功能
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    
    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成 Access Token
     * 
     * @param userId 使用者 ID
     * @param username 使用者名稱
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.USER_ID_CLAIM, userId);
        claims.put(JwtConstants.USERNAME_CLAIM, username);
        claims.put(JwtConstants.TOKEN_TYPE_CLAIM, JwtConstants.ACCESS_TOKEN_TYPE);
        
        return createToken(claims, username, jwtConfig.getAccessTokenExpiration());
    }
    
    /**
     * 生成 Refresh Token
     * 
     * @param userId 使用者 ID
     * @param username 使用者名稱
     * @return JWT refresh token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.USER_ID_CLAIM, userId);
        claims.put(JwtConstants.USERNAME_CLAIM, username);
        claims.put(JwtConstants.TOKEN_TYPE_CLAIM, JwtConstants.REFRESH_TOKEN_TYPE);
        
        return createToken(claims, username, jwtConfig.getRefreshTokenExpiration());
    }
    
    /**
     * 建立 JWT token
     * 
     * @param claims token 聲明
     * @param subject token 主體
     * @param expiration 過期時間（毫秒）
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 從 token 中提取使用者名稱
     * 
     * @param token JWT token
     * @return 使用者名稱
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * 從 token 中提取使用者 ID
     * 
     * @param token JWT token
     * @return 使用者 ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(JwtConstants.USER_ID_CLAIM, Long.class));
    }
    
    /**
     * 從 token 中提取過期時間
     * 
     * @param token JWT token
     * @return 過期時間
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * 從 token 中提取 token 類型
     * 
     * @param token JWT token
     * @return token 類型 (access/refresh)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(JwtConstants.TOKEN_TYPE_CLAIM, String.class));
    }
    
    /**
     * 從 token 中提取指定的聲明
     * 
     * @param token JWT token
     * @param claimsResolver 聲明解析器
     * @param <T> 返回類型
     * @return 聲明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 從 token 中提取所有聲明
     * 
     * @param token JWT token
     * @return 所有聲明
     */
    private Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("JWT token cannot be null or empty");
        }
        
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 檢查 token 是否過期
     * 
     * @param token JWT token
     * @return true 如果過期，false 如果未過期
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 驗證 token 是否有效
     * 
     * @param token JWT token
     * @param username 預期的使用者名稱
     * @return true 如果有效，false 如果無效
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.warn("Token validation failed for user {}: {}", username, e.getMessage());
            return false;
        }
    }
    
    /**
     * 驗證 token 是否有效（不檢查使用者名稱）
     * 
     * @param token JWT token
     * @return true 如果有效，false 如果無效
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 檢查是否為 Access Token
     * 
     * @param token JWT token
     * @return true 如果是 Access Token
     */
    public boolean isAccessToken(String token) {
        try {
            return JwtConstants.ACCESS_TOKEN_TYPE.equals(extractTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 檢查是否為 Refresh Token
     * 
     * @param token JWT token
     * @return true 如果是 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            return JwtConstants.REFRESH_TOKEN_TYPE.equals(extractTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 計算 token 的雜湊值（用於黑名單）
     * 
     * @param token JWT token
     * @return token 的 SHA-256 雜湊值
     */
    public String getTokenHash(String token) {
        if (token == null) {
            throw new RuntimeException("Failed to generate token hash");
        }
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Failed to generate token hash: {}", e.getMessage());
            throw new RuntimeException("Failed to generate token hash", e);
        }
    }
}
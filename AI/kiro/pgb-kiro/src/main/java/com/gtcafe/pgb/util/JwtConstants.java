package com.gtcafe.pgb.util;

/**
 * JWT 相關常數定義
 */
public final class JwtConstants {
    
    // Token 類型
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    
    // HTTP Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Claims 鍵名
    public static final String USER_ID_CLAIM = "userId";
    public static final String USERNAME_CLAIM = "username";
    public static final String TOKEN_TYPE_CLAIM = "type";
    
    // Redis 鍵前綴
    public static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";
    
    // 錯誤訊息
    public static final String INVALID_TOKEN_MESSAGE = "Invalid JWT token";
    public static final String EXPIRED_TOKEN_MESSAGE = "JWT token has expired";
    public static final String BLACKLISTED_TOKEN_MESSAGE = "JWT token has been blacklisted";
    public static final String MALFORMED_TOKEN_MESSAGE = "Malformed JWT token";
    public static final String UNSUPPORTED_TOKEN_MESSAGE = "Unsupported JWT token";
    public static final String SIGNATURE_EXCEPTION_MESSAGE = "JWT signature does not match";
    
    // 私有建構子防止實例化
    private JwtConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
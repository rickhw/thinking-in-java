package com.messageboard.security;

import com.messageboard.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Security 配置整合測試
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {
    
    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private MockMvc mockMvc;
    
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }
    
    @Test
    void testPublicEndpointsAccessible() throws Exception {
        setUp();
        
        // 測試健康檢查端點
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        // 測試 API 文件端點
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
        
        // 測試登入端點（應該返回 404 因為還沒實作 controller）
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testProtectedEndpointsRequireAuthentication() throws Exception {
        setUp();
        
        // 測試受保護的 API 端點需要認證
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/messages"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testValidJwtTokenAllowsAccess() throws Exception {
        setUp();
        
        // 生成有效的 JWT token
        String token = jwtUtil.generateAccessToken(1L, "testuser");
        
        // 使用有效 token 存取受保護端點（應該返回 404 因為還沒實作 controller）
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testInvalidJwtTokenDeniesAccess() throws Exception {
        setUp();
        
        // 使用無效 token
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        
        // 使用錯誤格式的 Authorization header
        mockMvc.perform(get("/api/users")
                .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testCorsConfiguration() throws Exception {
        setUp();
        
        // 測試 CORS 預檢請求
        mockMvc.perform(options("/api/users")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
    
    @Test
    void testRefreshTokenNotAcceptedForApiAccess() throws Exception {
        setUp();
        
        // 生成 Refresh Token
        String refreshToken = jwtUtil.generateRefreshToken(1L, "testuser");
        
        // Refresh Token 不應該被接受用於 API 存取
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }
}
package com.gtcafe.asimov.controller.api;


import java.lang.reflect.Field;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class SessionController {
    
    @GetMapping("/session-info")
    public Map<String, Object> getSessionInfo(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        
        // 獲取 Session ID
        sessionInfo.put("sessionId", session.getId());
        
        // 獲取 Session 創建時間
        sessionInfo.put("creationTime", new Date(session.getCreationTime()));
        
        // 獲取最後訪問時間
        sessionInfo.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
        
        // 獲取 Session 最大閒置時間（秒）
        sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        // 獲取 Session 是否為新建
        sessionInfo.put("isNew", session.isNew());
        
        // 獲取 Session 中所有屬性
        Enumeration<String> attributeNames = session.getAttributeNames();
        Map<String, Object> attributes = new HashMap<>();
        
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            attributes.put(name, session.getAttribute(name));
        }
        
        sessionInfo.put("attributes", attributes);
        
        return sessionInfo;
    }


    @Autowired
    private ApplicationContext context;
    
    @GetMapping("/test-session-config")
    public Map<String, Object> testSessionConfig(HttpServletRequest request, HttpSession session) {
        Map<String, Object> config = new HashMap<>();
        
        // 獲取 sessionId
        config.put("sessionId", session.getId());
        config.put("cookieName", request.getSession().getServletContext()
            .getSessionCookieConfig().getName());
        
        // 獲取 Redis 命名空間配置
        RedisIndexedSessionRepository repo = context.getBean(RedisIndexedSessionRepository.class);
        if (repo != null) {
            // 嘗試通過反射獲取 namespace
            try {
                Field namespaceField = RedisIndexedSessionRepository.class
                    .getDeclaredField("namespace");
                namespaceField.setAccessible(true);
                config.put("redisNamespace", namespaceField.get(repo));
            } catch (Exception e) {
                config.put("error", e.getMessage());
            }
        }
        
        return config;
    }
}
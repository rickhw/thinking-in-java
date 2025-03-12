package com.gtcafe.asimov.config;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    public Long getTenantIdFromToken(String token) {
        // 假設從 JWT token 中解碼取得 tenantId
        return 1L;
    }

    public Boolean getAwarenessFromToken(String token) {
        // 假設從 JWT token 中解碼取得 awareness
        return true;
    }
}

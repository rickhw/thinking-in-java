package com.gtcafe.asimov.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtils {
    private static final String SECRET_KEY = "your-secret-key";

    public static String extractTenantId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("tenantId", String.class);
    }
}

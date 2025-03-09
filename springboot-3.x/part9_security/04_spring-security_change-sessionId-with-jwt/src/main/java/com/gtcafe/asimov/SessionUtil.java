package com.gtcafe.asimov;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    public static void printSessionInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                System.out.printf("使用者名稱: [%s]\n", userDetails.getUsername());
                System.out.printf("使用者角色: [%s]\n", userDetails.getAuthorities());
            }
        }

        HttpSession session = request.getSession(false); // false: 如果沒有 Session 就回傳 null
        if (session != null) {
            System.out.printf("Session ID: [%s]\n", session.getId());
            System.out.printf("Session Timeout: [%s]\n", session.getMaxInactiveInterval());
            System.out.printf("Session 創建時間: [%s]\n", session.getCreationTime());
            System.out.printf("Session 最後存取時間: [%s]\n", session.getLastAccessedTime());
        }
    }
}
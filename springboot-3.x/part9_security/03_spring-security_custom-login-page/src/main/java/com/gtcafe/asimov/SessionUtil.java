package com.gtcafe.asimov;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionUtil {

    public static void printSessionInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                log.info("username: [{}], roles: [{}]", userDetails.getUsername(), userDetails.getAuthorities());
            }
        }

        HttpSession session = request.getSession(false); // false: 如果沒有 Session 就回傳 null
        if (session != null) {
            log.info("session: id: [{}], timeout: [{}], creationTime: [{}], lastAccessTime: [{}]", session.getId(), session.getMaxInactiveInterval(), session.getCreationTime(), session.getLastAccessedTime());
        }
    }
}
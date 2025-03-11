package com.gtcafe.asimov;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionUtil {

    public static void printSessionInfo(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                model.addAttribute("roles", userDetails.getAuthorities());
                log.info("username: [{}], roles: [{}]", userDetails.getUsername(), userDetails.getAuthorities());

            }
        }

        HttpSession session = request.getSession(false); // false: 如果沒有 Session 就回傳 null
        if (session != null) {
            
            // model.addAttribute("session", session);
            model.addAttribute("sessionId", session.getId());
            model.addAttribute("sessionTimeout", session.getMaxInactiveInterval());
            model.addAttribute("sessionCreationTime", session.getCreationTime());
            model.addAttribute("sessionLastAccessTime", session.getLastAccessedTime());

            log.info("sessionId: [{}], timeout: [{}], creationTime: [{}], lastAccessTime: [{}]", session.getId(), session.getMaxInactiveInterval(), session.getCreationTime(), session.getLastAccessedTime());
        }
    }
}
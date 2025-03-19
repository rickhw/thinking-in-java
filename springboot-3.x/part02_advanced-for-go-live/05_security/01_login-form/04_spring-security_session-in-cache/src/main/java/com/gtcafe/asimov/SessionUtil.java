package com.gtcafe.asimov;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public static void setHeaderInfo(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String hostname = getHostName();
        String clientIp = getClientIp(request);

        model.addAttribute("username", auth.getName());
        model.addAttribute("computeNodeHostname", hostname);
        model.addAttribute("clientIp", clientIp);

        SessionUtil.printSessionInfo(model, request);
    }


    public static void setSessionInfo(Model model, HttpSession session) {
        // Map<String, Object> sessionInfo = new HashMap<>();

        // 獲取 Session ID
        model.addAttribute("sessionId", session.getId());

        // 獲取 Session 創建時間
        model.addAttribute("creationTime", new Date(session.getCreationTime()));

        // 獲取最後訪問時間
        model.addAttribute("lastAccessedTime", new Date(session.getLastAccessedTime()));

        // 獲取 Session 最大閒置時間（秒）
        model.addAttribute("maxInactiveInterval", session.getMaxInactiveInterval());

        // 獲取 Session 是否為新建
        model.addAttribute("isNew", session.isNew());

        // 獲取 Session 中所有屬性
        Enumeration<String> attributeNames = session.getAttributeNames();
        Map<String, Object> attributes = new HashMap<>();

        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            attributes.put(name, session.getAttribute(name));
        }

        model.addAttribute("attributes", attributes);

        // return sessionInfo;
    }


    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(forwarded -> forwarded.split(",")[0].trim()) // 取第一個 IP
                .orElseGet(() -> Optional.ofNullable(request.getHeader("X-Real-IP"))
                        .orElse(request.getRemoteAddr()));

        // 檢查是否為 IPv6 並轉換為 IPv4
        if (ip.contains(":")) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                if (inetAddress instanceof Inet6Address) {
                    ip = convertIPv6ToIPv4(inetAddress);
                }
            } catch (UnknownHostException e) {
                return "Unknown IP";
            }
        }
        return ip;
    }

    public static String convertIPv6ToIPv4(InetAddress inetAddress) {
        if (inetAddress.isLoopbackAddress()) {
            return "127.0.0.1"; // 本機 IPv6 轉換
        }
        byte[] ipv4Bytes = inetAddress.getAddress();
        if (ipv4Bytes.length == 16) {
            return String.format("%d.%d.%d.%d", ipv4Bytes[12] & 0xFF, ipv4Bytes[13] & 0xFF, ipv4Bytes[14] & 0xFF,
                    ipv4Bytes[15] & 0xFF);
        }
        return inetAddress.getHostAddress(); // 若非 IPv6，直接回傳
    }
}
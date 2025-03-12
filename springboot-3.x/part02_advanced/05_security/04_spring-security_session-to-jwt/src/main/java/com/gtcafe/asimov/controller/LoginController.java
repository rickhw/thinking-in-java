package com.gtcafe.asimov.controller;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gtcafe.asimov.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String hostname = getHostName();
        String clientIp = getClientIp(request);

        model.addAttribute("username", auth.getName());
        model.addAttribute("computeNodeHostname", hostname);
        model.addAttribute("clientIp", clientIp);

        SessionUtil.printSessionInfo(model, request);

        return "login"; // 返回模板名稱
    }

    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String hostname = getHostName();
        String clientIp = getClientIp(request);

        model.addAttribute("username", auth.getName());
        model.addAttribute("computeNodeHostname", hostname);
        model.addAttribute("clientIp", clientIp);

        SessionUtil.printSessionInfo(model, request);

        return "home"; // 返回首頁模板
    }




    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

    private String getClientIp(HttpServletRequest request) {
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

    private String convertIPv6ToIPv4(InetAddress inetAddress) {
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

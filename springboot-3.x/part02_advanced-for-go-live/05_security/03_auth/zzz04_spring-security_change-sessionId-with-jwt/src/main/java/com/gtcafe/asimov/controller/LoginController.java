package com.gtcafe.asimov.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gtcafe.asimov.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // 返回模板名稱
    }

    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        model.addAttribute("username", auth.getName());

        SessionUtil.printSessionInfo(request);

        return "home"; // 返回首頁模板
    }
}
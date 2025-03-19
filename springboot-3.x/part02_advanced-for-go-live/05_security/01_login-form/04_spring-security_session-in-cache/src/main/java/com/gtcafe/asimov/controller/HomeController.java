package com.gtcafe.asimov.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gtcafe.asimov.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeController {


    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        
        SessionUtil.setHeaderInfo(model, request);
        return "home"; // 返回首頁模板
    }

    @GetMapping("/session")
    public String showSession(Model model, HttpServletRequest request) {
        SessionUtil.setHeaderInfo(model, request);
        SessionUtil.setSessionInfo(model, request.getSession());

        return "insession/session"; // 返回首頁模板
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        SessionUtil.setHeaderInfo(model, request);
        return "insession/profile"; // 返回首頁模板
    }

}

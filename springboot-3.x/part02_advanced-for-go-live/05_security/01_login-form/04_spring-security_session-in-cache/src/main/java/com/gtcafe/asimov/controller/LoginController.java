package com.gtcafe.asimov.controller;

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
        SessionUtil.setHeaderInfo(model, request);
        return "login"; // 返回模板名稱
    }

}

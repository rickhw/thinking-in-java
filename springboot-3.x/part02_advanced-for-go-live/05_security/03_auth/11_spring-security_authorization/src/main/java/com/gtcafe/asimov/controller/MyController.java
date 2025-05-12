package com.gtcafe.asimov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/register")
    public String register() {
        return "註冊畫面";
    }
    
    @GetMapping("/home")
    public String home() {
        return "系統首頁";
    }

    @GetMapping("/selected-courses")
    public String selectedCourses() {
        return "修課清單";
    }

    @GetMapping("/course-feedback")
    public String courseFeedback() {
        return "課程回饋";
    }

    @GetMapping("/members")
    public String members() {
        return "使用者列表";
    }
}

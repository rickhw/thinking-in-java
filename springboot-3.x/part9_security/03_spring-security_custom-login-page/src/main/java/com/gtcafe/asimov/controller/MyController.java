package com.gtcafe.asimov.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.repository.MemberRepository;

@RestController
public class MyController {

    @Autowired
    private MemberRepository memberRepository;

     @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/selected-courses")
    public String selectedCourses() {
        return "修課清單";
    }

    @GetMapping("/course-feedback")
    public String courseFeedback() {
        return "課程回饋";
    }

}

package com.gtcafe.springbootlab.day01.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
// import com.gtcafe.springbootlab.day01.AppConfigModel;

@RestController
public class RootController {

    // injects smtp.gmail.com
    @Value("${email.smtp.server}")
    private String server;

    // injects 467
    @Value("${email.smtp.port}")
    private Integer port;

    // injects hello@gmail.com
    @Value("${email.smtp.username}")
    private String username;

    @GetMapping("/email")
    public String getEmail() {
        return username;
    }
}

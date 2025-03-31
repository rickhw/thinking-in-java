package com.gtcafe.asimov.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public String createUser(@RequestParam String name) {
        userService.createUser(name);
        return "User created: " + name;
    }

    @PostMapping("/create-fail")
    public String createUserWithRollback(@RequestParam String name) {
        try {
            userService.createUserWithRollback(name);
        } catch (Exception e) {
            return "User creation failed: " + e.getMessage();
        }
        return "User created: " + name;
    }
}
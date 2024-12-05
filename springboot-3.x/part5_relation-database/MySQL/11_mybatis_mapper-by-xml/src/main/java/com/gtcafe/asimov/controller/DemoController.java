package com.gtcafe.asimov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.model.Department;
import com.gtcafe.asimov.model.User;
import com.gtcafe.asimov.service.DemoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {
    private final DemoService demoService;

    @GetMapping("/user/{id}")
    public User getUserWithDetail(@PathVariable Integer id) {
        return demoService.getUserWithDetailById(id);
    }

    @GetMapping("/department/{id}")
    public Department getDepartmentWithEmployees(@PathVariable Integer id) {
        return demoService.getDepartmentWithEmployees(id);
    }
}
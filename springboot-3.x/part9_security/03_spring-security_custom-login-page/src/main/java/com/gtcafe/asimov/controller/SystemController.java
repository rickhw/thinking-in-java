package com.gtcafe.asimov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

}
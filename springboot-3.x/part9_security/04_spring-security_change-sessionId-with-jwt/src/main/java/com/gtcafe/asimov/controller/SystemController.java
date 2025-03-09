package com.gtcafe.asimov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/_")
@Slf4j
public class SystemController {

    @GetMapping("/health")
    public String health() {
        log.info("Health check");
        return "OK";
    }

}
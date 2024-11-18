package com.gtcafe.asimov.platform.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalApiController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Service is healthy!";
    }
}
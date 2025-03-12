package com.gtcafe.asimov;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicApiController {

    @GetMapping("/version")
    public String version() {
        return "API Version 1.0";
    }
}
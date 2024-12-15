package com.gtcafe.asimov;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private static int count = 0;

    @GetMapping("/hello")
    public String hello() {
        count++;
        System.out.println("    - /demo/hello: request count = " + count);
        
        return "Hello, Rate Limited World!";
    }
}
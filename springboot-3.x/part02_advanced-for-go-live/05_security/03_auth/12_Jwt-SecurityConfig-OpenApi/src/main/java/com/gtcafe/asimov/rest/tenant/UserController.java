package com.gtcafe.asimov.rest.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public String create() {
        return "create";
    }

    @GetMapping("/{id}")
    public String retrieve(@PathVariable String id) {
        return "retrieve";
    }


}
package com.gtcafe.asimov.rest.provider;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis/users")
public class UserControllerS {

    @PostMapping
    public String create() {
        return "create";
    }

    @GetMapping("/{id}")
    public String retrieve(@PathVariable String id) {
        return "retrieve";
    }

    @PutMapping("/{id}")
    public String replace(@PathVariable String id) {
        return "replace";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable String id) {
        return "replace";
    }
}
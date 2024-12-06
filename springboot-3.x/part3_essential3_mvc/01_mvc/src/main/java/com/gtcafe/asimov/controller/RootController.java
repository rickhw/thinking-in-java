package com.gtcafe.asimov.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
	@RequestMapping("/")
	public String hello() {
		return "Hey, Spring Boot 的 Hello World !";

	}
}

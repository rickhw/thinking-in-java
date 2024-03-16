package com.gtcafe.springbootlab.day01.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NormalController {

  private static final Logger logger = LoggerFactory.getLogger(NormalController.class);

  @GetMapping("/public")
  public ResponseEntity<String> publicAction() {

    logger.info("start of publicAction(): /public");

    // 5. response message
    return ResponseEntity.ok("public");
  }

  @GetMapping("/auth-needed")
  public ResponseEntity<String> protectedNeeded() {

    logger.info("start of protectedNeeded(): /auth-needed");

    // 5. response message
    return ResponseEntity.ok("auth-needed");
  }

}

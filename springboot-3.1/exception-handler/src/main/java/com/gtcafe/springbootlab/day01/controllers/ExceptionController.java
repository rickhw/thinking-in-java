package com.gtcafe.springbootlab.day01.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.springbootlab.day01.payload.response.GlobalExceptionResponse;

@RestController
public class ExceptionController {

  private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

  @GetMapping("/throws-exception")
  public ResponseEntity<String> throwDefaultExceptions() throws Exception {
    throw new Exception("unexpected exception");
  }


  @GetMapping("/throws-global-exception")
  public ResponseEntity<String> throwGlobalExceptions() throws GlobalExceptionResponse {
    throw new GlobalExceptionResponse("UnexpectedException", "Unexpected Exception Occurs, Please contact with adminsitrator.");
  }

}

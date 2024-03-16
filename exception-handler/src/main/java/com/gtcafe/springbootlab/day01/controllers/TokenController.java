package com.gtcafe.springbootlab.day01.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.springbootlab.day01.payload.request.CreateAppTokenRequest;
import com.gtcafe.springbootlab.day01.payload.request.DomainObjectRequest;
import com.gtcafe.springbootlab.day01.payload.response.CreateAppTokenResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/tokens")
@Validated  // for path parameter validation
public class TokenController {

  private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

  // lab01-1: basic validate the payload
  @PostMapping("/")
  public ResponseEntity<CreateAppTokenResponse> createToken(
      @Valid @RequestBody CreateAppTokenRequest request) {

    logger.info("start createToken() /createToken");

    CreateAppTokenResponse res = new CreateAppTokenResponse(123L, request.getTokenName());

    // 5. response message
    return ResponseEntity.ok(res);
  }

  // lab01-2: validate the http headers
  @PostMapping("/validate-http-header")
  public ResponseEntity<String> validateHttpHeaders(
      @Valid @RequestBody CreateAppTokenRequest request,
      @RequestHeader("X-Auth-Token") String authToken) {

    logger.info("start validateHttpHeaders() /validate-http-header");

    // CreateAppTokenResponse res = new CreateAppTokenResponse(123L, request.getTokenName());

    // 5. response message
    return ResponseEntity.ok(authToken);
  }

  // lab01-3: nested object
  @PostMapping("/nested-object")
  public ResponseEntity<String> nestedObject(
      @Valid @RequestBody DomainObjectRequest request) {

    logger.info("start nestedObject() /nested-object");
    logger.info(" - $.kind: [{}]", request.getKind());
    logger.info(" - $.metadata.name: [{}]", request.getMetadata().getName());
    logger.info(" - $.metadata.label: [{}]", request.getMetadata().getLabel());
    logger.info(" - $.metadata.email: [{}]", request.getMetadata().getEmail());

    // CreateAppTokenResponse res = new CreateAppTokenResponse(123L, request.getTokenName());

    // 5. response message
    return ResponseEntity.ok("ok");
  }

  // lab01-4: Path Parameters
  @GetMapping("/name/{name}/age/{age}")
  public ResponseEntity<String> pathParams(
      // @Valid // not support
      @PathVariable("name") @NotBlank String name,
      @PathVariable("age") @Positive(message = "Student ID must be greater than zero") int age) {

    logger.info("start pathParams() /name/{name}/age/{age}");
    logger.info(" - name: [{}]", name);
    logger.info(" - age: [{}]", age);

    // CreateAppTokenResponse res = new CreateAppTokenResponse(123L, request.getTokenName());

    // 5. response message
    return ResponseEntity.ok("ok");
  }

  // @ExceptionHandler(MethodArgumentNotValidException.class)
  // public ResponseEntity<Map<String, String>>
  // handleValidationException(MethodArgumentNotValidException ex) {
  // Map<String, String> errors = new HashMap<>();
  // ex.getBindingResult().getAllErrors().forEach((error) -> {
  // String fieldName = ((FieldError) error).getField();
  // String errorMessage = error.getDefaultMessage();
  // errors.put(fieldName, errorMessage);
  // });
  // return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  // }
}

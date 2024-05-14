package com.gtcafe.springbootlab.day01.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.gtcafe.springbootlab.day01.ErrorDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

// lab01-1: basic validate the payload with global handler
@ControllerAdvice
public class GlobalExceptionHandler2 {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler2.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();

        ex.getAllErrors().forEach(err -> errors.add(err.getDefaultMessage()));

        Map<String, List<String>> result = new HashMap<>();
        result.put("errors", errors);

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // HttpMessage Invalid: Json Payload Invalid
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> jsonNotValid(HttpMessageNotReadableException ex, HttpServletRequest request) {
        // List<String> errors = new ArrayList<>();

        // ex.

        // ex.getAllErrors().forEach(err -> errors.add(err.getDefaultMessage()));

        Map<String, String> result = new HashMap<>();
        result.put("errors", ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // @ref: https://www.codejava.net/frameworks/spring-boot/rest-api-validate-path-variables-examples
    // 會留下 exception log
    // path parameter exception handler
    // @ExceptionHandler(ConstraintViolationException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    // @ResponseBody
    // public ErrorDTO handleConstraintViolationException(HttpServletRequest request, Exception ex) {
    //     ErrorDTO error = new ErrorDTO();

    //     error.setTimestamp(new Date());
    //     error.setStatus(HttpStatus.BAD_REQUEST.value());
    //     error.addError(ex.getMessage());
    //     error.setPath(request.getServletPath());

    //     logger.error(ex.getMessage(), ex);

    //     return error;
    // }

    // 不會留下 exception log
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        ErrorDTO error = new ErrorDTO();

        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.addError(ex.getMessage());
        error.setPath(request.getServletPath());

        // logger.error(ex.getMessage(), ex);
        // Map<String, String> result = new HashMap<>();
        // result.put("errors", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
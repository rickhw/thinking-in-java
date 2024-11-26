package com.gtcafe.rws.booter.exception;

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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.gtcafe.rws.booter.payload.standard.response.InvalidRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

// lab01-1: basic validate the payload with global handler
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    // public ResponseEntity<String>
    // handleValidationException(MethodArgumentNotValidException ex) {
    // return ResponseEntity.badRequest().body("參數驗證失敗：" +
    // ex.getBindingResult().getFieldError().getDefaultMessage());
    // }

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {

    //     Map<String, String> errors = new HashMap<>();
    //     ex.getBindingResult().getAllErrors().forEach((error) -> {
    //         String fieldName = ((FieldError) error).getField();
    //         String errorMessage = error.getDefaultMessage();
    //         errors.put(fieldName, errorMessage);
    //     });

    //     return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    // }

    @ExceptionHandler(Error.class)
    public ResponseEntity<?> handleGlobalError(
            HttpServletRequest request,
            HttpServletResponse response, Exception ex) {

        logger.error("Global Error, message: {}\n{}", ex.getMessage(), ex);

        // do something with request and response
        Map<String, String> result = new HashMap<>();
        result.put("errors", ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            HttpServletRequest request,
            HttpServletResponse response, Exception ex) {

        // logger.error(ex.getMessage(), ex);
        logger.error("Global Exception, message: {}\n{}", ex.getMessage(), ex);

        // do something with request and response
        Map<String, String> result = new HashMap<>();
        result.put("errors", ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {

        logger.error(ex.getMessage(), ex);

        Map<String, String> result = new HashMap<>();
        result.put("errors", ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

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

        Map<String, String> result = new HashMap<>();
        result.put("errors", ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<InvalidRequest> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        InvalidRequest error = new InvalidRequest();

        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.addError(ex.getMessage());
        error.setPath(request.getServletPath());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
package com.gtcafe.springbootlab.day01.handlers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.gtcafe.springbootlab.day01.payload.response.GlobalExceptionResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    // private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleDefaultException(
            HttpServletRequest request,
            HttpServletResponse response, 
            Exception ex
        ) {

        Map<String, String> result = new HashMap<>();
        result.put("class", ex.getClass().getName());
        result.put("message", ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GlobalExceptionResponse.class)
    public ResponseEntity<?> handleGlobalException(
            HttpServletRequest request,
            HttpServletResponse response, 
            GlobalExceptionResponse ex
        ) {

        System.out.println("handleGlobalException");
        // process the data structure of response 
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", ex.getErrorCode());
        result.put("message", ex.getErrorMessage());
        result.put("timestamp", ex.getTimestamp().toString());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}
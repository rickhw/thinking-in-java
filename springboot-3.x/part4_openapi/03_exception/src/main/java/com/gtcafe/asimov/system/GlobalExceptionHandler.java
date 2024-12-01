package com.gtcafe.asimov.system;


import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import com.gtcafe.asimov.system.exception.ExceptionModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionModel> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request, HandlerMethod handlerMethod) {

        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        log.error("controllerName: [{}], methodName: [{}]", controllerName, methodName);

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.error("Validation failed: {}", errors);
        
        // ErrorResponse error = ErrorResponse.builder()
        //         .timestamp(LocalDateTime.now())
        //         .status(HttpStatus.BAD_REQUEST.value())
        //         .error("Validation Failed")
        //         .message(errors)
        //         .path(request.getDescription(false))
        //         .build();

        ExceptionModel error = ExceptionModel.builder()
                .timestamp(LocalDateTime.now())
                .message(errors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionModel> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        ExceptionModel error = ExceptionModel.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                // .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
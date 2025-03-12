package com.gtcafe.asimov.system;


import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.gtcafe.asimov.system.exception.ExceptionModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionModel> catchMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request, HandlerMethod handlerMethod) {

        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        log.error("From controller: [{}], method: [{}], exception: {}", controllerName, methodName, ex);

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
                .errorCode("MethodArgumentNotValidException")
                .message(errors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionModel> catahHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, WebRequest request, HandlerMethod handlerMethod) {

        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        log.error("From controller: [{}], method: [{}], exception: {}", controllerName, methodName, ex);
        
        ExceptionModel error = ExceptionModel.builder()
                .kind("kind")
                .errorCode("X01")
                .errorMessage("HttpMessageNotReadableException")
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ExceptionModel> catchJsonMappingException(
        HttpMessageNotReadableException ex, WebRequest request, HandlerMethod handlerMethod) {
        return handleJsonPayloadException(ex, request, handlerMethod);
    }

    // @ExceptionHandler(JsonParseException.class)
    // public ResponseEntity<ExceptionModel> catchJsonParseException(
    //     HttpMessageNotReadableException ex, WebRequest request, HandlerMethod handlerMethod) {
    //     return handleJsonPayloadException(ex, request, handlerMethod);
    // }

    private ResponseEntity<ExceptionModel> handleJsonPayloadException(
        HttpMessageNotReadableException ex, WebRequest request, HandlerMethod handlerMethod) {

        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        log.error("From controller: [{}], method: [{}], exception: {}", controllerName, methodName, ex);
        
        ExceptionModel error = ExceptionModel.builder()
                .kind("kind")
                .errorCode("X01")
                .errorMessage("JsonMappingException")
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ExceptionModel> handleHttpMediaTypeNotSupportedException(
            Exception ex, WebRequest request, HandlerMethod handlerMethod) {
        
        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();
        log.error("From controller: [{}], method: [{}], exception: {}", controllerName, methodName, ex);
        
        ExceptionModel error = ExceptionModel.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                // .error("Internal Server Error")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionModel> handleAllUncaughtException(
            Exception ex, WebRequest request, HandlerMethod handlerMethod) {
        
        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();
        log.error("From controller: [{}], method: [{}], exception: {}", controllerName, methodName, ex);
        
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
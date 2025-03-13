package com.gtcafe.domain;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gtcafe.domain.exception.DomainException;
import com.gtcafe.system.response.StandardErrorResponse;

@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<StandardErrorResponse> handleUnauthorizedException(DomainException ex) {
        StandardErrorResponse errorResponse = StandardErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(), 
            "Unauthorized", 
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
}
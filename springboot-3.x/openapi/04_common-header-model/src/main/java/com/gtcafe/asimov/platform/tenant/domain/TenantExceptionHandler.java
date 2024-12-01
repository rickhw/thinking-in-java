package com.gtcafe.asimov.platform.tenant.domain;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.gtcafe.asimov.common.ErrorResponse;
import com.gtcafe.asimov.platform.tenant.domain.exception.TenantException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class TenantExceptionHandler {

    @ExceptionHandler(TenantException.class)
    public ResponseEntity<ErrorResponse> handleTenantException(TenantException ex, WebRequest request) {
        log.error("Tenant not found", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                // .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}
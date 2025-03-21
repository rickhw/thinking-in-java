package com.gtcafe.asimov.platform.tenant.domain;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.gtcafe.asimov.platform.tenant.domain.exception.TenantException;
import com.gtcafe.asimov.system.exception.ExceptionModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class TenantExceptionHandler {

    @ExceptionHandler(TenantException.class)
    public ResponseEntity<ExceptionModel> handleTenantException(TenantException ex, WebRequest request) {
        // log.error("handleTenantException thrown by TenantService", ex);
        log.error("exceptions thrown by TenantService, errorNo: {}", ex.getErrorNo());

        // ex.getErrorNo()
        
        ExceptionModel error = ExceptionModel.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                // .error("Not Found")
                .errorCode("TenantException")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
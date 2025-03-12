package com.gtcafe.asimov.region.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConcurrentOperationException extends RuntimeException {
    public ConcurrentOperationException(String message) {
        super(message);
    }
}
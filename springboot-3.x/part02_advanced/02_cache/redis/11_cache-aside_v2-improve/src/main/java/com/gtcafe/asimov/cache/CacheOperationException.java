package com.gtcafe.asimov.cache;


public class CacheOperationException extends RuntimeException {
    public CacheOperationException(String message) {
        super(message);
    }

    public CacheOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
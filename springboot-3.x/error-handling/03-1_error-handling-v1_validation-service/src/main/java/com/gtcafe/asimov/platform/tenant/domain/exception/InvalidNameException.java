package com.gtcafe.asimov.platform.tenant.domain.exception;

public class InvalidNameException extends BaseException {
    public InvalidNameException(String message) {
        super("INVALID_NAME", message);
    }
}
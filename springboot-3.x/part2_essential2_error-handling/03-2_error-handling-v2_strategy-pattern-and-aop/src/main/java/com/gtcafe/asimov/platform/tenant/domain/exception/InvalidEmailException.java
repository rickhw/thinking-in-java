package com.gtcafe.asimov.platform.tenant.domain.exception;

import com.gtcafe.asimov.system.exception.BaseException;

public class InvalidEmailException extends BaseException {
    public InvalidEmailException(String message) {
        super("INVALID_EMAIL", message);
    }
}
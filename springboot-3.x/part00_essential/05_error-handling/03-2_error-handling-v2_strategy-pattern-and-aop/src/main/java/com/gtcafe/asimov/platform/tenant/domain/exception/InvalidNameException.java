package com.gtcafe.asimov.platform.tenant.domain.exception;

import com.gtcafe.asimov.system.exception.BaseException;

public class InvalidNameException extends BaseException {
    public InvalidNameException(String message) {
        super("INVALID_NAME", message);
    }
}
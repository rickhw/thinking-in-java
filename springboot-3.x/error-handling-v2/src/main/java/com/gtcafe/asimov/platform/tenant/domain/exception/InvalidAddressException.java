package com.gtcafe.asimov.platform.tenant.domain.exception;

import com.gtcafe.asimov.system.exception.BaseException;

public class InvalidAddressException extends BaseException {
    public InvalidAddressException(String message) {
        super("INVALID_ADDR", message);
    }
}
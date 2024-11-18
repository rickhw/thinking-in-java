package com.gtcafe.asimov.platform.tenant.domain.exception;

import com.gtcafe.asimov.system.exception.BaseException;

public class InvalidPlanException extends BaseException {
    public InvalidPlanException(String message) {
        super("INVALID_PLAN", message);
    }
}
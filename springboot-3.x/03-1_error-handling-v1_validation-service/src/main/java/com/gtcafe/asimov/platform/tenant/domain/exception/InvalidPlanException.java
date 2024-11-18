package com.gtcafe.asimov.platform.tenant.domain.exception;

public class InvalidPlanException extends BaseException {
    public InvalidPlanException(String message) {
        super("INVALID_PLAN", message);
    }
}
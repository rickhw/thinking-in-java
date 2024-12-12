package com.gtcafe.asimov.platform.tenant.domain.exception;

public class QuotaExceededException extends BaseException {
    public QuotaExceededException(String message) {
        super("QUOTA_EXCEEDED", message);
    }
}
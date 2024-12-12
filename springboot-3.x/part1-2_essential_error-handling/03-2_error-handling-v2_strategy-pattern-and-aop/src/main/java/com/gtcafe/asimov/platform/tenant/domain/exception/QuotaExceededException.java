package com.gtcafe.asimov.platform.tenant.domain.exception;

import com.gtcafe.asimov.system.exception.BaseException;

public class QuotaExceededException extends BaseException {
    public QuotaExceededException(String message) {
        super("QUOTA_EXCEEDED", message);
    }
}
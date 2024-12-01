package com.gtcafe.asimov.platform.tenant.domain.exception;

public class TenantException extends RuntimeException {
    public TenantException(String errorNo) {
        super("Tenant not found with id: " + errorNo);
    }
}

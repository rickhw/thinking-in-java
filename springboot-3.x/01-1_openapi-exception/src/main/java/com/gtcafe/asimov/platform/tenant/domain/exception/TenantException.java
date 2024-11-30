package com.gtcafe.asimov.platform.tenant.domain.exception;

public class TenantException extends RuntimeException {
    public TenantException(String id) {
        super("Tenant not found with id: " + id);
    }
}

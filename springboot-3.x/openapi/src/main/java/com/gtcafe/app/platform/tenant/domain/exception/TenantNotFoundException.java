package com.gtcafe.app.platform.tenant.domain.exception;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String id) {
        super("Tenant not found with id: " + id);
    }
}

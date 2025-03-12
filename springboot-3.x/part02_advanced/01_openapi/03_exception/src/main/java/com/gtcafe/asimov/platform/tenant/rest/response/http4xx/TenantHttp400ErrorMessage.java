package com.gtcafe.asimov.platform.tenant.rest.response.http4xx;

public enum TenantHttp400ErrorMessage {
    INVALID_TENANT_NAME("Tenant name is invalid"),
    INVALID_EMAIL("Email is invalid")
    ;

    private String message;

    private TenantHttp400ErrorMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return message;
    }
}
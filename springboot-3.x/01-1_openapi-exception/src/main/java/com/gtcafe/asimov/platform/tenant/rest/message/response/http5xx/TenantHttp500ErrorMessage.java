package com.gtcafe.asimov.platform.tenant.rest.message.response.http5xx;

public enum TenantHttp500ErrorMessage {
    FAILED_TO_CREATE_TENANT("Failed to create tenant")
    
    ;

    private String message;

    TenantHttp500ErrorMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return message;
    }
}



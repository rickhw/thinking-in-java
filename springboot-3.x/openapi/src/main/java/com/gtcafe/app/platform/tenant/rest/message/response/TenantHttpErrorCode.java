package com.gtcafe.app.platform.tenant.rest.message.response;

public enum TenantHttpErrorCode{
    UNAUTHORIZED("401", "Unauthorized"),
    TOO_MANY_REQUEST("429", "TooManyRequest")
    ;

    private final String errorCode;
    private final String errorMessage;

    TenantHttpErrorCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
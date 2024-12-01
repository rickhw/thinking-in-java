package com.gtcafe.asimov.platform.tenant.domain.exception;

import lombok.Getter;

public class TenantException extends RuntimeException {
    @Getter
    private String errorNo;
    
    public TenantException(String errorNo) {
        super("Tenant exception: " + errorNo);
        this.errorNo = errorNo;
    }

}

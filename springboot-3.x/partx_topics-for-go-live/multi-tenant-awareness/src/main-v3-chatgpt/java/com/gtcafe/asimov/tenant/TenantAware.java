package com.gtcafe.asimov.tenant;

public interface TenantAware {
    void setTenantId(String tenantId);
    String getTenantId();
}
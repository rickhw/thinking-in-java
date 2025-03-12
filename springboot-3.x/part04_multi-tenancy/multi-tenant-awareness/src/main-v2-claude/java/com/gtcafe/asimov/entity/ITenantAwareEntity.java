package com.gtcafe.asimov.entity;

public interface ITenantAwareEntity {
    String getTenantId();
    void setTenantId(String tenantId);
}
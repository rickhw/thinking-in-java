package com.gtcafe.asimov.tenant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class TenantAwareEntityListener {
    @PrePersist
    @PreUpdate
    public void setTenant(Object entity) {
        if (entity instanceof TenantAware) {
            ((TenantAware) entity).setTenantId(TenantContext.getTenantId());
        }
    }
}
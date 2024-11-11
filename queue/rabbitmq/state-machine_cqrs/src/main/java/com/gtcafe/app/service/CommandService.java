// CommandService.java
package com.gtcafe.app.service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.enums.TenantStatus;
import com.gtcafe.app.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class CommandService {
    private final TenantRepository tenantRepository;

    public CommandService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void updateTenantStatus(String tenantId, TenantStatus status) {
        Tenant tenant = tenantRepository.findById(tenantId);
        tenant.setStatus(status);
        tenantRepository.save(tenant);
    }
}

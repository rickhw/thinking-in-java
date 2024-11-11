// QueryService.java
package com.gtcafe.app.service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    private final TenantRepository tenantRepository;

    public QueryService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }
}

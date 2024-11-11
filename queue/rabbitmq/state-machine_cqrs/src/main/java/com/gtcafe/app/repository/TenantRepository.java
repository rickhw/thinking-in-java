// TenantRepository.java
package com.gtcafe.app.repository;

import com.gtcafe.app.domain.Tenant;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TenantRepository {
    private final Map<String, Tenant> tenantStore = new ConcurrentHashMap<>();

    public Tenant findById(String id) {
        return tenantStore.get(id);
    }

    public void save(Tenant tenant) {
        tenantStore.put(tenant.getId(), tenant);
    }
}

package com.gtcafe.asimov.platform.tenant.domain;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.tenant.domain.exception.TenantException;
import com.gtcafe.asimov.platform.tenant.domain.model.Tenant;
import com.gtcafe.asimov.platform.tenant.domain.model.TenantState;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;

@Service
public class TenantService {
    
    // @Autowired
    // private TenantRepository tenantRepository;

    private HashMap<String, Tenant> tenants = new HashMap<>();

    public Tenant createTenantAsync(CreateTenantRequest tenant) {

        // tenants.put(tenant, null)
        throw new NotImplementedException();

        // return tenantRepository.findAll();


    }


    public List<Tenant> getTenants() {
        throw new NotImplementedException();

        // return tenantRepository.findAll();
    }

    public Tenant getTenantById(String id) {
        // 1. find from repository


        // if (tenant == null) {
            throw new TenantException(id);
        // }
        // throw new NotImplementedException();
    }

    public Tenant changeTanantStatus(String id, TenantState toState) {
        // 1. checkout transit toState is valid


        // if (tenant == null) {
            // throw new TenantNotFoundException(id);
        // }
        throw new NotImplementedException();
    }

}

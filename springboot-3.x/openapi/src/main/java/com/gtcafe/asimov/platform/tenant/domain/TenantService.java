package com.gtcafe.asimov.platform.tenant.domain;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.tenant.domain.model.Tenant;

@Service
public class TenantService {
    
    // @Autowired
    // private TenantRepository tenantRepository;

    public List<Tenant> getTenants() {
        throw new NotImplementedException();

        // return tenantRepository.findAll();
    }

    public Tenant getTenantById(String id) {
        throw new NotImplementedException();

    }
}

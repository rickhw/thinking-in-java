package com.gtcafe.asimov.platform.tenant.domain;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.gtcafe.asimov.platform.tenant.domain.exception.InvalidNameException;
import com.gtcafe.asimov.platform.tenant.domain.exception.InvalidPlanException;
import com.gtcafe.asimov.platform.tenant.domain.exception.QuotaExceededException;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;

@Service
@Validated
public class TenantValidationService {
    private static final Set<String> VALID_PLANS = Set.of("a", "b", "c");
    
    public void validatePayload(CreateTenantRequest request) {
        // Additional custom validation beyond @Pattern
        if (!request.getName().matches("[a-z0-9]+")) {
            throw new InvalidNameException("Name contains invalid characters");
        }
    }
    
    public void validateQuota(String tenantId) {
        // Example quota check logic
        long currentTenantCount = getCurrentTenantCount();
        if (currentTenantCount >= 1000) {
            throw new QuotaExceededException("Maximum number of tenants reached");
        }
    }
    
    public void validateAssociations(CreateTenantRequest request) {
        if (!VALID_PLANS.contains(request.getPlan())) {
            throw new InvalidPlanException("Invalid plan selected. Valid plans are: " + 
                String.join(", ", VALID_PLANS));
        }
    }
    
    private long getCurrentTenantCount() {
        // Implementation to get current tenant count
        return 0;
    }
}
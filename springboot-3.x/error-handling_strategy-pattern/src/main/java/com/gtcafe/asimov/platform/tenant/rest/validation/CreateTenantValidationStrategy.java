package com.gtcafe.asimov.platform.tenant.rest.validation;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.gtcafe.asimov.platform.tenant.domain.exception.InvalidNameException;
import com.gtcafe.asimov.platform.tenant.domain.exception.InvalidPlanException;
import com.gtcafe.asimov.platform.tenant.domain.exception.QuotaExceededException;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;
import com.gtcafe.asimov.system.validation.ValidationStrategy;

@Component
public class CreateTenantValidationStrategy implements ValidationStrategy<CreateTenantRequest> {
    
    private static final Set<String> VALID_PLANS = Set.of("a", "b", "c");
    
    @Override
    public void validatePayload(CreateTenantRequest request) {
        if (!request.getName().matches("[a-z0-9]+")) {
            throw new InvalidNameException("Name contains invalid characters");
        }
    }
    
    @Override
    public void validateQuota(CreateTenantRequest request) {
        long currentTenantCount = getCurrentTenantCount();
        if (currentTenantCount >= 1000) {
            throw new QuotaExceededException("Maximum number of tenants reached");
        }
    }
    
    @Override
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
package com.gtcafe.asimov.platform.tenant.rest.validation;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.gtcafe.asimov.platform.tenant.domain.exception.InvalidAddressException;
import com.gtcafe.asimov.platform.tenant.domain.exception.InvalidEmailException;
import com.gtcafe.asimov.platform.tenant.domain.exception.QuotaExceededException;
import com.gtcafe.asimov.platform.tenant.rest.message.request.UpdateTenantAttributeRequest;
import com.gtcafe.asimov.system.validation.IValidationStrategy;

@Component
public class UpdateTenantAttributeValidationStrategy implements IValidationStrategy<UpdateTenantAttributeRequest> {
    
    private static final Set<String> VALID_PLANS = Set.of("a", "b", "c");
    
    @Override
    public void validatePayload(UpdateTenantAttributeRequest request) {
        if (!request.getEmail().matches("[a-z0-9]+")) {
            throw new InvalidEmailException("Email contains invalid characters");
        }
    }
    
    @Override
    public void validateQuota(UpdateTenantAttributeRequest request) {
        long currentTenantCount = getCurrentTenantCount();
        if (currentTenantCount >= 1000) {
            throw new QuotaExceededException("Maximum number of tenants reached");
        }
    }
    
    @Override
    public void validateAssociations(UpdateTenantAttributeRequest request) {
        if (!VALID_PLANS.contains(request.getAddress())) {
            throw new InvalidAddressException("Invalid address selected");
        }
    }

    private long getCurrentTenantCount() {
        // Implementation to get current tenant count
        return 0;
    }
}
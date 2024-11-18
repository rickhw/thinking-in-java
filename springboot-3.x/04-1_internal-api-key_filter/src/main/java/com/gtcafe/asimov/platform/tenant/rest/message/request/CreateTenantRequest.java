package com.gtcafe.asimov.platform.tenant.rest.message.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateTenantRequest {
    @Pattern(regexp = "[a-z0-9]+", message = "Name must contain only lowercase letters and numbers")
    private String name;
    
    @NotNull(message = "Plan is required")
    private String plan;
}
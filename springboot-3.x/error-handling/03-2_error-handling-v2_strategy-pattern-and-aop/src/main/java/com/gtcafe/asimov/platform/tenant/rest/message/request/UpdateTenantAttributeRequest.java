package com.gtcafe.asimov.platform.tenant.rest.message.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateTenantAttributeRequest {
    @Pattern(regexp = "[a-z0-9]+", message = "Name contains invalid characters")
    private String email;
    
    @NotNull(message = "Address is required")
    private String address;
}
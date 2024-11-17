package com.gtcafe.app.platform.tenant.rest.message.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantAttributeRequest {
    
    @Schema(
        description = "Description of the tenant",
        example = "The Legend of Zelda: Breath of the Wild is an action-adventure game developed and published by Nintendo.",
        nullable = false
    )
    private String description;
}
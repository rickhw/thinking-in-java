package com.gtcafe.asimov.platform.tenant.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
public class TenantSpec {

    @Getter @Setter
    @Schema(
        description = "Description of the tenant",
        example = "the tenant is a game",
        nullable = false
    )
    private String description;

}
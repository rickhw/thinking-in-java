package com.gtcafe.asimov.platform.tenant.rest.message.response;

import com.gtcafe.asimov.platform.tenant.domain.model.TenantKind;
import com.gtcafe.asimov.platform.tenant.domain.model.TenantMetadata;
import com.gtcafe.asimov.platform.tenant.domain.model.TenantSpec;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveTenantResponse {

    @Schema(
        description = "the kind of the tenant",
        example = "platform.Tenant",
        defaultValue = TenantKind.NAME
    )
    private String kind;

    @Schema(
        description = "the unique id of the tenant",
        example = "123e4567-e89b-12d3-a456-426614174000",
        pattern = "UUID"
    )
    private String id;


    @Schema(
        description = "the metadata of the tenant. write-once, read-many by user; write-many, read-many by system"
    )
    private TenantMetadata metadata;

    @Schema(
        description = "the spec of the tenant. write and read by user"
    )
    private TenantSpec spec;

}
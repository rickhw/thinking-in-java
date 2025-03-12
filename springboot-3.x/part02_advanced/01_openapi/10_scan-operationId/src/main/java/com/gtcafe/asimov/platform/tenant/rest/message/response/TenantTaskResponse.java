package com.gtcafe.asimov.platform.tenant.rest.message.response;

import com.gtcafe.asimov.platform.task.rest.TaskResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantTaskResponse extends TaskResponse<RetrieveTenantResponse> {
    
    @Schema(
        description = "the data of the tenant"
    )
    private RetrieveTenantResponse data;
}

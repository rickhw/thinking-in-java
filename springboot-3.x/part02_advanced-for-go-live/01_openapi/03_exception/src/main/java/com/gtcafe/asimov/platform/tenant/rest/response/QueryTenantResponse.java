package com.gtcafe.asimov.platform.tenant.rest.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTenantResponse {

    @Schema(
        description = "the data set of tenants"
    )
    private List<RetrieveTenantResponse> dataSet;


    @Schema(
        description = "condition of the query"
    )
    private QueryCondition condition;

}
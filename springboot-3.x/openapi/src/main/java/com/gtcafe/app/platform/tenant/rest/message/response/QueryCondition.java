package com.gtcafe.app.platform.tenant.rest.message.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryCondition {
    
    @Schema(
        description = "the previous page number",
        example = "1"
    )
    String previousPage;

    @Schema(
        description = "the next page number",
        example = "1"
    )
    String nextPage;    
}
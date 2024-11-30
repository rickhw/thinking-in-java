package com.gtcafe.asimov.platform.tenant.rest.message.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(
        description = "the unique name of the tenant",
        example = "Breath-of-the-Wild",
        nullable = false,
        pattern = "^[a-zA-Z0-9-]+$"
    )
    private String name;
    
    @NotBlank(message = "Email is required")
    @Schema(
        description = "the root email of the tenant",
        example = "rick@abc.com",
        nullable = false,
        pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    private String email;
    
    @Schema(
        description = "Description of the tenant",
        example = "The Legend of Zelda: Breath of the Wild is an action-adventure game developed and published by Nintendo.",
        nullable = false
    )
    private String description;
}
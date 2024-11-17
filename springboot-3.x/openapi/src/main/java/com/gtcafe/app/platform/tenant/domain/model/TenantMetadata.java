package com.gtcafe.app.platform.tenant.domain.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
public class 
TenantMetadata {
    
    @Getter @Setter
    @Schema(
        description = "the unique name of the tenant",
        example = "Breath-of-the-Wild",
        nullable = false,
        pattern = "^[a-zA-Z0-9-]+$"
    )
    private String name;
    
    @Getter @Setter
    @Schema(
        description = "the root email of the tenant",
        example = "rick@abc.com",
        nullable = false,
        pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    private String email;
    
    @Getter @Setter
    @Schema(
        description = "the state of the tenant",
        nullable = false
    )
    private TenantState state;

    @Getter @Setter
    @Schema(
        description = "the start time of the resource",
        example = "2021-08-01T00:00:00",
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime startTime;

    @Getter @Setter
    @Schema(
        description = "the last modified time of the resource",
        example = "2021-08-01T00:00:00",
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime lastModified;

    public TenantMetadata() {
        this.state = TenantState.pending;
        this.startTime = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

}
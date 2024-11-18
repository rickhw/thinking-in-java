package com.gtcafe.asimov.platform.tenant.domain.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
public class Tenant {

    @Getter @Setter
    private String id;
    
    public Tenant() {
        // this.kind = TenantKind.NAME;
        this.id = UUID.randomUUID().toString();
    }
}
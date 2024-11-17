package com.gtcafe.app.platform.tenant.domain.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
public class Tenant {

    @Getter
    public String kind;

    @Getter @Setter
    private String id;
    
    @Getter @Setter
    private TenantMetadata metadata;

    @Getter @Setter
    private TenantSpec spec;
    
    public Tenant() {
        this.kind = TenantKind.NAME;
        this.id = UUID.randomUUID().toString();
        this.metadata = TenantMetadata.builder().build();
        this.spec = TenantSpec.builder().build();
    }
}
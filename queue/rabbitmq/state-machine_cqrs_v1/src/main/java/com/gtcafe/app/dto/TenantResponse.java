package com.gtcafe.app.dto;

import com.gtcafe.app.domain.Tenant;
import lombok.Data;

@Data
public class TenantResponse {
    private Long id;
    private String name;
    private Tenant.TenantState state;
    
    public static TenantResponse from(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.setId(tenant.getId());
        response.setName(tenant.getName());
        response.setState(tenant.getState());
        return response;
    }
}
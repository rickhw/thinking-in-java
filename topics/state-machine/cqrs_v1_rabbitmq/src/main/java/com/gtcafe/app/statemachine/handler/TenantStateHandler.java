package com.gtcafe.app.statemachine.handler;

import org.springframework.stereotype.Component;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantStateHandler {
    private final TenantRepository tenantRepository;
    
    public void handleIniting(Tenant tenant) {
        log.info("tenant: [{}], state: initing", tenant);
        tenant.setState(Tenant.TenantState.ACTIVE);
        tenantRepository.save(tenant);
    }
    
    public void handleActive(Tenant tenant) {
        // Handle active state logic
        tenantRepository.save(tenant);
    }
    
    public void handleInactive(Tenant tenant) {
        // Handle inactive state logic
        tenantRepository.save(tenant);
    }
    
    public void handleTerminated(Tenant tenant) {
        // Handle terminated state logic
        tenantRepository.save(tenant);
    }
}
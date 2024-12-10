package com.gtcafe.app.service;

import org.springframework.stereotype.Service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantQueryService {
    private final TenantRepository tenantRepository;
    
    public Tenant getTenant(Long id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }
}
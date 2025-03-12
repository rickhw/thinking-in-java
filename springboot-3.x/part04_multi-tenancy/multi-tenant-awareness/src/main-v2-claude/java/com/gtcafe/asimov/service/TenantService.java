package com.gtcafe.asimov.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    
    public String getCurrentTenantId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
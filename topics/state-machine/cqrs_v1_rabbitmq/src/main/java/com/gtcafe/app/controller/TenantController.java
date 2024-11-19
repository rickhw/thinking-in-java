package com.gtcafe.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.dto.TenantCreateRequest;
import com.gtcafe.app.dto.TenantResponse;
import com.gtcafe.app.service.TenantCommandService;
import com.gtcafe.app.service.TenantQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantCommandService commandService;
    private final TenantQueryService queryService;
    
    @PostMapping
    public TenantResponse createTenant(@RequestBody TenantCreateRequest request) {
        return TenantResponse.from(commandService.createTenant(request));
    }
    
    @GetMapping("/{id}")
    public TenantResponse getTenant(@PathVariable Long id) {
        return TenantResponse.from(queryService.getTenant(id));
    }
    
    @PutMapping("/{id}/state")
    public void updateState(@PathVariable Long id, @RequestParam Tenant.TenantState state) {
        commandService.updateState(id, state);
    }
}
package com.gtcafe.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.app.commands.CreateTenantCommand;
import com.gtcafe.app.commands.UpdateTenantStatusCommand;
import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.services.TenantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {
    
    private final TenantService tenantService;
    
    @PostMapping
    public ResponseEntity<Tenant> createTenant(
            @RequestBody CreateTenantCommand command
    ) {
        log.info("Request: [{}]", command);
        return ResponseEntity.ok(tenantService.createTenant(command));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenant(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenant(id));
    }
    
    @PutMapping("/{id}/{state}")
    public ResponseEntity<Tenant> updateTenantStatus(
            @PathVariable Long id,
            @PathVariable String state) {
        
        return ResponseEntity.ok(tenantService.updateTenantStatus(new UpdateTenantStatusCommand(id, state)));
    }
}
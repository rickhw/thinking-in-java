package com.gtcafe.asimov.platform.tenant.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.request.UpdateTenantAttributeRequest;
import com.gtcafe.asimov.system.context.TenantContext;
import com.gtcafe.asimov.system.validation.ValidateRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @PostMapping(
        consumes = { MediaType.APPLICATION_JSON_VALUE }, 
        produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    @Validated
    @ValidateRequest
    public ResponseEntity<String> create(@RequestBody CreateTenantRequest request) {
        
        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("Service - TenantId: " + context.getTenantId());
        System.out.println("Service - AppName: " + context.getAppName());
        System.out.println("Service - RoleName: " + context.getRoleName());

        
        return ResponseEntity.ok("ok");
    }


    // ------------------------------------------------------------------------
    @GetMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> query() {

        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("Service - TenantId: " + context.getTenantId());
        System.out.println("Service - AppName: " + context.getAppName());
        System.out.println("Service - RoleName: " + context.getRoleName());

        return ResponseEntity.ok("ok");
    }

    // ------------------------------------------------------------------------
    @PutMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ValidateRequest
    public ResponseEntity<String> update(@RequestBody UpdateTenantAttributeRequest request) {

        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("Service - TenantId: " + context.getTenantId());
        System.out.println("Service - AppName: " + context.getAppName());
        System.out.println("Service - RoleName: " + context.getRoleName());

        return ResponseEntity.ok("ok");
    }
}
package com.gtcafe.asimov.platform.tenant.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.TenantContext;
import com.gtcafe.asimov.platform.tenant.domain.TenantService;
import com.gtcafe.asimov.platform.tenant.domain.TenantValidationService;
import com.gtcafe.asimov.platform.tenant.domain.model.Tenant;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.response.RetrieveTenantResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.TenantTaskResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService service;

    @Autowired
    private TenantValidationService validationService;

    @PostMapping(
        consumes = { MediaType.APPLICATION_JSON_VALUE }, 
        produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    @Validated
    public ResponseEntity<String> create(@RequestBody CreateTenantRequest request) {
        
        // 1. Payload Validation
        validationService.validatePayload(request);
        
        // 2. Quota Check
        validationService.validateQuota(request.getName());
        
        // 3. Association Check
        validationService.validateAssociations(request);

        // log.info("Created new tenant with id: {}", tenant.getId());
        return ResponseEntity.ok("ok");
    }


    // ------------------------------------------------------------------------
    @GetMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> query() {

        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("Service - TenantId: " + context.getTenantId());
        System.out.println("Service - AppName: " + context.getAppName());
        System.out.println("Service - RoleName: " + context.getRoleName());

        // QueryTenantResponse response = QueryTenantResponse.builder()
        //         // .tenants(filteredTenants)
        //         .build();

        return ResponseEntity.ok("ok");
    }

}
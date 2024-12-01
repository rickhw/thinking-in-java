package com.gtcafe.asimov.platform.tenant.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.TenantContext;
import com.gtcafe.asimov.platform.tenant.domain.TenantService;
import com.gtcafe.asimov.platform.tenant.domain.model.Tenant;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.request.UpdateTenantAttributeRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.response.RetrieveTenantResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.TenantTaskResponse;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService service;

    // @PostMapping(
    //     consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    //     produces = { MediaType.APPLICATION_JSON_VALUE }
    // )
    // public ResponseEntity<TenantTaskResponse> create(@RequestBody CreateTenantRequest request) {
        
    //     Tenant tenant = service.createTenantAsync(request);

    //     TenantTaskResponse response = new TenantTaskResponse();

    //     log.info("Created new tenant with id: {}", tenant.getId());
    //     return ResponseEntity.ok(response);
    // }


    // ------------------------------------------------------------------------
    @GetMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> query() {

        TenantContext context = TenantContext.getCurrentContext();
        System.out.println("Service - TenantId: " + context.getTenantId());
        System.out.println("Service - AppName: " + context.getAppName());
        System.out.println("Service - RoleName: " + context.getRoleName());

        return ResponseEntity.ok("ok");
    }

    // // ------------------------------------------------------------------------
    // @GetMapping(
    //     value = "/{id}", 
    //     consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    //     produces = { MediaType.APPLICATION_JSON_VALUE }
    // )
    // public ResponseEntity<RetrieveTenantResponse> retrieve(@PathVariable String id) {
    //     log.debug("Fetching tenant with id: {}", id);

    //     Tenant tenant = service.getTenantById(id);

    //     RetrieveTenantResponse response = RetrieveTenantResponse.builder()
    //             // .tenant(tenant)
    //             .build();

    //     return ResponseEntity.ok(response);
    // }


    // // ------------------------------------------------------------------------
    // @DeleteMapping(
    //     value = "/{id}", 
    //     consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    //     produces = { MediaType.APPLICATION_JSON_VALUE }
    // )
    // public ResponseEntity<TenantTaskResponse> delete( @PathVariable String id) {

    //     // Tenant tenant = service.getTenantById(id);
    //     log.info("Deleted tenant with id: {}", id);

    //     TenantTaskResponse response = new TenantTaskResponse();
    //     return ResponseEntity.ok(response);
    // }

    // // ------------------------------------------------------------------------
    // @PutMapping(
    //     value = "/{id}", 
    //     consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    //     produces = { MediaType.APPLICATION_JSON_VALUE }
    // )
    // public ResponseEntity<RetrieveTenantResponse> update(@PathVariable String id, @Valid @RequestBody UpdateTenantAttributeRequest request) {

    //     RetrieveTenantResponse tenant = RetrieveTenantResponse.builder()
    //             // .tenant(tenant)
    //             .build();

    //     log.info("Updated tenant with id: {}", id);
    //     return ResponseEntity.ok(tenant);
    // }

    // // ------------------------------------------------------------------------
    // @PatchMapping(
    //     value = "/{id}:inactive", 
    //     consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    //     produces = { MediaType.APPLICATION_JSON_VALUE }
    // )
    // public ResponseEntity<TenantTaskResponse> setInactive(@PathVariable String id) {

    //     log.info("Updated tenant with id: {}", id);

    //     TenantTaskResponse response = new TenantTaskResponse();
    //     return ResponseEntity.ok(response);
    // }

    // // ------------------------------------------------------------------------
    // @PatchMapping(
    //     value = "/{id}:active", 
    //     consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    //     produces = { MediaType.APPLICATION_JSON_VALUE }
    // )

    // public ResponseEntity<TenantTaskResponse> setActive(@PathVariable String id) {

    //     log.info("Updated tenant with id: {}", id);

    //     TenantTaskResponse response = new TenantTaskResponse();
    //     return ResponseEntity.ok(response);
    // }
}
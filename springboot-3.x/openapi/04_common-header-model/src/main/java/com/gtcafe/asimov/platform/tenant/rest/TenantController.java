package com.gtcafe.asimov.platform.tenant.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.tenant.domain.TenantService;
import com.gtcafe.asimov.platform.tenant.domain.model.Tenant;
import com.gtcafe.asimov.platform.tenant.domain.model.TenantState;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.request.UpdateTenantAttributeRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.response.QueryTenantResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.RetrieveTenantResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.TenantTaskResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tenants")
public class TenantController implements ITenantController {

    @Autowired
    private TenantService service;

    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<TenantTaskResponse> create(
        CreateTenantRequest request
    ) {

        Tenant tenant = service.createTenantAsync(request);

        TenantTaskResponse response = new TenantTaskResponse();

        log.info("Created new tenant with id: {}", tenant.getId());
        return ResponseEntity.ok(response);
    }


    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<QueryTenantResponse> query(
        TenantState state
    ) {

        log.debug("Fetching tenants with filters - state: {}", state);

        QueryTenantResponse response = QueryTenantResponse.builder()
                // .tenants(filteredTenants)
                .build();

        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<RetrieveTenantResponse> retrieve(
        String id
    ) {
        log.debug("Fetching tenant with id: {}", id);

        Tenant tenant = service.getTenantById(id);

        RetrieveTenantResponse response = RetrieveTenantResponse.builder()
                // .tenant(tenant)
                .build();

        return ResponseEntity.ok(response);
    }


    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<TenantTaskResponse> delete(
        String id
    ) {

        // Tenant tenant = service.getTenantById(id);
        log.info("Deleted tenant with id: {}", id);

        TenantTaskResponse response = new TenantTaskResponse();
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<RetrieveTenantResponse> update(
        String id,
        UpdateTenantAttributeRequest request
    ) {

        RetrieveTenantResponse tenant = RetrieveTenantResponse.builder()
                // .tenant(tenant)
                .build();

        log.info("Updated tenant with id: {}", id);
        return ResponseEntity.ok(tenant);
    }

    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<TenantTaskResponse> setInactive(
        String id
    ) {

        log.info("Updated tenant with id: {}", id);

        TenantTaskResponse response = new TenantTaskResponse();
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @Override
    public ResponseEntity<TenantTaskResponse> setActive(
        String id
    ) {

        log.info("Updated tenant with id: {}", id);

        TenantTaskResponse response = new TenantTaskResponse();
        return ResponseEntity.ok(response);
    }
}
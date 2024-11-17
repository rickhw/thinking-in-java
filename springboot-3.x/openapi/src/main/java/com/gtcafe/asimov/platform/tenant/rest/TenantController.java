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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.rest.message.error.GlobalHttp401ErrorResponse;
import com.gtcafe.asimov.platform.rest.message.error.GlobalHttp429ErrorResponse;
import com.gtcafe.asimov.platform.rest.message.error.GlobalHttp500ErrorResponse;
import com.gtcafe.asimov.platform.tenant.domain.TenantService;
import com.gtcafe.asimov.platform.tenant.domain.exception.TenantNotFoundException;
import com.gtcafe.asimov.platform.tenant.domain.model.Tenant;
import com.gtcafe.asimov.platform.tenant.domain.model.TenantState;
import com.gtcafe.asimov.platform.tenant.rest.message.request.CreateTenantRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.request.UpdateTenantAttributeRequest;
import com.gtcafe.asimov.platform.tenant.rest.message.response.QueryTenantResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.RetrieveTenantResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.TenantTaskResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.error.TenantHttp400ErrorResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.error.TenantHttp404ErrorResponse;
import com.gtcafe.asimov.platform.tenant.rest.message.response.error.TenantHttp500ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenant", description = "Tenant management APIs")
public class TenantController {

    @Autowired
    private TenantService service;

    // ------------------------------------------------------------------------
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[ASYNC] Create new tenant", description = "Create a new tenant with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted the task of created tenant", content = @Content(schema = @Schema(implementation = TenantTaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided", content = @Content(schema = @Schema(implementation = TenantHttp400ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Tenant request body", required = true, content = @Content(schema = @Schema(implementation = CreateTenantRequest.class)))
    public ResponseEntity<TenantTaskResponse> create(
            @Parameter(description = "Tenant details", required = true) @Valid @RequestBody CreateTenantRequest request) {

        Tenant tenant = new Tenant();
        TenantTaskResponse response = new TenantTaskResponse();

        log.info("Created new tenant with id: {}", tenant.getId());
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @GetMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[SYNC] Query all tenants", description = "Retrieve all tenants with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tenants", content = @Content(schema = @Schema(implementation = QueryTenantResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    public ResponseEntity<QueryTenantResponse> query(
            @Parameter(description = "Filter tenants by active status", schema = @Schema(implementation = TenantState.class)) @RequestParam(required = false, defaultValue = "ACTIVE") TenantState state) {

        log.debug("Fetching tenants with filters - state: {}", state);

        QueryTenantResponse response = QueryTenantResponse.builder()
                // .tenants(filteredTenants)
                .build();

        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @GetMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[SYNC] Retrieve tenant by ID", description = "Retrieve a specific tenant by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tenant", content = @Content(schema = @Schema(implementation = RetrieveTenantResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(implementation = TenantHttp404ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    public ResponseEntity<RetrieveTenantResponse> retrieve(
            @Parameter(description = "Tenant ID", required = true) @PathVariable String id) {
        log.debug("Fetching tenant with id: {}", id);

        Tenant tenant = service.getTenantById(id);
        if (tenant == null) {
            throw new TenantNotFoundException(id);
        }

        RetrieveTenantResponse response = RetrieveTenantResponse.builder()
                // .tenant(tenant)
                .build();

        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @DeleteMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[ASYNC] Delete tenant", description = "Delete a tenant by its ID, the state has to set as inactive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accept the command of the deleting tenant", content = @Content(schema = @Schema(implementation = TenantTaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(implementation = TenantHttp404ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    public ResponseEntity<TenantTaskResponse> delete(
            @Parameter(description = "Tenant ID", required = true) @PathVariable String id) {

        // Tenant tenant = service.getTenantById(id);
        log.info("Deleted tenant with id: {}", id);

        TenantTaskResponse response = new TenantTaskResponse();
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @PutMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[SYNC] Update the attributes of tenant", description = "Update an existing tenant's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated tenant", content = @Content(schema = @Schema(implementation = RetrieveTenantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided", content = @Content(schema = @Schema(implementation = TenantHttp400ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(implementation = TenantHttp404ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Update tenant request body", required = true, content = @Content(schema = @Schema(implementation = UpdateTenantAttributeRequest.class)))
    public ResponseEntity<RetrieveTenantResponse> update(
            @Parameter(description = "Tenant ID", required = true) @PathVariable String id,
            @Parameter(description = "Updated tenant details", required = true) @Valid @RequestBody UpdateTenantAttributeRequest request) {
        // if (!tenants.containsKey(id)) {
        // throw new TenantNotFoundException(id);
        // }
        // tenant.setId(id);
        // tenants.put(id, tenant);

        RetrieveTenantResponse tenant = RetrieveTenantResponse.builder()
                // .tenant(tenant)
                .build();

        log.info("Updated tenant with id: {}", id);
        return ResponseEntity.ok(tenant);
    }

    // ------------------------------------------------------------------------
    @PatchMapping(value = "/{id}:inactive", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[ASYNC] Make the tenant as inactive", description = "Set the tenant to inactive state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accept the command of inactive tenant", content = @Content(schema = @Schema(implementation = TenantTaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided", content = @Content(schema = @Schema(implementation = TenantHttp400ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(implementation = TenantHttp404ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    public ResponseEntity<TenantTaskResponse> setInactive(
            @Parameter(description = "Tenant ID", required = true) @PathVariable String id) {

        // if (!tenants.containsKey(id)) {
        // throw new TenantNotFoundException(id);
        // }
        // tenant.setId(id);
        // tenants.put(id, tenant);

        log.info("Updated tenant with id: {}", id);

        TenantTaskResponse response = new TenantTaskResponse();
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    @PatchMapping(value = "/{id}:active", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "[ASYNC] Make the tenant as active", description = "Set the tenant to active state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accept the command of active tenant", content = @Content(schema = @Schema(implementation = TenantTaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided", content = @Content(schema = @Schema(implementation = TenantHttp400ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(implementation = TenantHttp404ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = GlobalHttp401ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GlobalHttp429ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(oneOf = { TenantHttp500ErrorResponse.class, GlobalHttp500ErrorResponse.class })))
    })
    public ResponseEntity<TenantTaskResponse> setActive(
            @Parameter(description = "Tenant ID", required = true) @PathVariable String id) {

        // if (!tenants.containsKey(id)) {
        // throw new TenantNotFoundException(id);
        // }
        // tenant.setId(id);
        // tenants.put(id, tenant);

        log.info("Updated tenant with id: {}", id);

        TenantTaskResponse response = new TenantTaskResponse();
        return ResponseEntity.ok(response);
    }
}
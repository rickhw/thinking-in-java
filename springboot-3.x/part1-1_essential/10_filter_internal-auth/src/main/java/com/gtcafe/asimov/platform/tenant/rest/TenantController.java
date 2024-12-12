package com.gtcafe.asimov.platform.tenant.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> create(@RequestBody String request) {
        
        
        return ResponseEntity.ok("ok");
    }


    // ------------------------------------------------------------------------
    @GetMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> query() {

        return ResponseEntity.ok("ok");
    }

    // // ------------------------------------------------------------------------
    // @PutMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    // public ResponseEntity<String> update(@RequestBody UpdateTenantAttributeRequest request) {

    //     return ResponseEntity.ok("ok");
    // }
}
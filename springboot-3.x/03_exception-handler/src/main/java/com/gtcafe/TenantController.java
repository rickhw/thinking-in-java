package com.gtcafe;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private List<TenantDTO> tenants = new ArrayList<>();
    private Long idCounter = 1L;

    // 模擬簡單的授權檢查
    private void checkAuthorization() {
        // 這裡你可以加入實際的授權邏輯
        boolean isAuthorized = Math.random() > 0.2; // 80% 成功授權
        if (!isAuthorized) {
            throw new UnauthorizedException("Unauthorized access");
        }
    }

    @PostMapping
    public ResponseEntity<TenantDTO> createTenant(@Valid @RequestBody TenantDTO tenantDTO) {
        // checkAuthorization();
        System.out.println("ok");
        TenantDTO newTenant = new TenantDTO(
            idCounter++, 
            tenantDTO.name(), 
            tenantDTO.email(), 
            tenantDTO.description()
        );
        tenants.add(newTenant);
        return ResponseEntity.ok(newTenant);
    }

    @GetMapping
    public ResponseEntity<List<TenantDTO>> getAllTenants() {
        // checkAuthorization();
        System.out.println("query");
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantDTO> getTenantById(@PathVariable Long id) {
        checkAuthorization();
        Optional<TenantDTO> tenant = tenants.stream()
            .filter(t -> t.id().equals(id))
            .findFirst();
        
        return tenant.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantDTO> updateTenant(
        @PathVariable Long id, 
        @Valid @RequestBody TenantDTO tenantDTO
    ) {
        checkAuthorization();
        for (int i = 0; i < tenants.size(); i++) {
            if (tenants.get(i).id().equals(id)) {
                TenantDTO updatedTenant = new TenantDTO(
                    id, 
                    tenantDTO.name(), 
                    tenantDTO.email(), 
                    tenantDTO.description()
                );
                tenants.set(i, updatedTenant);
                return ResponseEntity.ok(updatedTenant);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        checkAuthorization();
        boolean removed = tenants.removeIf(tenant -> tenant.id().equals(id));
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
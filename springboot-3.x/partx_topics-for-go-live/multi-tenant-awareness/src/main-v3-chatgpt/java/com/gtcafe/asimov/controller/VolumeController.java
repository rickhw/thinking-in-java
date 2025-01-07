package com.gtcafe.asimov.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.service.VolumeService;
import com.gtcafe.asimov.tenant.TenantContext;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/volumes")
@RequiredArgsConstructor
public class VolumeController {
    private final VolumeService service;

    @GetMapping
    public List<Volume> getAllVolumes(@RequestHeader("Authorization") String token) {
        Claims claims = parseToken(token);
        boolean awareness = Boolean.parseBoolean(claims.get("awareness").toString());
        String tenantId = (String) claims.get("tenantId");
        return service.getAllVolumes(tenantId, awareness);
    }

    @PostMapping
    public Volume createVolume(@RequestHeader("Authorization") String token, @RequestBody Volume volume) {
        Claims claims = parseToken(token);
        TenantContext.setTenantId((String) claims.get("tenantId"));
        return service.createVolume(volume);
    }

    @PutMapping("/{id}")
    public Volume updateVolume(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Volume volume) {
        Claims claims = parseToken(token);
        TenantContext.setTenantId((String) claims.get("tenantId"));
        return service.updateVolume(id, volume);
    }

    @DeleteMapping("/{id}")
    public void deleteVolume(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Claims claims = parseToken(token);
        TenantContext.setTenantId((String) claims.get("tenantId"));
        service.deleteVolume(id);
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey("secret")
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }
}
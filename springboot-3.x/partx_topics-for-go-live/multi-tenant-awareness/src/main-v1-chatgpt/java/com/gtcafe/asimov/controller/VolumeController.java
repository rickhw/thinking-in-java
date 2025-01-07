package com.gtcafe.asimov.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.dto.VolumeRequest;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.service.VolumeService;

@RestController
@RequestMapping("/api/volumes")
public class VolumeController {
    private final VolumeService volumeService;

    public VolumeController(VolumeService volumeService) {
        this.volumeService = volumeService;
    }

    @GetMapping
    public List<Volume> getAllVolumes() {
        String tenantId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return volumeService.findAll(tenantId);
    }

    @PostMapping
    public Volume createVolume(@RequestBody VolumeRequest request) {
        String tenantId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return volumeService.create(tenantId, request);
    }
}

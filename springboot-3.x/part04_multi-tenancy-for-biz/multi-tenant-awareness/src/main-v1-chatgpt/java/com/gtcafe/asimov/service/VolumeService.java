package com.gtcafe.asimov.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.dto.VolumeRequest;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;

@Service
public class VolumeService {
    private final VolumeRepository volumeRepository;

    public VolumeService(VolumeRepository volumeRepository) {
        this.volumeRepository = volumeRepository;
    }

    public List<Volume> findAll(String tenantId) {
        return volumeRepository.findByTenantId(tenantId);
    }

    public Volume create(String tenantId, VolumeRequest request) {
        Volume volume = new Volume();
        volume.setTenantId(tenantId);
        volume.setName(request.getName());
        return volumeRepository.save(volume);
    }
}

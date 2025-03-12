package com.gtcafe.asimov.service;

import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolumeService {
    private final VolumeRepository repository;

    public List<Volume> getAllVolumes(String tenantId, boolean awareness) {
        return awareness ? repository.findByTenantId(tenantId) : repository.findAll();
    }

    public Volume createVolume(Volume volume) {
        return repository.save(volume);
    }

    public Volume updateVolume(Long id, Volume updatedVolume) {
        Volume volume = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Volume not found"));
        volume.setName(updatedVolume.getName());
        volume.setDescription(updatedVolume.getDescription());
        return repository.save(volume);
    }

    public void deleteVolume(Long id) {
        repository.deleteById(id);
    }
}
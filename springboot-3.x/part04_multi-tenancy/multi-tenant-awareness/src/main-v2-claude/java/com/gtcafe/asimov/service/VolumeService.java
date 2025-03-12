package com.gtcafe.asimov.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.dto.VolumeDTO;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VolumeService {

    private final VolumeRepository volumeRepository;
    private final TenantService tenantService;

    public List<VolumeDTO> getAllVolumes() {
        return volumeRepository.findAllByTenantId(tenantService.getCurrentTenantId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public VolumeDTO getVolume(Long id) {
        return volumeRepository.findByIdAndTenantId(id, tenantService.getCurrentTenantId())
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Volume not found"));
    }

    public VolumeDTO createVolume(VolumeDTO volumeDTO) {
        Volume volume = convertToEntity(volumeDTO);
        volume.setTenantId(tenantService.getCurrentTenantId());
        return convertToDTO(volumeRepository.save(volume));
    }

    public VolumeDTO updateVolume(Long id, VolumeDTO volumeDTO) {
        Volume volume = volumeRepository.findByIdAndTenantId(id, tenantService.getCurrentTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Volume not found"));
        
        volume.setName(volumeDTO.getName());
        volume.setDescription(volumeDTO.getDescription());
        volume.setSize(volumeDTO.getSize());
        
        return convertToDTO(volumeRepository.save(volume));
    }

    public void deleteVolume(Long id) {
        Volume volume = volumeRepository.findByIdAndTenantId(id, tenantService.getCurrentTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Volume not found"));
        volumeRepository.delete(volume);
    }

    private VolumeDTO convertToDTO(Volume volume) {
        VolumeDTO dto = new VolumeDTO();
        dto.setId(volume.getId());
        dto.setName(volume.getName());
        dto.setDescription(volume.getDescription());
        dto.setSize(volume.getSize());
        return dto;
    }

    private Volume convertToEntity(VolumeDTO dto) {
        Volume volume = new Volume();
        volume.setName(dto.getName());
        volume.setDescription(dto.getDescription());
        volume.setSize(dto.getSize());
        return volume;
    }
}
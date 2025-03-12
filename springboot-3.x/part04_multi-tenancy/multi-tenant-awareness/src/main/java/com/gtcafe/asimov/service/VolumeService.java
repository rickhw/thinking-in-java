package com.gtcafe.asimov.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VolumeService {
    private final VolumeRepository volumeRepository;

    public List<Volume> getAll() {
        return volumeRepository.findAll();
    }

    public Optional<Volume> getById(Long id) {
        return volumeRepository.findById(id);
    }

    public Volume save(Volume volume) {
        return volumeRepository.save(volume);
    }

    public void deleteById(Long id) {
        volumeRepository.deleteById(id);
    }
}

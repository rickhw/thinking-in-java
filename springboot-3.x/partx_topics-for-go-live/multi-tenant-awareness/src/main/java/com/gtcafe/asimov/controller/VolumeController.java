package com.gtcafe.asimov.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.dto.VolumeRequest;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.service.VolumeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/volumes")
@RequiredArgsConstructor
public class VolumeController {
    private final VolumeService volumeService;

    @GetMapping
    public ResponseEntity<List<Volume>> getAll() {
        return ResponseEntity.ok(volumeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Volume> getById(@PathVariable Long id) {
        return volumeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Volume> create(@RequestBody VolumeRequest request) {
        Volume volume = new Volume();
        volume.setName(request.getName());
        volume.setDescription(request.getDescription());
        return ResponseEntity.ok(volumeService.save(volume));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Volume> update(@PathVariable Long id, @RequestBody VolumeRequest request) {
        return volumeService.getById(id)
                .map(volume -> {
                    volume.setName(request.getName());
                    volume.setDescription(request.getDescription());
                    return ResponseEntity.ok(volumeService.save(volume));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        volumeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

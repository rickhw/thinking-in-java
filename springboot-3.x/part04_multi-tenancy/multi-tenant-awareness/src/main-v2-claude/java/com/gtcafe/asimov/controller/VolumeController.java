package com.gtcafe.asimov.controller;

import com.gtcafe.asimov.dto.VolumeDTO;
import com.gtcafe.asimov.service.VolumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volumes")
@RequiredArgsConstructor
public class VolumeController {

    private final VolumeService volumeService;

    @GetMapping
    public ResponseEntity<List<VolumeDTO>> getAllVolumes() {
        return ResponseEntity.ok(volumeService.getAllVolumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolumeDTO> getVolume(@PathVariable Long id) {
        return ResponseEntity.ok(volumeService.getVolume(id));
    }

    @PostMapping
    public ResponseEntity<VolumeDTO> createVolume(@RequestBody VolumeDTO volumeDTO) {
        return ResponseEntity.ok(volumeService.createVolume(volumeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VolumeDTO> updateVolume(@PathVariable Long id, @RequestBody VolumeDTO volumeDTO) {
        return ResponseEntity.ok(volumeService.updateVolume(id, volumeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolume(@PathVariable Long id) {
        volumeService.deleteVolume(id);
        return ResponseEntity.ok().build();
    }
}

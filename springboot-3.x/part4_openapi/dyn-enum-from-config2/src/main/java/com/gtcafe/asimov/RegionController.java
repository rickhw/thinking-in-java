package com.gtcafe.asimov;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regions")
public class RegionController {
    @Autowired
    private RegionConfigService regionConfigService;

    @GetMapping("/{region}/ec2/instances")
    public ResponseEntity<?> getEC2Instances(@PathVariable String region) {
        RegionProperties.RegionConfig regionConfig = 
            regionConfigService.getRegionConfig(region);
        
        if (regionConfig == null) {
            return ResponseEntity.badRequest().body("Invalid region");
        }

        return ResponseEntity.ok(regionConfig);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listRegions() {
        return ResponseEntity.ok(
            Arrays.stream(Region.values())
                .filter(r -> r != Region.UNKNOWN)
                .map(r -> Map.of(
                    "name", r.name(),
                    "displayName", r.getDisplayName(),
                    "endpoint", r.getEndpoint()
                ))
                .collect(Collectors.toList())
        );
    }
}
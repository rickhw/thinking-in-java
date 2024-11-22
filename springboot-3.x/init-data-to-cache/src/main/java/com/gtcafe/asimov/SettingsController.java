package com.gtcafe.asimov;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ConfigurationLoader configurationLoader;

    @Autowired
    public SettingsController(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    @GetMapping
    public ResponseEntity<Settings> getSettings() {
        return ResponseEntity.ok(configurationLoader.getCurrentSettings());
    }

    @PutMapping
    public ResponseEntity<Settings> updateSettings(@RequestBody Settings settings) {
        configurationLoader.updateSettings(settings);
        return ResponseEntity.ok(settings);
    }
}
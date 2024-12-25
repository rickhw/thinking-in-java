package com.gtcafe.asimov;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationConfig {
    private final Map<String, String> configMap = new HashMap<>();
    
    @Autowired
    private ConfigProperties yamlConfig;
    
    @Autowired
    private ConfigRepository configRepository;
    
    @PostConstruct
    public void init() {
        // 1. Load static config
        configMap.putAll(StaticConfig.getDefaultConfig());
        
        // 2. Override with YAML config
        configMap.putAll(yamlConfig.getProperties());
        
        // 3. Override with database config
        configRepository.findAll().forEach(config -> 
            configMap.put(config.getConfigKey(), config.getConfigValue())
        );
    }
    
    public String getConfig(String key) {
        return configMap.get(key);
    }
    
    public Map<String, String> getAllConfig() {
        return new HashMap<>(configMap);
    }
    
    public void updateConfig(String key, String value) {
        ConfigEntity config = new ConfigEntity();
        config.setConfigKey(key);
        config.setConfigValue(value);
        configRepository.save(config);
        configMap.put(key, value);
    }
}
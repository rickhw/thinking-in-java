package com.gtcafe.asimov;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.config")
public class ConfigProperties {
    private final Map<String, String> properties = new HashMap<>();

    public Map<String, String> getProperties() {
        return properties;
    }
}
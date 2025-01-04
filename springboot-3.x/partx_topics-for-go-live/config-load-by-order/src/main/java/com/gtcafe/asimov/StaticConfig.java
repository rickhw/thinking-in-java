package com.gtcafe.asimov;

import java.util.HashMap;
import java.util.Map;

public class StaticConfig {
    private static final Map<String, String> DEFAULT_CONFIG = new HashMap<>();
    
    static {
        DEFAULT_CONFIG.put("default.quota", "5");
        DEFAULT_CONFIG.put("max.connections", "50");
    }
    
    public static Map<String, String> getDefaultConfig() {
        return new HashMap<>(DEFAULT_CONFIG);
    }
}
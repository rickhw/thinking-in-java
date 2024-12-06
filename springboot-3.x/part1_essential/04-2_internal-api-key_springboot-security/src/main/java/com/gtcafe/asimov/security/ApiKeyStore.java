package com.gtcafe.asimov.security;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiKeyStore {
    private final Map<String, String> apiKeys = new ConcurrentHashMap<>();

    public ApiKeyStore() {
        apiKeys.put("service1-api-key", "Service 1");
        apiKeys.put("service2-api-key", "Service 2");
    }

    public boolean isValidApiKey(String apiKey) {
        return apiKeys.containsKey(apiKey);
    }

    public void addApiKey(String apiKey, String serviceName) {
        apiKeys.put(apiKey, serviceName);
    }
}
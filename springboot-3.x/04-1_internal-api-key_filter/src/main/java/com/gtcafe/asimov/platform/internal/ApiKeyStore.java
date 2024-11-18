package com.gtcafe.asimov.platform.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ApiKeyStore {
    private final Map<String, String> apiKeys = new ConcurrentHashMap<>();

    public ApiKeyStore() {
        // 初始化一些測試用的 API Keys
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
package com.gtcafe.asimov;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class VersionService {
    private final Properties versionProperties;

    public VersionService() throws IOException {
        versionProperties = new Properties();
        versionProperties.load(new ClassPathResource("version.properties").getInputStream());
    }

    public Map<String, String> getVersionInfo() {
        Map<String, String> versionInfo = new HashMap<>();
        versionInfo.put("githash", versionProperties.getProperty("build.githash", "N/A"));
        versionInfo.put("timestamp", versionProperties.getProperty("build.timestamp", "N/A"));
        versionInfo.put("os.name", versionProperties.getProperty("build.os.name", "N/A"));
        versionInfo.put("os.version", versionProperties.getProperty("build.os.version", "N/A"));
        versionInfo.put("java.version", versionProperties.getProperty("build.java.version", "N/A"));
        versionInfo.put("java.provider", versionProperties.getProperty("build.java.provider", "N/A"));
        return versionInfo;
    }
}
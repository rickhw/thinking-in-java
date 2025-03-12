package com.gtcafe.asimov;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VersionInfoStartupListener implements ApplicationListener<ApplicationStartedEvent> {
    private final VersionService versionService;

    public VersionInfoStartupListener(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Map<String, String> versionInfo = versionService.getVersionInfo();
        
        System.out.println("========================================");
        System.out.println("🚀 Application Startup Information 🚀");
        System.out.println("========================================");
        System.out.println("Application Name: my-awesome-application");
        System.out.println("Build Timestamp: " + versionInfo.get("timestamp"));
        System.out.println("Git Hash: " + versionInfo.get("githash"));
        System.out.println("========================================");
    }
}
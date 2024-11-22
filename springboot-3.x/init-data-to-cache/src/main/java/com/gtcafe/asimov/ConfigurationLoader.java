package com.gtcafe.asimov;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Component
public class ConfigurationLoader {

    private static final String REDIS_KEY = "app:settings";
    private static final String CONFIG_FILE = "config.json";
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Settings> redisTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ConfigurationLoader(RedisTemplate<String, Settings> redisTemplate, 
                             ResourceLoader resourceLoader) {
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() throws Exception {
        // 初始加載配置
        loadConfiguration();
        
        // 設置檔案監控
        setupFileMonitoring();
    }

    public void loadConfiguration() throws Exception {
        InputStream inputStream = resourceLoader.getResource("classpath:" + CONFIG_FILE)
                                             .getInputStream();
        
        Settings settings = objectMapper.readValue(inputStream, Settings.class);
        redisTemplate.opsForValue().set(REDIS_KEY, settings);
    }

    private void setupFileMonitoring() throws Exception {
        String path = resourceLoader.getResource("classpath:" + CONFIG_FILE)
                                  .getFile()
                                  .getParent();
        
        FileAlterationObserver observer = new FileAlterationObserver(path);
        observer.addListener(new FileAlterationListener() {
            @Override
            public void onFileChange(File file) {
                if (file.getName().equals(CONFIG_FILE)) {
                    try {
                        loadConfiguration();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Other required methods with empty implementations
            public void onStart(FileAlterationObserver observer) {}
            public void onDirectoryCreate(File directory) {}
            public void onDirectoryChange(File directory) {}
            public void onDirectoryDelete(File directory) {}
            public void onFileCreate(File file) {}
            public void onFileDelete(File file) {}
            public void onStop(FileAlterationObserver observer) {}
        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(5));
        monitor.addObserver(observer);
        monitor.start();
    }

    public Settings getCurrentSettings() {
        return redisTemplate.opsForValue().get(REDIS_KEY);
    }

    public void updateSettings(Settings newSettings) {
        redisTemplate.opsForValue().set(REDIS_KEY, newSettings);
    }
}
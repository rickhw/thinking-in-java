package com.example;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.model.DataModel;
import com.example.service.RedisService;

@Component
public class DataLoader implements CommandLineRunner {

    private final RedisService redisService;

    @Autowired
    public DataLoader(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void run(String... args) throws Exception {
        DataModel tenantData = new DataModel();
        tenantData.setId("12345");

        for(int i =0; i<=10000; i++) {            
            Map<String, String> tenantInfo = new HashMap<>();
            tenantInfo.put("name", "Tenant " + i);
            tenantInfo.put("location", "Taipei");
            tenantData.addItem(tenantInfo);
        }
        redisService.saveData("tenant", tenantData);
    }
}

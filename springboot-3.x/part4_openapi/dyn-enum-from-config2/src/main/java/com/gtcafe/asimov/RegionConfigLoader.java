package com.gtcafe.asimov;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;

@Configuration
public class RegionConfigLoader {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public RegionProperties regionProperties() throws IOException {
        // 根據 Profile 動態選擇 YAML 檔案
        String filename = "regions-" + activeProfile + ".yaml";
        
        // 如果特定 Profile 的檔案不存在，則使用預設檔案
        Resource resource = new ClassPathResource(filename);
        if (!resource.exists()) {
            resource = new ClassPathResource("regions-default.yaml");
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(resource.getInputStream(), RegionProperties.class);
    }
}
package com.gtcafe.asimov;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Arrays;
import java.util.Map;

@Configuration
public class RegionConfigService implements InitializingBean {

    @Autowired
    private RegionProperties regionProperties;

    @Autowired
    private Environment env;

    // 動態更新 Enum 值
    @Override
    public void afterPropertiesSet() {
        Map<String, RegionProperties.RegionConfig> configMap = 
            regionProperties.toMap();

        // 動態更新 Enum
        Arrays.stream(Region.values())
            .filter(r -> r != Region.UNKNOWN)
            .forEach(region -> {
                RegionProperties.RegionConfig config = 
                    configMap.get(region.name());
                
                if (config != null) {
                    region.setDisplayName(config.getDisplayName());
                    region.setEndpoint(config.getEndpoint());
                }
            });
    }

    // 額外的輔助方法
    public RegionProperties.RegionConfig getRegionConfig(String regionName) {
        return regionProperties.toMap().get(regionName);
    }
}
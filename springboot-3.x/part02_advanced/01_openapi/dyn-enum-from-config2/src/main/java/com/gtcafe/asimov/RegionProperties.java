package com.gtcafe.asimov;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class RegionProperties {
    private List<RegionConfig> regions;

    @Data
    public static class RegionConfig {
        private String name;
        private String displayName;
        private String endpoint;
    }

    // 將配置轉換為 Map，方便後續處理
    public Map<String, RegionConfig> toMap() {
        return regions.stream()
            .collect(Collectors.toMap(
                RegionConfig::getName, 
                config -> config
            ));
    }
}

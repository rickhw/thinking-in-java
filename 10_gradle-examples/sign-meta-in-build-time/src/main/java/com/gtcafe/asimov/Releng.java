package com.gtcafe.asimov;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app")
@PropertySource("classpath:releng.properties")
@Data
public class Releng {

    private String productName;

    private String serviceName;

    private String version;

    private String buildType;

    private String buildId;

    private String hashcode;

    private String buildSdk;
    private String buildOs;
}

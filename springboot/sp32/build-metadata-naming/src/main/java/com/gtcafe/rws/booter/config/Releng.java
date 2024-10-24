package com.gtcafe.rws.booter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@PropertySource("classpath:releng.properties")
public class Releng {
    // private String productName;
    // private String serviceName;

    private String groupId;
    private String roleId;

    private String version;
    private String buildType;
    private String buildId;
    private String hashcode;

    // Getter 和 Setter 方法


    public String toString() {
        return String.format("{"
            // + "\"productName\": \"%s\""
            // + ", \"serviceName\": \"%s\"" 
            + " \"groupId:\": \"%s\""
            + ", \"roleId:\": \"%s\""
            + ", \"version:\": \"%s\""
            + ", \"buildType:\": \"%s\""
            + ", \"buildId:\": \"%s\""
            + ", \"hashcode:\": \"%s\""
            + "}",
            // productName, serviceName, 
            groupId, roleId, version, buildType, buildId, hashcode) ;

    }

    // public String getProductName() {
    //     return productName;
    // }

    // public void setProductName(String productName) {
    //     this.productName = productName;
    // }

    // public String getServiceName() {
    //     return serviceName;
    // }

    // public void setServiceName(String serviceName) {
    //     this.serviceName = serviceName;
    // }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getHashcode() {
        return hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

}

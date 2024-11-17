package com.gtcafe.app.domain;

public enum TenantStatus {
    PENDING("pending","inqueue"),
    ACTIVE("active", "the resource is active."),
    INACTIVE("inactive", "the resource is inactive."),
    UPDATING("updating", "the resource is updating."),
    DELETING("deleting", "the resource is deleting."),
    DELETED("deleted", "the resource is deleted."),
    ERROR("error", "the resource is error."),
    UNKNOWN("unknow", "the resource is unknown.");

    private String name;
    private String description;

    TenantStatus (String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }
    
    public String getDescription() {
        return this.description;
    }
}


package com.gtcafe.app.platform.tenant.domain.model;

public enum TenantState {
    ACTIVE("ACTIVE", "the condition for query"),

    pending("pending", "Tenant is pending for activation"),
    active("active", "Tenant is active"),
    inactive("inactive", "Tenant is inactive"),
    updating("updating", "Tenant is updating"),
    deleting("deleting", "Tenant is deleting"),
    deleted("deleted", "Tenant is deleted")
    ;

    private String label;
    private String description;

    private TenantState(String label, String desc) {
        this.label = label;
        this.description = desc;
    }

    public String toString() {
        return this.label;
    }

}
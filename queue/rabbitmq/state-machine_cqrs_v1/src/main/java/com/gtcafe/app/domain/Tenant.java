package com.gtcafe.app.domain;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@ToString
public class Tenant extends BaseResource implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private TenantState state;
    
    public enum TenantState {
        INITING, ACTIVE, INACTIVE, TERMINATED
    }
}
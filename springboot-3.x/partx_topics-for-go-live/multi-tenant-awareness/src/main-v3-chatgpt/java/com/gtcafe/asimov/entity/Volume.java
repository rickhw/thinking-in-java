package com.gtcafe.asimov.entity;

import com.gtcafe.asimov.tenant.TenantAware;
import com.gtcafe.asimov.tenant.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(TenantAwareEntityListener.class)
public class Volume implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(nullable = true)
    private String tenantId;
}
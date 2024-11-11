// Tenant.java
package com.gtcafe.app.domain;

import com.gtcafe.app.enums.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    private String id;
    private TenantStatus status;
}

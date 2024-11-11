// TenantStatusUpdateRequest.java
package com.gtcafe.app.dto;

import com.gtcafe.app.enums.TenantStatus;
import lombok.Data;

@Data
public class TenantStatusUpdateRequest {
    private String tenantId;
    private TenantStatus newStatus;
}

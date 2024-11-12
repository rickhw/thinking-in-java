package com.gtcafe.app.commands;

import com.gtcafe.app.domain.TenantStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateTenantStatusCommand {
    private Long tenantId;
    private TenantStatus targetStatus;
}
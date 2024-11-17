package com.gtcafe.app.commands;

import com.gtcafe.app.domain.TenantStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateTenantStatusCommand {
    @Getter @Setter
    private Long tenantId;

    @Getter @Setter
    private TenantStatus targetStatus;
}
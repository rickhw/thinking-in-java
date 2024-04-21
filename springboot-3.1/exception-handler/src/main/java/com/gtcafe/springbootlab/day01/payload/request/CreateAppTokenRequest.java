package com.gtcafe.springbootlab.day01.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CreateAppTokenRequest {

	@NotBlank(message = "tokenName cannot empty")
    @Size(max = 50, message = "tokenName max lenght is 50.")
    private String tokenName;

    @NotBlank(message = "appName cannot empty")
    @Size(max = 50, message = "appName max lenght is 50.")
    private String appName;

    @NotNull(message = "tenantId cannot empty")
    @PositiveOrZero(message = "tenantId must > 0")
    private Integer tenantId;

	public String getTokenName() {
		return tokenName;
	}

	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getTenantId() {
        return tenantId;
    }
 
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }
}

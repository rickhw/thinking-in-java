package com.gtcafe.rws.booter.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CreateTenantRequest {

	@NotBlank(message = "tenantName cannot empty")
    @Size(max = 50, message = "tenantName max lenght is 50.")
	private String tenantName;

	@NotBlank(message = "tentenantKey cannot empty")
    @Size(max = 50, message = "accountName max lenght is 50.")
  	private String tenantKey;

    @NotBlank(message = "description cannot empty")
    @Size(max = 255, message = "description max lenght is 255.")
    private String description;

    @NotBlank(message = "rootAccountId cannot empty")
	@PositiveOrZero(message = "tenantId must > 0")
  	private int rootAccountId;

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantKey() {
		return tenantKey;
	}

	public void setTenantKey(String tenantKey) {
		this.tenantKey = tenantKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getRootAccountId() {
		return rootAccountId;
	}

	public void setRootAccountId(int rootAccountId) {
		this.rootAccountId = rootAccountId;
	}

}

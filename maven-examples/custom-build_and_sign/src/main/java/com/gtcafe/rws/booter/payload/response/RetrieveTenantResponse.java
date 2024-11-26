package com.gtcafe.rws.booter.payload.response;

public class RetrieveTenantResponse {

	private Long id;
	private String tenantName;
  	// private String tenantKey;
    private String description;
  	private Long rootAccountId;
	
	
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	// public String getTenantKey() {
	// 	return tenantKey;
	// }
	// public void setTenantKey(String tenantKey) {
	// 	this.tenantKey = tenantKey;
	// }
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getRootAccountId() {
		return rootAccountId;
	}
	public void setRootAccountId(Long rootAccountId) {
		this.rootAccountId = rootAccountId;
	}


}

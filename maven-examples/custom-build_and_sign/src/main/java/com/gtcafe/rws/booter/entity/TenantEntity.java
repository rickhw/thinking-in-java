package com.gtcafe.rws.booter.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class TenantEntity {

	// internal id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
	private long id;

	// for display only, modifiable
	@Column(name = "tenant_name")
	private String tenantName;

	// a key value, unique, non-modifiable
	// @Column(name = "tenant_key")
	// private String tenantKey;

	// a key value, unique, modifiable
	// @Column(name = "dns")
	// private String dns;
	
	// @Column(name = "tenant_type")
	// private ETenant tenantType;

	@Column(name = "description")
	private String description;

	@OneToOne(cascade = CascadeType.ALL)
	@MapsId
	@JoinColumn(name = "account_id")
  	private AccountEntity rootAccount;


	public TenantEntity() {

	}

	// public String getTenantKey() {
	// 	return tenantKey;
	// }

	// public void setTenantKey(String tenantKey) {
	// 	this.tenantKey = tenantKey;
	// }

	// public String getDns() {
	// 	return dns;
	// }

	// public void setDns(String dns) {
	// 	this.dns = dns;
	// }

	public TenantEntity(String tenantName, String description) {
		super();
		this.tenantName = tenantName;
		this.description = description;
		// this.emailId = emailId;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String firstName) {
		this.tenantName = firstName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String lastName) {
		this.description = lastName;
	}

	// public ETenant getTenantType() {
	// 	return tenantType;
	// }

	// public void setTenantType(ETenant tenantType) {
	// 	this.tenantType = tenantType;
	// }

	public AccountEntity getRootAccount() {
		return rootAccount;
	}

	public void setRootAccount(AccountEntity rootAccount) {
		this.rootAccount = rootAccount;
	}

}

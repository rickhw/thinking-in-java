package com.gtcafe.rws.booter.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;

	@Column(name = "account_name")
	private String accountName;

	// @Column(name = "provider")
	// private AuthProvider provider;
	@Column(name = "disabled", columnDefinition = "boolean default false")
	private boolean disabled;

	@Column(name = "state")
	// @Enumerated(EnumType.ORDINAL)
	@Enumerated(EnumType.STRING)
	private EAccountState state;

	@Column(name = "description")
	private String description;

	@Column(name = "createdAt")
	private Date createdAt;

	public AccountEntity() {

	}

	public AccountEntity(String accountName, String description) {
		super();
		this.accountName = accountName;
		this.description = description;
		// this.emailId = emailId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String firstName) {
		this.accountName = firstName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String lastName) {
		this.description = lastName;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public EAccountState getState() {
		return state;
	}

	public void setState(EAccountState state) {
		this.state = state;
	}

	// public AuthProvider getProvider() {
	// 	return provider;
	// }

	// public void setProvider(AuthProvider provider) {
	// 	this.provider = provider;
	// }

	// public TenantEntity getTenant() {
	// 	return tenant;
	// }

	// public void setTenant(TenantEntity tenant) {
	// 	this.tenant = tenant;
	// }

}

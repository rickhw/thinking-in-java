package com.gtcafe.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtcafe.app.domain.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
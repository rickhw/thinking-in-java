package com.gtcafe.app.repository;

import com.gtcafe.app.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
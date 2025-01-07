package com.gtcafe.asimov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.gtcafe.asimov.entity.ITenantAwareEntity;

@NoRepositoryBean
public interface TenantAwareRepository<T extends ITenantAwareEntity> extends JpaRepository<T, Long> {
}
package com.gtcafe.asimov.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gtcafe.asimov.entity.Volume;

@Repository
public interface VolumeRepository extends TenantAwareRepository<Volume> {
    List<Volume> findAllByTenantId(String tenantId);
    Optional<Volume> findByIdAndTenantId(Long id, String tenantId);
}

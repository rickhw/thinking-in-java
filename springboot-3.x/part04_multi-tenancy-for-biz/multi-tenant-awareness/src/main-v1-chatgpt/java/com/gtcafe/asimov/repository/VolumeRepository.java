package com.gtcafe.asimov.repository;

import com.gtcafe.asimov.entity.Volume;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeRepository extends TenantAwareRepository<Volume, Long> {
    List<Volume> findByTenantId(String tenantId);
}

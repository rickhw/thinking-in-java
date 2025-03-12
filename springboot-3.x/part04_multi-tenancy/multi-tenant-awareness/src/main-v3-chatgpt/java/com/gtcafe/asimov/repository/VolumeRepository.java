package com.gtcafe.asimov.repository;

import com.gtcafe.asimov.entity.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {
    List<Volume> findByTenantId(String tenantId);
}
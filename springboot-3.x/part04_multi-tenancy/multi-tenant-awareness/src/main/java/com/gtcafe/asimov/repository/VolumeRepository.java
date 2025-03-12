package com.gtcafe.asimov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.asimov.entity.Volume;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {
}

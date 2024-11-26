package com.gtcafe.rws.booter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.rws.booter.entity.TenantEntity;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, Long>{

}

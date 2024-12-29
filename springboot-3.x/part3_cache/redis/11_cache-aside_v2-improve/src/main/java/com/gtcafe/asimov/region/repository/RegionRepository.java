package com.gtcafe.asimov.region.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Long>{

    RegionEntity findByRegionCode(String regionCode);
    
    @Query("SELECT r FROM RegionEntity r")
    List<RegionEntity> findAllWithPagination(Pageable pageable);
    
    default List<RegionEntity> findAllWithPagination(int pageNumber, int pageSize) {
        return findAllWithPagination(PageRequest.of(pageNumber, pageSize));
    }

    // RegionEntity findByRegionCode(String regionCode);
}

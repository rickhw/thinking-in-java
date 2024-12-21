package com.gtcafe.asimov.region;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtcafe.asimov.cache.CacheRepository;
import com.gtcafe.asimov.region.repository.RegionEntity;
import com.gtcafe.asimov.region.repository.RegionRepository;
import com.gtcafe.asimov.region.schema.Region;
import com.gtcafe.asimov.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegionService {

    private static final String CACHE_KEY_PREFIX = "region:";
    // private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    
    private final CacheRepository cacheRepos;
    private final RegionRepository dbRepos;
    private final JsonUtils jsonUtils;


    @Transactional
    public Region create(Region region) {
        try {
            RegionEntity entity = RegionMapper.mapDomainToEntity(region);
            dbRepos.save(entity);

            region.setResourceId(entity.getId());
            
            updateCache(region);
            
            return region;
        } catch (Exception e) {
            log.error("Error creating region: [{}]", region, e);
            throw new RuntimeException("Failed to create region", e);
        }
    }

    // Cache-aside pattern
    @Retryable(value = DataAccessException.class, maxAttempts = 3)
    public Region retrieve(String regionCode) {
        String cacheKey = generateCacheKey(regionCode);
        
        // 1. Try cache first
        Optional<Region> cachedRegion = getFromCache(cacheKey);
        if (cachedRegion.isPresent()) {
            log.debug("Cache hit for region: [{}]", regionCode);
            return cachedRegion.get();
        }

        // 1.1 Cache miss - try to acquire lock
        String lockKey = cacheKey + ":lock";
        boolean lockAcquired = false;
        
        try {
            lockAcquired = cacheRepos.setIfNotExists(lockKey, getLockString(), LOCK_TIMEOUT);
            log.debug("Lock acquired: [{}]", lockAcquired);
            
            if (lockAcquired) {
                // Double check cache after acquiring lock
                cachedRegion = getFromCache(cacheKey);
                if (cachedRegion.isPresent()) {
                    return cachedRegion.get();
                }

                // Load from database
                Region region = loadFromDatabase(regionCode);
                if (region != null) {
                    updateCache(region);
                    return region;
                }
                
                throw new ResourceNotFoundException("Region not found: " + regionCode);
            } else {
                // Wait for other thread to load data
                Thread.sleep(100);
                return retrieve(regionCode);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        } finally {
            if (lockAcquired) {
                cacheRepos.delete(lockKey);
            }
        }
    }

    private Optional<Region> getFromCache(String cacheKey) {
        log.debug("Reading from cache for cacheKey: [{}]", cacheKey);
        try {
            String cachedValue = cacheRepos.retrieveObject(cacheKey)
                .orElse(null);
                
            if (cachedValue == null) {
                return Optional.empty();
            }
            
            return jsonUtils.jsonStringToModelSafe(cachedValue, Region.class);
        } catch (Exception e) {
            log.warn("Error reading from cache for cacheKey: [{}]", cacheKey, e);
            return Optional.empty();
        }
    }

    private Region loadFromDatabase(String regionCode) {
        log.debug("Loading region from database, regionCode: [{}]", regionCode);
        RegionEntity entity = dbRepos.findByRegionCode(regionCode);
        return entity != null ? RegionMapper.mapEntityToDomain(entity) : null;
    }

    private void updateCache(Region region) {
        log.debug("Updating cache for region: [{}]", region.getRegionCode());
        try {
            String cacheKey = generateCacheKey(region.getRegionCode());
            String jsonString = jsonUtils.modelToJsonString(region);
            cacheRepos.saveOrUpdateObject(cacheKey, jsonString, CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error updating cache for region: [{}]", region.getRegionCode(), e);
        }
    }

    private String generateCacheKey(String regionCode) {
        return CACHE_KEY_PREFIX + regionCode;
    }

    private String getLockString() {
        return Long.toString(Thread.currentThread().getId());
    }
}
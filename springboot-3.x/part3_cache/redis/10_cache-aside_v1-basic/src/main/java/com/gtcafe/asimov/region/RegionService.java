package com.gtcafe.asimov.region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.cache.CacheRepository;
import com.gtcafe.asimov.region.repository.RegionEntity;
import com.gtcafe.asimov.region.repository.RegionRepository;
import com.gtcafe.asimov.region.schema.Region;
import com.gtcafe.asimov.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegionService {

  @Autowired
  private CacheRepository cacheRepos;

  @Autowired
  private RegionRepository dbRepos;

  @Autowired
  private JsonUtils jsonUtils;

  // @Autowired
  // private TimeUtils timeUtils;

  public Region create(Region region) {
    RegionEntity entity = RegionMapper.mapDomainToEntity(region);

    dbRepos.save(entity);

    // restore the id
    region.setResourceId(entity.getId());

    // save to cache
    String jsonString = jsonUtils.modelToJsonString(region);
    cacheRepos.saveOrUpdateObject(region.getRegionCode(), jsonString);

    return region;
  }

  // Cache aside pattern
  public Region retrieve(String regionCode) {
    Region region = null;

    // 1. find from cache
    String regionString = cacheRepos.retrieveObject(regionCode);

    if (regionString != null) {
      region = jsonUtils.jsonStringToModel(regionString, Region.class);
      log.info("retrieve regionCode [{}] from cache directly.", regionCode);
      return region;
    }
    
    // 2. if not found, find from db
    if (region == null) {
      RegionEntity entity = dbRepos.findByRegionCode(regionCode);
      region = RegionMapper.mapEntityToDomain(entity);
    }

    // 2.1 save to cache
    if (region != null) {

      String jsonString = jsonUtils.modelToJsonString(region);
      cacheRepos.saveOrUpdateObject(regionCode, jsonString);
      log.info("cache miss, retrieve regionCode [{}] from db, and update to cache", regionCode);

      return region;
    } 

    // 3. if still not found, return null
    log.error("Region not found: [{}]", regionCode);

    return region;
  }

}
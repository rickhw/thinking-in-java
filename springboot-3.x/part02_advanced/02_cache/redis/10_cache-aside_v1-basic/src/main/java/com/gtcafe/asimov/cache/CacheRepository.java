package com.gtcafe.asimov.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class CacheRepository {

  private final RedisTemplate<String, String> _redisTemplate;
  private final ValueOperations<String, String> _valueOps;

  @Autowired
  public CacheRepository(RedisTemplate<String, String> redisTemplate) {
    this._redisTemplate = redisTemplate;
    this._valueOps = redisTemplate.opsForValue();
  }

  // Create or Update any type of DomainObject
  public void saveOrUpdateObject(String key, String object) {
    this._valueOps.set(key, object);

    // Optional: Set expiration time if needed
    // valueOps.set(key, domainObject, 1, TimeUnit.HOURS);
  }

  public String retrieveObject(String key) {
      String result = _valueOps.get(key);
      // if (result != null && clazz.isInstance(result)) {
      //     return (T) result;
      // }
      return result;
  }


  // Update any type of DomainObject
  public void updateObject(String key, String object) {
    if (Boolean.TRUE.equals(_redisTemplate.hasKey(key))) {
        _valueOps.set(key, object);
    } else {
        throw new RuntimeException("DomainObject not found for key: " + key);
    }
  }

  // Delete a DomainObject by key
  public void deleteObject(String key) {
      _redisTemplate.delete(key);
  }

}

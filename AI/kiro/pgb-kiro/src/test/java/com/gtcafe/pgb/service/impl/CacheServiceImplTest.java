package com.gtcafe.pgb.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import com.gtcafe.pgb.service.CacheService;

/**
 * CacheService 單元測試
 */
@ExtendWith(MockitoExtension.class)
class CacheServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        cacheService = new CacheServiceImpl(redisTemplate);
    }

    @Test
    void testSet() {
        // Given
        String key = "test:key";
        String value = "test value";

        // When
        cacheService.set(key, value);

        // Then
        verify(valueOperations).set(key, value);
    }

    @Test
    void testSetWithTimeout() {
        // Given
        String key = "test:key";
        String value = "test value";
        Duration timeout = Duration.ofMinutes(10);

        // When
        cacheService.set(key, value, timeout);

        // Then
        verify(valueOperations).set(key, value, timeout);
    }

    @Test
    void testSetThrowsException() {
        // Given
        String key = "test:key";
        String value = "test value";
        doThrow(new RuntimeException("Redis error")).when(valueOperations).set(key, value);

        // When & Then
        assertThatThrownBy(() -> cacheService.set(key, value))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to set cache");
    }

    @Test
    void testGet() {
        // Given
        String key = "test:key";
        String expectedValue = "test value";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // When
        Object result = cacheService.get(key);

        // Then
        assertThat(result).isEqualTo(expectedValue);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithType() {
        // Given
        String key = "test:key";
        String expectedValue = "test value";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // When
        String result = cacheService.get(key, String.class);

        // Then
        assertThat(result).isEqualTo(expectedValue);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithTypeWrongType() {
        // Given
        String key = "test:key";
        String value = "test value";
        when(valueOperations.get(key)).thenReturn(value);

        // When
        Integer result = cacheService.get(key, Integer.class);

        // Then
        assertThat(result).isNull();
        verify(valueOperations).get(key);
    }

    @Test
    void testGetReturnsNullOnException() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis error"));

        // When
        Object result = cacheService.get(key);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testDelete() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(true);

        // When
        Boolean result = cacheService.delete(key);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).delete(key);
    }

    @Test
    void testDeleteMultiple() {
        // Given
        Set<String> keys = new HashSet<>(Arrays.asList("key1", "key2", "key3"));
        when(redisTemplate.delete(keys)).thenReturn(3L);

        // When
        Long result = cacheService.delete(keys);

        // Then
        assertThat(result).isEqualTo(3L);
        verify(redisTemplate).delete(keys);
    }

    @Test
    void testExists() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // When
        Boolean result = cacheService.exists(key);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testExpire() {
        // Given
        String key = "test:key";
        Duration timeout = Duration.ofMinutes(10);
        when(redisTemplate.expire(key, timeout)).thenReturn(true);

        // When
        Boolean result = cacheService.expire(key, timeout);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).expire(key, timeout);
    }

    @Test
    void testGetExpire() {
        // Given
        String key = "test:key";
        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(600L);

        // When
        Long result = cacheService.getExpire(key);

        // Then
        assertThat(result).isEqualTo(600L);
        verify(redisTemplate).getExpire(key, TimeUnit.SECONDS);
    }

    @Test
    void testHSet() {
        // Given
        String key = "test:hash";
        String hashKey = "field1";
        String value = "value1";

        // When
        cacheService.hSet(key, hashKey, value);

        // Then
        verify(hashOperations).put(key, hashKey, value);
    }

    @Test
    void testHGet() {
        // Given
        String key = "test:hash";
        String hashKey = "field1";
        String expectedValue = "value1";
        when(hashOperations.get(key, hashKey)).thenReturn(expectedValue);

        // When
        Object result = cacheService.hGet(key, hashKey);

        // Then
        assertThat(result).isEqualTo(expectedValue);
        verify(hashOperations).get(key, hashKey);
    }

    @Test
    void testHDelete() {
        // Given
        String key = "test:hash";
        String[] hashKeys = { "field1", "field2" };
        when(hashOperations.delete(eq(key), any(Object[].class))).thenReturn(2L);

        // When
        Long result = cacheService.hDelete(key, hashKeys);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(hashOperations).delete(eq(key), any(Object[].class));
    }

    @Test
    void testHExists() {
        // Given
        String key = "test:hash";
        String hashKey = "field1";
        when(hashOperations.hasKey(key, hashKey)).thenReturn(true);

        // When
        Boolean result = cacheService.hExists(key, hashKey);

        // Then
        assertThat(result).isTrue();
        verify(hashOperations).hasKey(key, hashKey);
    }

    @Test
    void testLLeftPush() {
        // Given
        String key = "test:list";
        Object[] values = { "value1", "value2" };
        when(listOperations.leftPushAll(key, values)).thenReturn(2L);

        // When
        Long result = cacheService.lLeftPush(key, values);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(listOperations).leftPushAll(key, values);
    }

    @Test
    void testLRightPush() {
        // Given
        String key = "test:list";
        Object[] values = { "value1", "value2" };
        when(listOperations.rightPushAll(key, values)).thenReturn(2L);

        // When
        Long result = cacheService.lRightPush(key, values);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(listOperations).rightPushAll(key, values);
    }

    @Test
    void testLLeftPop() {
        // Given
        String key = "test:list";
        String expectedValue = "value1";
        when(listOperations.leftPop(key)).thenReturn(expectedValue);

        // When
        Object result = cacheService.lLeftPop(key);

        // Then
        assertThat(result).isEqualTo(expectedValue);
        verify(listOperations).leftPop(key);
    }

    @Test
    void testLRightPop() {
        // Given
        String key = "test:list";
        String expectedValue = "value1";
        when(listOperations.rightPop(key)).thenReturn(expectedValue);

        // When
        Object result = cacheService.lRightPop(key);

        // Then
        assertThat(result).isEqualTo(expectedValue);
        verify(listOperations).rightPop(key);
    }

    @Test
    void testLRange() {
        // Given
        String key = "test:list";
        List<Object> expectedList = Arrays.asList("value1", "value2", "value3");
        when(listOperations.range(key, 0, -1)).thenReturn(expectedList);

        // When
        List<Object> result = cacheService.lRange(key, 0, -1);

        // Then
        assertThat(result).isEqualTo(expectedList);
        verify(listOperations).range(key, 0, -1);
    }

    @Test
    void testLSize() {
        // Given
        String key = "test:list";
        when(listOperations.size(key)).thenReturn(3L);

        // When
        Long result = cacheService.lSize(key);

        // Then
        assertThat(result).isEqualTo(3L);
        verify(listOperations).size(key);
    }

    @Test
    void testSAdd() {
        // Given
        String key = "test:set";
        Object[] values = { "value1", "value2" };
        when(setOperations.add(key, values)).thenReturn(2L);

        // When
        Long result = cacheService.sAdd(key, values);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(setOperations).add(key, values);
    }

    @Test
    void testSRemove() {
        // Given
        String key = "test:set";
        Object[] values = { "value1", "value2" };
        when(setOperations.remove(key, values)).thenReturn(2L);

        // When
        Long result = cacheService.sRemove(key, values);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(setOperations).remove(key, values);
    }

    @Test
    void testSIsMember() {
        // Given
        String key = "test:set";
        String value = "value1";
        when(setOperations.isMember(key, value)).thenReturn(true);

        // When
        Boolean result = cacheService.sIsMember(key, value);

        // Then
        assertThat(result).isTrue();
        verify(setOperations).isMember(key, value);
    }

    @Test
    void testSMembers() {
        // Given
        String key = "test:set";
        Set<Object> expectedSet = new HashSet<>(Arrays.asList("value1", "value2"));
        when(setOperations.members(key)).thenReturn(expectedSet);

        // When
        Set<Object> result = cacheService.sMembers(key);

        // Then
        assertThat(result).isEqualTo(expectedSet);
        verify(setOperations).members(key);
    }

    @Test
    void testSSize() {
        // Given
        String key = "test:set";
        when(setOperations.size(key)).thenReturn(2L);

        // When
        Long result = cacheService.sSize(key);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(setOperations).size(key);
    }

    @Test
    void testKeys() {
        // Given
        String pattern = "test:*";
        Set<String> expectedKeys = new HashSet<>(Arrays.asList("test:key1", "test:key2"));
        when(redisTemplate.keys(pattern)).thenReturn(expectedKeys);

        // When
        Set<String> result = cacheService.keys(pattern);

        // Then
        assertThat(result).isEqualTo(expectedKeys);
        verify(redisTemplate).keys(pattern);
    }

    @Test
    void testClearByPattern() {
        // Given
        String pattern = "test:*";
        Set<String> keys = new HashSet<>(Arrays.asList("test:key1", "test:key2"));
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // When
        Long result = cacheService.clearByPattern(pattern);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(keys);
    }

    @Test
    void testClearByPatternNoKeys() {
        // Given
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(new HashSet<>());

        // When
        Long result = cacheService.clearByPattern(pattern);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(any(Set.class));
    }
}
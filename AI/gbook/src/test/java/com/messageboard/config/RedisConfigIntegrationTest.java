package com.messageboard.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Redis 配置單元測試
 * 測試 Redis 連線、基本操作和序列化功能
 * 注意：此測試需要本地 Redis 服務運行
 */
class RedisConfigIntegrationTest {

    private RedisTemplate<String, Object> redisTemplate;
    private StringRedisTemplate stringRedisTemplate;
    private RedisHealthIndicator redisHealthIndicator;
    private RedisConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        // 手動建立 Redis 配置
        try {
            connectionFactory = new LettuceConnectionFactory("localhost", 6379);
            ((LettuceConnectionFactory) connectionFactory).setDatabase(1); // 使用測試資料庫
            ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();
            
            RedisConfig redisConfig = new RedisConfig();
            redisTemplate = redisConfig.redisTemplate(connectionFactory);
            stringRedisTemplate = redisConfig.stringRedisTemplate(connectionFactory);
            redisHealthIndicator = new RedisHealthIndicator(connectionFactory);
            
            // 檢查 Redis 是否可用，如果不可用則跳過測試
            assumeTrue(redisHealthIndicator.isHealthy(), "Redis is not available, skipping tests");
            
            // 清空測試資料庫
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        } catch (Exception e) {
            assumeTrue(false, "Could not connect to Redis: " + e.getMessage());
        }
    }

    @Test
    void testRedisConnection() {
        // 測試 Redis 連線是否正常
        String ping = redisTemplate.getConnectionFactory().getConnection().ping();
        assertThat(ping).isEqualTo("PONG");
    }

    @Test
    void testStringRedisTemplate() {
        // 測試 StringRedisTemplate 基本操作
        String key = "test:string";
        String value = "Hello Redis";

        // 設定值
        stringRedisTemplate.opsForValue().set(key, value);

        // 取得值
        String retrievedValue = stringRedisTemplate.opsForValue().get(key);
        assertThat(retrievedValue).isEqualTo(value);

        // 測試過期時間
        stringRedisTemplate.expire(key, 10, TimeUnit.SECONDS);
        Long ttl = stringRedisTemplate.getExpire(key);
        assertThat(ttl).isGreaterThan(0);

        // 刪除鍵
        Boolean deleted = stringRedisTemplate.delete(key);
        assertThat(deleted).isTrue();

        // 確認已刪除
        String deletedValue = stringRedisTemplate.opsForValue().get(key);
        assertThat(deletedValue).isNull();
    }

    @Test
    void testRedisTemplateWithObject() {
        // 測試 RedisTemplate 物件序列化
        String key = "test:user";
        String testData = "test user data";

        // 設定物件
        redisTemplate.opsForValue().set(key, testData);

        // 取得物件
        Object retrievedData = redisTemplate.opsForValue().get(key);
        assertThat(retrievedData).isNotNull();
        assertThat(retrievedData).isEqualTo(testData);

        // 測試過期時間
        redisTemplate.expire(key, 30, TimeUnit.SECONDS);
        Long ttl = redisTemplate.getExpire(key);
        assertThat(ttl).isGreaterThan(0);
    }

    @Test
    void testHashOperations() {
        // 測試 Hash 操作
        String key = "test:hash";
        String hashKey1 = "field1";
        String hashKey2 = "field2";
        String value1 = "value1";
        String value2 = "value2";

        // 設定 Hash 值
        redisTemplate.opsForHash().put(key, hashKey1, value1);
        redisTemplate.opsForHash().put(key, hashKey2, value2);

        // 取得 Hash 值
        Object retrievedValue1 = redisTemplate.opsForHash().get(key, hashKey1);
        Object retrievedValue2 = redisTemplate.opsForHash().get(key, hashKey2);

        assertThat(retrievedValue1).isEqualTo(value1);
        assertThat(retrievedValue2).isEqualTo(value2);

        // 取得所有 Hash 鍵
        Long size = redisTemplate.opsForHash().size(key);
        assertThat(size).isEqualTo(2);

        // 刪除 Hash 欄位
        Long deleted = redisTemplate.opsForHash().delete(key, hashKey1);
        assertThat(deleted).isEqualTo(1);

        // 確認欄位已刪除
        Boolean exists = redisTemplate.opsForHash().hasKey(key, hashKey1);
        assertThat(exists).isFalse();
    }

    @Test
    void testListOperations() {
        // 測試 List 操作
        String key = "test:list";
        String value1 = "item1";
        String value2 = "item2";
        String value3 = "item3";

        // 左推入
        redisTemplate.opsForList().leftPush(key, value1);
        redisTemplate.opsForList().leftPush(key, value2);
        redisTemplate.opsForList().leftPush(key, value3);

        // 取得列表長度
        Long size = redisTemplate.opsForList().size(key);
        assertThat(size).isEqualTo(3);

        // 取得範圍內的元素
        var list = redisTemplate.opsForList().range(key, 0, -1);
        assertThat(list).hasSize(3);
        assertThat(list).containsExactly(value3, value2, value1);

        // 右彈出
        Object poppedValue = redisTemplate.opsForList().rightPop(key);
        assertThat(poppedValue).isEqualTo(value1);

        // 確認列表長度減少
        Long newSize = redisTemplate.opsForList().size(key);
        assertThat(newSize).isEqualTo(2);
    }

    @Test
    void testSetOperations() {
        // 測試 Set 操作
        String key = "test:set";
        String member1 = "member1";
        String member2 = "member2";
        String member3 = "member3";

        // 添加成員
        redisTemplate.opsForSet().add(key, member1, member2, member3);

        // 取得集合大小
        Long size = redisTemplate.opsForSet().size(key);
        assertThat(size).isEqualTo(3);

        // 檢查成員是否存在
        Boolean isMember = redisTemplate.opsForSet().isMember(key, member1);
        assertThat(isMember).isTrue();

        // 取得所有成員
        var members = redisTemplate.opsForSet().members(key);
        assertThat(members).hasSize(3);
        assertThat(members).containsExactlyInAnyOrder(member1, member2, member3);

        // 移除成員
        Long removed = redisTemplate.opsForSet().remove(key, member1);
        assertThat(removed).isEqualTo(1);

        // 確認成員已移除
        Boolean stillMember = redisTemplate.opsForSet().isMember(key, member1);
        assertThat(stillMember).isFalse();
    }

    @Test
    void testKeyOperations() {
        // 測試鍵操作
        String key1 = "test:key1";
        String key2 = "test:key2";
        String pattern = "test:*";

        // 設定一些鍵
        stringRedisTemplate.opsForValue().set(key1, "value1");
        stringRedisTemplate.opsForValue().set(key2, "value2");

        // 檢查鍵是否存在
        Boolean exists1 = redisTemplate.hasKey(key1);
        Boolean exists2 = redisTemplate.hasKey(key2);
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();

        // 搜尋符合模式的鍵
        var keys = redisTemplate.keys(pattern);
        assertThat(keys).hasSize(2);
        assertThat(keys).containsExactlyInAnyOrder(key1, key2);

        // 設定過期時間
        redisTemplate.expire(key1, 60, TimeUnit.SECONDS);
        Long ttl = redisTemplate.getExpire(key1);
        assertThat(ttl).isGreaterThan(0);

        // 刪除鍵
        Long deleted = redisTemplate.delete(keys);
        assertThat(deleted).isEqualTo(2);

        // 確認鍵已刪除
        Boolean stillExists1 = redisTemplate.hasKey(key1);
        Boolean stillExists2 = redisTemplate.hasKey(key2);
        assertThat(stillExists1).isFalse();
        assertThat(stillExists2).isFalse();
    }
}
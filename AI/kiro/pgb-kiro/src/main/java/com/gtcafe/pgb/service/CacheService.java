package com.gtcafe.pgb.service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 快取服務介面
 * 提供統一的快取操作介面，支援各種資料類型的快取操作
 */
public interface CacheService {

    /**
     * 設定快取值
     * @param key 快取鍵
     * @param value 快取值
     */
    void set(String key, Object value);

    /**
     * 設定快取值並指定過期時間
     * @param key 快取鍵
     * @param value 快取值
     * @param timeout 過期時間
     */
    void set(String key, Object value, Duration timeout);

    /**
     * 取得快取值
     * @param key 快取鍵
     * @return 快取值，如果不存在則返回 null
     */
    Object get(String key);

    /**
     * 取得快取值並指定類型
     * @param key 快取鍵
     * @param clazz 值的類型
     * @param <T> 泛型類型
     * @return 快取值，如果不存在則返回 null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 刪除快取
     * @param key 快取鍵
     * @return 是否成功刪除
     */
    Boolean delete(String key);

    /**
     * 批量刪除快取
     * @param keys 快取鍵集合
     * @return 成功刪除的數量
     */
    Long delete(Set<String> keys);

    /**
     * 檢查快取是否存在
     * @param key 快取鍵
     * @return 是否存在
     */
    Boolean exists(String key);

    /**
     * 設定快取過期時間
     * @param key 快取鍵
     * @param timeout 過期時間
     * @return 是否成功設定
     */
    Boolean expire(String key, Duration timeout);

    /**
     * 取得快取剩餘過期時間
     * @param key 快取鍵
     * @return 剩餘時間（秒），-1 表示永不過期，-2 表示鍵不存在
     */
    Long getExpire(String key);

    /**
     * 設定 Hash 快取
     * @param key 快取鍵
     * @param hashKey Hash 鍵
     * @param value 值
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * 取得 Hash 快取值
     * @param key 快取鍵
     * @param hashKey Hash 鍵
     * @return Hash 值
     */
    Object hGet(String key, String hashKey);

    /**
     * 刪除 Hash 快取
     * @param key 快取鍵
     * @param hashKeys Hash 鍵
     * @return 成功刪除的數量
     */
    Long hDelete(String key, String... hashKeys);

    /**
     * 檢查 Hash 鍵是否存在
     * @param key 快取鍵
     * @param hashKey Hash 鍵
     * @return 是否存在
     */
    Boolean hExists(String key, String hashKey);

    /**
     * 左推入列表
     * @param key 快取鍵
     * @param values 值列表
     * @return 列表長度
     */
    Long lLeftPush(String key, Object... values);

    /**
     * 右推入列表
     * @param key 快取鍵
     * @param values 值列表
     * @return 列表長度
     */
    Long lRightPush(String key, Object... values);

    /**
     * 左彈出列表
     * @param key 快取鍵
     * @return 彈出的值
     */
    Object lLeftPop(String key);

    /**
     * 右彈出列表
     * @param key 快取鍵
     * @return 彈出的值
     */
    Object lRightPop(String key);

    /**
     * 取得列表範圍內的元素
     * @param key 快取鍵
     * @param start 開始索引
     * @param end 結束索引
     * @return 元素列表
     */
    List<Object> lRange(String key, long start, long end);

    /**
     * 取得列表長度
     * @param key 快取鍵
     * @return 列表長度
     */
    Long lSize(String key);

    /**
     * 添加集合元素
     * @param key 快取鍵
     * @param values 值列表
     * @return 成功添加的數量
     */
    Long sAdd(String key, Object... values);

    /**
     * 移除集合元素
     * @param key 快取鍵
     * @param values 值列表
     * @return 成功移除的數量
     */
    Long sRemove(String key, Object... values);

    /**
     * 檢查集合是否包含元素
     * @param key 快取鍵
     * @param value 值
     * @return 是否包含
     */
    Boolean sIsMember(String key, Object value);

    /**
     * 取得集合所有元素
     * @param key 快取鍵
     * @return 元素集合
     */
    Set<Object> sMembers(String key);

    /**
     * 取得集合大小
     * @param key 快取鍵
     * @return 集合大小
     */
    Long sSize(String key);

    /**
     * 根據模式搜尋鍵
     * @param pattern 搜尋模式
     * @return 符合的鍵集合
     */
    Set<String> keys(String pattern);

    /**
     * 清空指定模式的快取
     * @param pattern 快取鍵模式
     * @return 清空的數量
     */
    Long clearByPattern(String pattern);
}
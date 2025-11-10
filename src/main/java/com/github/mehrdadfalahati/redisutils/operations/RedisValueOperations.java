package com.github.mehrdadfalahati.redisutils.operations;

import com.github.mehrdadfalahati.redisutils.core.RedisKey;

import java.util.Map;

/**
 * Operations on Redis strings (simple key-value).
 * Supports storing any serializable object as a value.
 */
public interface RedisValueOperations {

    /**
     * Set value for key with optional expiration.
     * @param key the Redis key (with optional TTL)
     * @param value the value to store
     */
    void set(RedisKey key, Object value);

    /**
     * Set value only if key does not exist.
     * @param key the Redis key (with optional TTL)
     * @param value the value to store
     * @return true if value was set, false if key already exists
     */
    boolean setIfAbsent(RedisKey key, Object value);

    /**
     * Set value only if key already exists.
     * @param key the Redis key (with optional TTL)
     * @param value the value to store
     * @return true if value was set, false if key doesn't exist
     */
    boolean setIfPresent(RedisKey key, Object value);

    /**
     * Get value for key.
     * @param key the Redis key
     * @param type the expected value type
     * @return the value, or null if key doesn't exist
     */
    <T> T get(String key, Class<T> type);

    /**
     * Get and delete key atomically.
     * @param key the Redis key
     * @param type the expected value type
     * @return the value, or null if key doesn't exist
     */
    <T> T getAndDelete(String key, Class<T> type);

    /**
     * Set value and return old value atomically.
     * @param key the Redis key (with optional TTL)
     * @param value the new value to store
     * @param type the expected old value type
     * @return the old value, or null if key didn't exist
     */
    <T> T getAndSet(RedisKey key, Object value, Class<T> type);

    /**
     * Get multiple values at once.
     * @param type the expected value type
     * @param keys the keys to get
     * @return map of key to value (null values for missing keys)
     */
    <T> Map<String, T> multiGet(Class<T> type, String... keys);

    /**
     * Set multiple key-value pairs at once.
     * @param keyValues map of RedisKey to value
     */
    void multiSet(Map<RedisKey, Object> keyValues);

    /**
     * Increment numeric value.
     * @param key the Redis key
     * @return the value after increment
     */
    long increment(String key);

    /**
     * Increment numeric value by delta.
     * @param key the Redis key
     * @param delta the increment amount
     * @return the value after increment
     */
    long incrementBy(String key, long delta);

    /**
     * Decrement numeric value.
     * @param key the Redis key
     * @return the value after decrement
     */
    long decrement(String key);
}

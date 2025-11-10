package com.github.mehrdadfalahati.redisutils.operations;

import com.github.mehrdadfalahati.redisutils.core.RedisKey;

/**
 * Extended string operations interface for Redis.
 * Provides additional string-specific operations beyond basic value operations.
 * <p>
 * This interface extends the basic value operations with Redis string commands
 * like append, substring, bitwise operations, etc.
 */
public interface RedisStringOperations extends RedisValueOperations {

    /**
     * Append a value to a key. If key doesn't exist, creates it.
     * @param key the Redis key
     * @param value the value to append
     * @return the length of the string after append
     */
    long append(String key, String value);

    /**
     * Get a substring of the value stored at key.
     * @param key the Redis key
     * @param start start offset (0-based, inclusive)
     * @param end end offset (inclusive, -1 means end of string)
     * @return the substring
     */
    String getRange(String key, long start, long end);

    /**
     * Overwrite part of a string at key starting at the specified offset.
     * @param key the Redis key
     * @param offset the offset to start writing at
     * @param value the value to write
     * @return the length of the string after modification
     */
    long setRange(String key, long offset, String value);

    /**
     * Get the length of the value stored at key.
     * @param key the Redis key
     * @return the length, or 0 if key doesn't exist
     */
    long strlen(String key);

    /**
     * Set multiple keys atomically.
     * Unlike multiSet, this uses MSET which is atomic.
     * @param keyValues map of RedisKey to value
     */
    void atomicMultiSet(java.util.Map<RedisKey, Object> keyValues);

    /**
     * Set multiple keys only if none of them exist (atomic operation).
     * @param keyValues map of RedisKey to value
     * @return true if all keys were set, false if any key already existed
     */
    boolean multiSetIfAbsent(java.util.Map<RedisKey, Object> keyValues);
}

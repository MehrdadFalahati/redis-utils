package com.github.mehrdadfalahati.redisutils.operations;

import java.time.Duration;
import java.util.Set;

/**
 * Operations on Redis keys (key management).
 */
public interface RedisKeyOperations {

    /**
     * Check if key exists.
     * @param key the key to check
     * @return true if key exists, false otherwise
     */
    boolean exists(String key);

    /**
     * Delete one or more keys.
     * @param keys the keys to delete
     * @return number of keys deleted
     */
    long delete(String... keys);

    /**
     * Set expiration on key.
     * @param key the key
     * @param duration the expiration duration
     * @return true if expiration was set, false if key doesn't exist
     */
    boolean expire(String key, Duration duration);

    /**
     * Get time-to-live for key.
     * @param key the key
     * @return TTL, or null if key has no expiration or doesn't exist
     */
    Duration ttl(String key);

    /**
     * Remove expiration from key.
     * @param key the key
     * @return true if expiration was removed, false otherwise
     */
    boolean persist(String key);

    /**
     * Find all keys matching pattern.
     * WARNING: Use with caution in production (blocking operation)
     * @param pattern the pattern (e.g., "user:*")
     * @return set of matching keys
     */
    Set<String> keys(String pattern);
}

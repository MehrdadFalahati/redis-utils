package com.github.mehrdadfalahati.redisutils.client;

import com.github.mehrdadfalahati.redisutils.operations.*;

/**
 * Unified Redis client interface abstracting the underlying implementation.
 * Provides access to all Redis operations through a consistent API.
 * <p>
 * This interface hides the complexity of connection management and
 * allows switching between different Redis clients (Lettuce, Jedis) transparently.
 * <p>
 * Usage:
 * <pre>{@code
 * @Autowired
 * private RedisClient redisClient;
 *
 * public void example() {
 *     RedisValueOperations ops = redisClient.valueOps();
 *     ops.set(RedisKey.of("user:1"), user);
 * }
 * }</pre>
 */
public interface RedisClient {

    /**
     * Get operations for Redis keys (key management).
     * @return key operations instance
     */
    RedisKeyOperations keyOps();

    /**
     * Get operations for Redis strings (simple key-value).
     * Alias for opsForValue().
     * @return value operations instance
     */
    RedisValueOperations valueOps();

    /**
     * Get operations for Redis values (strings).
     * @return value operations instance
     */
    default RedisValueOperations opsForValue() {
        return valueOps();
    }

    /**
     * Get operations for Redis hashes.
     * @return hash operations instance
     */
    RedisHashOperations opsForHash();

    /**
     * Get operations for Redis lists.
     * @return list operations instance
     */
    RedisListOperations opsForList();

    /**
     * Get operations for Redis sets.
     * @return set operations instance
     */
    RedisSetOperations opsForSet();

    /**
     * Get operations for Redis sorted sets (ZSets).
     * @return sorted set operations instance
     */
    RedisZSetOperations opsForZSet();

    /**
     * Check if the client is connected and healthy.
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Close the client and release all resources.
     * Should be called on application shutdown.
     */
    void close();

    /**
     * Execute a custom operation with access to the underlying connection.
     * This is an escape hatch for advanced use cases not covered by the API.
     * <p>
     * WARNING: Use with caution. Direct access breaks abstraction.
     *
     * @param callback the operation to execute
     * @param <T> the return type
     * @return the result of the callback
     */
    <T> T executeCommand(RedisCommandCallback<T> callback);

    /**
     * Callback interface for executing custom Redis commands.
     * @param <T> the return type
     */
    @FunctionalInterface
    interface RedisCommandCallback<T> {
        /**
         * Execute a command with the given connection.
         * @param connection the Redis connection (implementation-specific)
         * @return the result
         */
        T doInRedis(Object connection);
    }
}

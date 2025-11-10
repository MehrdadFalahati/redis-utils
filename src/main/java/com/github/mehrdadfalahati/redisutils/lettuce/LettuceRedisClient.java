package com.github.mehrdadfalahati.redisutils.lettuce;

import com.github.mehrdadfalahati.redisutils.client.RedisClient;
import com.github.mehrdadfalahati.redisutils.exception.RedisConnectionException;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;

/**
 * Lettuce-based implementation of RedisClient.
 * Manages connection lifecycle and provides access to Redis operations.
 * <p>
 * This implementation uses Spring's RedisTemplate internally but provides
 * direct access to Lettuce for advanced use cases.
 */
@Slf4j
public class LettuceRedisClient implements RedisClient {

    private final RedisKeyOperations keyOperations;
    private final RedisValueOperations valueOperations;
    private final StatefulRedisConnection<String, Object> connection;
    private volatile boolean closed = false;

    public LettuceRedisClient(
            RedisKeyOperations keyOperations,
            RedisValueOperations valueOperations,
            StatefulRedisConnection<String, Object> connection) {
        this.keyOperations = keyOperations;
        this.valueOperations = valueOperations;
        this.connection = connection;
    }

    @Override
    public RedisKeyOperations keyOps() {
        ensureNotClosed();
        return keyOperations;
    }

    @Override
    public RedisValueOperations valueOps() {
        ensureNotClosed();
        return valueOperations;
    }

    @Override
    public boolean isConnected() {
        if (closed || connection == null) {
            return false;
        }
        try {
            // Ping to verify connection
            String result = connection.sync().ping();
            return "PONG".equalsIgnoreCase(result);
        } catch (Exception e) {
            log.warn("Redis connection check failed", e);
            return false;
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (connection != null) {
                try {
                    connection.close();
                    log.info("Redis connection closed successfully");
                } catch (Exception e) {
                    log.error("Error closing Redis connection", e);
                }
            }
        }
    }

    @Override
    public <T> T executeCommand(RedisCommandCallback<T> callback) {
        ensureNotClosed();
        if (connection == null) {
            throw new RedisConnectionException("No Redis connection available");
        }
        try {
            return callback.doInRedis(connection.sync());
        } catch (Exception e) {
            throw new RedisConnectionException("Failed to execute custom Redis command", e);
        }
    }

    private void ensureNotClosed() {
        if (closed) {
            throw new IllegalStateException("RedisClient has been closed");
        }
    }
}

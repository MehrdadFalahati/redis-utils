package com.github.mehrdadfalahati.redisutils.util;

import com.github.mehrdadfalahati.redisutils.config.RedisProperties;
import com.github.mehrdadfalahati.redisutils.exception.RedisConnectionException;
import com.github.mehrdadfalahati.redisutils.exception.RedisException;
import com.github.mehrdadfalahati.redisutils.exception.RedisTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Utility for executing Redis commands with retry logic and timeout handling.
 * Provides a consistent way to handle transient failures.
 * Created by auto-configuration, not annotated with @Component.
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCommandExecutor {

    private final RedisProperties properties;

    /**
     * Execute a Redis command with retry and timeout handling.
     *
     * @param operation the operation name (for logging)
     * @param command the command to execute
     * @param <T> the return type
     * @return the result of the command
     * @throws RedisException if the command fails after all retries
     */
    public <T> T execute(String operation, Supplier<T> command) {
        RedisProperties.RetryConfig retry = properties.getRetry();

        if (!retry.isEnabled()) {
            return executeOnce(operation, command);
        }

        int attempts = 0;
        Duration backoff = retry.getInitialBackoff();
        Exception lastException = null;

        while (attempts < retry.getMaxAttempts()) {
            try {
                return executeOnce(operation, command);
            } catch (RedisTimeoutException e) {
                lastException = e;
                if (!retry.isRetryOnTimeout()) {
                    throw e;
                }
                log.warn("Redis operation '{}' timed out (attempt {}/{})",
                    operation, attempts + 1, retry.getMaxAttempts());
            } catch (RedisConnectionException e) {
                lastException = e;
                log.warn("Redis connection error on operation '{}' (attempt {}/{}): {}",
                    operation, attempts + 1, retry.getMaxAttempts(), e.getMessage());
            } catch (RedisException e) {
                // Don't retry other Redis exceptions (e.g., serialization, operation errors)
                throw e;
            }

            attempts++;
            if (attempts < retry.getMaxAttempts()) {
                sleep(backoff);
                backoff = Duration.ofMillis(
                    Math.min(
                        (long) (backoff.toMillis() * retry.getBackoffMultiplier()),
                        retry.getMaxBackoff().toMillis()
                    )
                );
            }
        }

        log.error("Redis operation '{}' failed after {} attempts", operation, attempts);
        throw new RedisException(
            String.format("Operation '%s' failed after %d attempts", operation, attempts),
            lastException
        );
    }

    /**
     * Execute a command without retry logic.
     */
    private <T> T executeOnce(String operation, Supplier<T> command) {
        try {
            return command.get();
        } catch (io.lettuce.core.RedisConnectionException e) {
            throw new RedisConnectionException(
                "Failed to connect to Redis for operation: " + operation, e
            );
        } catch (io.lettuce.core.RedisCommandTimeoutException e) {
            throw new RedisTimeoutException(
                "Redis operation '" + operation + "' timed out",
                e,
                properties.getTimeout().toMillis()
            );
        } catch (Exception e) {
            throw new RedisException(
                "Redis operation '" + operation + "' failed: " + e.getMessage(), e
            );
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisException("Interrupted while waiting for retry", e);
        }
    }
}

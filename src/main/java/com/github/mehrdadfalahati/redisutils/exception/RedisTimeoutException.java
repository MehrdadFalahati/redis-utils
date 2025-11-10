package com.github.mehrdadfalahati.redisutils.exception;

/**
 * Exception thrown when a Redis operation exceeds the configured timeout.
 * Indicates slow commands, network latency, or Redis overload.
 */
public class RedisTimeoutException extends RedisException {

    private final long timeoutMillis;

    public RedisTimeoutException(String message, long timeoutMillis) {
        super(message);
        this.timeoutMillis = timeoutMillis;
    }

    public RedisTimeoutException(String message, Throwable cause, long timeoutMillis) {
        super(message, cause);
        this.timeoutMillis = timeoutMillis;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}

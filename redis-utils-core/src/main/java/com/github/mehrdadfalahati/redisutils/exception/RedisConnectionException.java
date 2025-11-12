package com.github.mehrdadfalahati.redisutils.exception;

/**
 * Exception thrown when Redis connection fails or is lost.
 * Indicates network issues, authentication failures, or Redis unavailability.
 */
public class RedisConnectionException extends RedisException {

    public RedisConnectionException(String message) {
        super(message);
    }

    public RedisConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisConnectionException(Throwable cause) {
        super(cause);
    }
}

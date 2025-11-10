package com.github.mehrdadfalahati.redisutils.exception;

/**
 * Base unchecked exception for all Redis operations.
 * Wraps underlying client exceptions into a consistent hierarchy.
 */
public class RedisException extends RuntimeException {

    public RedisException(String message) {
        super(message);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisException(Throwable cause) {
        super(cause);
    }
}

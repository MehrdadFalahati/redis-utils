package com.github.mehrdadfalahati.redisutils.exception;

/**
 * Exception thrown when a Redis operation fails for reasons other than
 * connection, timeout, or serialization issues.
 * Examples: wrong data type, invalid arguments, Redis errors.
 */
public class RedisOperationException extends RedisException {

    private final String operation;

    public RedisOperationException(String operation, String message) {
        super(String.format("Redis operation '%s' failed: %s", operation, message));
        this.operation = operation;
    }

    public RedisOperationException(String operation, String message, Throwable cause) {
        super(String.format("Redis operation '%s' failed: %s", operation, message), cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}

package com.github.mehrdadfalahati.redisutils.exception;

/**
 * Exception thrown when serialization or deserialization of Redis values fails.
 * Indicates incompatible data types or corrupted data in Redis.
 */
public class RedisSerializationException extends RedisException {

    private final Class<?> targetType;

    public RedisSerializationException(String message, Class<?> targetType) {
        super(message);
        this.targetType = targetType;
    }

    public RedisSerializationException(String message, Throwable cause, Class<?> targetType) {
        super(message, cause);
        this.targetType = targetType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }
}

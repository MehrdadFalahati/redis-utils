package com.github.mehrdadfalahati.redisutils.serialization;

import com.github.mehrdadfalahati.redisutils.exception.RedisException;

/**
 * Exception thrown when serialization or deserialization fails.
 */
public class SerializationException extends RedisException {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super("Serialization failed", cause);
    }
}

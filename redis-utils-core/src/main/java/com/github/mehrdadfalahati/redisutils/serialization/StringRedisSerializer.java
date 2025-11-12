package com.github.mehrdadfalahati.redisutils.serialization;

import java.nio.charset.StandardCharsets;

/**
 * Serializer for String values using UTF-8 encoding.
 */
public class StringRedisSerializer implements RedisValueSerializer<String> {

    @Override
    public byte[] serialize(String value) throws SerializationException {
        if (value == null) {
            return null;
        }
        try {
            return value.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize string", e);
        }
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize string", e);
        }
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}

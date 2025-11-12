package com.github.mehrdadfalahati.redisutils.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON serializer using Jackson ObjectMapper.
 * Supports any POJO that can be serialized to/from JSON.
 *
 * @param <T> the type to serialize/deserialize
 */
public class JsonRedisSerializer<T> implements RedisValueSerializer<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public JsonRedisSerializer(ObjectMapper objectMapper, Class<T> type) {
        this.objectMapper = objectMapper;
        this.type = type;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object to JSON", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON to object", e);
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}

package com.github.mehrdadfalahati.redisutils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing per-type serializers.
 * Provides a strategy for selecting the appropriate serializer based on the value type.
 * Falls back to a default JSON serializer for types without a specific serializer.
 */
public class RedisSerializerRegistry {

    private final Map<Class<?>, RedisValueSerializer<?>> serializers = new ConcurrentHashMap<>();
    private final ObjectMapper defaultObjectMapper;

    public RedisSerializerRegistry(ObjectMapper defaultObjectMapper) {
        this.defaultObjectMapper = defaultObjectMapper;
        registerDefaultSerializers();
    }

    /**
     * Register default serializers for common types.
     */
    private void registerDefaultSerializers() {
        register(new StringRedisSerializer());
        register(new ByteArrayRedisSerializer());
    }

    /**
     * Register a serializer for a specific type.
     *
     * @param serializer the serializer to register
     * @param <T>        the type the serializer handles
     */
    public <T> void register(RedisValueSerializer<T> serializer) {
        serializers.put(serializer.getType(), serializer);
    }

    /**
     * Get a serializer for the given type.
     * If no specific serializer is registered, creates a JSON serializer.
     *
     * @param type the type to get a serializer for
     * @param <T>  the type parameter
     * @return a serializer for the type
     */
    @SuppressWarnings("unchecked")
    public <T> RedisValueSerializer<T> getSerializer(Class<T> type) {
        RedisValueSerializer<?> serializer = serializers.get(type);
        if (serializer != null) {
            return (RedisValueSerializer<T>) serializer;
        }

        // Create and cache a JSON serializer for this type
        JsonRedisSerializer<T> jsonSerializer = new JsonRedisSerializer<>(defaultObjectMapper, type);
        serializers.put(type, jsonSerializer);
        return jsonSerializer;
    }

    /**
     * Check if a serializer is registered for the given type.
     *
     * @param type the type to check
     * @return true if a serializer is registered
     */
    public boolean hasSerializer(Class<?> type) {
        return serializers.containsKey(type);
    }

    /**
     * Remove a serializer for a specific type.
     *
     * @param type the type to remove the serializer for
     */
    public void unregister(Class<?> type) {
        serializers.remove(type);
    }

    /**
     * Clear all registered serializers except defaults.
     */
    public void clear() {
        serializers.clear();
        registerDefaultSerializers();
    }
}

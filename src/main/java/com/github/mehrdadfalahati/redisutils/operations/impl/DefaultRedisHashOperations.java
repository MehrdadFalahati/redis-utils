package com.github.mehrdadfalahati.redisutils.operations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutils.operations.RedisHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of RedisHashOperations using Spring's RedisTemplate.
 */
public class DefaultRedisHashOperations implements RedisHashOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DefaultRedisHashOperations(RedisTemplate<String, Object> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void put(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public void putAll(String key, Map<String, Object> values) {
        redisTemplate.opsForHash().putAll(key, values);
    }

    @Override
    public boolean putIfAbsent(String key, String field, Object value) {
        Boolean result = redisTemplate.opsForHash().putIfAbsent(key, field, value);
        return result != null && result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, String field, Class<T> type) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) {
            return null;
        }

        // If already correct type, return directly
        if (type.isInstance(value)) {
            return (T) value;
        }

        // Convert using ObjectMapper
        return objectMapper.convertValue(value, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> multiGet(String key, List<String> fields, Class<T> type) {
        List<Object> values = redisTemplate.opsForHash().multiGet(key, (List) fields);

        Map<String, T> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            Object value = values.get(i);
            if (value == null) {
                result.put(field, null);
            } else if (type.isInstance(value)) {
                result.put(field, (T) value);
            } else {
                result.put(field, objectMapper.convertValue(value, type));
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> entries(String key, Class<T> type) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> {
                            Object value = entry.getValue();
                            if (type.isInstance(value)) {
                                return (T) value;
                            }
                            return objectMapper.convertValue(value, type);
                        }
                ));
    }

    @Override
    public Set<String> keys(String key) {
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        return keys.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> values(String key, Class<T> type) {
        List<Object> values = redisTemplate.opsForHash().values(key);

        return values.stream()
                .map(value -> {
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                    return objectMapper.convertValue(value, type);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasKey(String key, String field) {
        Boolean result = redisTemplate.opsForHash().hasKey(key, field);
        return result != null && result;
    }

    @Override
    public long delete(String key, String... fields) {
        Long result = redisTemplate.opsForHash().delete(key, (Object[]) fields);
        return result != null ? result : 0L;
    }

    @Override
    public long size(String key) {
        Long result = redisTemplate.opsForHash().size(key);
        return result != null ? result : 0L;
    }

    @Override
    public long increment(String key, String field, long delta) {
        Long result = redisTemplate.opsForHash().increment(key, field, delta);
        return result != null ? result : 0L;
    }

    @Override
    public double increment(String key, String field, double delta) {
        Double result = redisTemplate.opsForHash().increment(key, field, delta);
        return result != null ? result : 0.0;
    }
}

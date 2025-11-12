package com.github.mehrdadfalahati.redisutils.operations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutils.operations.RedisListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of RedisListOperations using Spring's RedisTemplate.
 */
public class DefaultRedisListOperations implements RedisListOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DefaultRedisListOperations(RedisTemplate<String, Object> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public long leftPush(String key, Object... values) {
        Long result = redisTemplate.opsForList().leftPushAll(key, values);
        return result != null ? result : 0L;
    }

    @Override
    public long leftPushIfPresent(String key, Object value) {
        Long result = redisTemplate.opsForList().leftPushIfPresent(key, value);
        return result != null ? result : 0L;
    }

    @Override
    public long rightPush(String key, Object... values) {
        Long result = redisTemplate.opsForList().rightPushAll(key, values);
        return result != null ? result : 0L;
    }

    @Override
    public long rightPushIfPresent(String key, Object value) {
        Long result = redisTemplate.opsForList().rightPushIfPresent(key, value);
        return result != null ? result : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T leftPop(String key, Class<T> type) {
        Object value = redisTemplate.opsForList().leftPop(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        return objectMapper.convertValue(value, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T rightPop(String key, Class<T> type) {
        Object value = redisTemplate.opsForList().rightPop(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        return objectMapper.convertValue(value, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> range(String key, long start, long end, Class<T> type) {
        List<Object> values = redisTemplate.opsForList().range(key, start, end);
        if (values == null || values.isEmpty()) {
            return List.of();
        }

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
    public void trim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    @Override
    public long size(String key) {
        Long result = redisTemplate.opsForList().size(key);
        return result != null ? result : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T index(String key, long index, Class<T> type) {
        Object value = redisTemplate.opsForList().index(key, index);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        return objectMapper.convertValue(value, type);
    }

    @Override
    public void set(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    @Override
    public long remove(String key, long count, Object value) {
        Long result = redisTemplate.opsForList().remove(key, count, value);
        return result != null ? result : 0L;
    }
}

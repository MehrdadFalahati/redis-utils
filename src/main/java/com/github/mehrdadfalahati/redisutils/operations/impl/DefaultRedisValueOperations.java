package com.github.mehrdadfalahati.redisutils.operations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutils.core.RedisKey;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of RedisValueOperations.
 * Note: Not annotated with @Component as it's created by auto-configuration.
 */
@RequiredArgsConstructor
public class DefaultRedisValueOperations implements RedisValueOperations {

    protected final RedisTemplate<String, Object> redisTemplate;
    protected final ObjectMapper objectMapper;

    @Override
    public void set(RedisKey key, Object value) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        if (key.hasExpiration()) {
            ops.set(key.key(), value, key.timeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        } else {
            ops.set(key.key(), value);
        }
    }

    @Override
    public boolean setIfAbsent(RedisKey key, Object value) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Boolean result;
        if (key.hasExpiration()) {
            result = ops.setIfAbsent(key.key(), value, key.timeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        } else {
            result = ops.setIfAbsent(key.key(), value);
        }
        return result != null && result;
    }

    @Override
    public boolean setIfPresent(RedisKey key, Object value) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Boolean result;
        if (key.hasExpiration()) {
            result = ops.setIfPresent(key.key(), value, key.timeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        } else {
            result = ops.setIfPresent(key.key(), value);
        }
        return result != null && result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // Value is already deserialized by RedisSerializer
        if (type.isInstance(value)) {
            return (T) value;
        }
        // If types don't match, try to convert using ObjectMapper
        // This handles cases where GenericJackson2JsonRedisSerializer returns LinkedHashMap
        return objectMapper.convertValue(value, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAndDelete(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().getAndDelete(key);
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
    public <T> T getAndSet(RedisKey key, Object value, Class<T> type) {
        Object oldValue = redisTemplate.opsForValue().getAndSet(key.key(), value);
        if (oldValue == null) {
            return null;
        }

        // Set expiration if specified
        if (key.hasExpiration()) {
            redisTemplate.expire(key.key(), key.timeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }

        if (type.isInstance(oldValue)) {
            return (T) oldValue;
        }
        return objectMapper.convertValue(oldValue, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> multiGet(Class<T> type, String... keys) {
        if (keys == null || keys.length == 0) {
            return Map.of();
        }

        List<Object> values = redisTemplate.opsForValue().multiGet(Arrays.asList(keys));
        Map<String, T> result = new HashMap<>();

        for (int i = 0; i < keys.length; i++) {
            Object value = values != null ? values.get(i) : null;
            if (value != null) {
                if (type.isInstance(value)) {
                    result.put(keys[i], (T) value);
                } else {
                    result.put(keys[i], objectMapper.convertValue(value, type));
                }
            }
        }

        return result;
    }

    @Override
    public void multiSet(Map<RedisKey, Object> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return;
        }

        // Convert RedisKey to String for multiSet
        Map<String, Object> stringMap = new HashMap<>();
        for (Map.Entry<RedisKey, Object> entry : keyValues.entrySet()) {
            stringMap.put(entry.getKey().key(), entry.getValue());
        }

        redisTemplate.opsForValue().multiSet(stringMap);

        // Set expiration for keys that require it
        for (RedisKey key : keyValues.keySet()) {
            if (key.hasExpiration()) {
                redisTemplate.expire(key.key(), key.timeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public long increment(String key) {
        Long result = redisTemplate.opsForValue().increment(key);
        return result != null ? result : 0L;
    }

    @Override
    public long incrementBy(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0L;
    }

    @Override
    public long decrement(String key) {
        Long result = redisTemplate.opsForValue().decrement(key);
        return result != null ? result : 0L;
    }

    @Override
    public long decrementBy(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0L;
    }
}

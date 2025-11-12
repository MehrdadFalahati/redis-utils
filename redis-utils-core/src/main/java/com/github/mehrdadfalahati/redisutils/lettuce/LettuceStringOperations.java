package com.github.mehrdadfalahati.redisutils.lettuce;

import com.github.mehrdadfalahati.redisutils.core.RedisKey;
import com.github.mehrdadfalahati.redisutils.operations.impl.DefaultRedisValueOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisStringOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Lettuce-based implementation of RedisStringOperations.
 * Extends the default value operations with string-specific commands.
 * Created by auto-configuration, not annotated with @Component.
 */
public class LettuceStringOperations extends DefaultRedisValueOperations implements RedisStringOperations {

    public LettuceStringOperations(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    public long append(String key, String value) {
        Integer result = redisTemplate.opsForValue().append(key, value);
        return result != null ? result.longValue() : 0L;
    }

    @Override
    public String getRange(String key, long start, long end) {
        return redisTemplate.opsForValue().get(key, start, end);
    }

    @Override
    public long setRange(String key, long offset, String value) {
        redisTemplate.opsForValue().set(key, value, offset);
        // Redis SETRANGE returns the length of the string after modification
        Long length = redisTemplate.opsForValue().size(key);
        return length != null ? length : 0L;
    }

    @Override
    public long strlen(String key) {
        Long length = redisTemplate.opsForValue().size(key);
        return length != null ? length : 0L;
    }

    @Override
    public void atomicMultiSet(Map<RedisKey, Object> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return;
        }

        // Convert to string map for MSET
        Map<String, Object> stringMap = new HashMap<>();
        for (Map.Entry<RedisKey, Object> entry : keyValues.entrySet()) {
            stringMap.put(entry.getKey().key(), entry.getValue());
        }

        // MSET is atomic
        redisTemplate.opsForValue().multiSet(stringMap);

        // Note: MSET doesn't support TTL, so we set expirations separately
        // This makes the overall operation non-atomic for TTL
        for (RedisKey key : keyValues.keySet()) {
            if (key.hasExpiration()) {
                redisTemplate.expire(key.key(), key.ttl());
            }
        }
    }

    @Override
    public boolean multiSetIfAbsent(Map<RedisKey, Object> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return false;
        }

        // Convert to string map
        Map<String, Object> stringMap = new HashMap<>();
        for (Map.Entry<RedisKey, Object> entry : keyValues.entrySet()) {
            stringMap.put(entry.getKey().key(), entry.getValue());
        }

        // MSETNX is atomic - sets all or nothing
        Boolean result = redisTemplate.opsForValue().multiSetIfAbsent(stringMap);

        // If successful and any keys have TTL, set them
        if (Boolean.TRUE.equals(result)) {
            for (RedisKey key : keyValues.keySet()) {
                if (key.hasExpiration()) {
                    redisTemplate.expire(key.key(), key.ttl());
                }
            }
        }

        return Boolean.TRUE.equals(result);
    }
}

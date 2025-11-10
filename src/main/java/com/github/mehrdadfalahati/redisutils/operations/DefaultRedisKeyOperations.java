package com.github.mehrdadfalahati.redisutils.operations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of RedisKeyOperations.
 * Note: Not annotated with @Component as it's created by auto-configuration.
 */
@RequiredArgsConstructor
public class DefaultRedisKeyOperations implements RedisKeyOperations {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public long delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }
        return redisTemplate.delete(Set.of(keys));
    }

    @Override
    public boolean expire(String key, Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return redisTemplate.expire(key, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Duration ttl(String key) {
        Long ttlMillis = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        if (ttlMillis == null || ttlMillis < 0) {
            return null; // No expiration or key doesn't exist
        }
        return Duration.ofMillis(ttlMillis);
    }

    @Override
    public boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}

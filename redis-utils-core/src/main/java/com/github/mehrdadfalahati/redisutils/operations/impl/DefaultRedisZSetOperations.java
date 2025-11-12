package com.github.mehrdadfalahati.redisutils.operations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutils.operations.RedisZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of RedisZSetOperations using Spring's RedisTemplate.
 */
public class DefaultRedisZSetOperations implements RedisZSetOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DefaultRedisZSetOperations(RedisTemplate<String, Object> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean add(String key, Object value, double score) {
        Boolean result = redisTemplate.opsForZSet().add(key, value, score);
        return result != null && result;
    }

    @Override
    public long remove(String key, Object... members) {
        Long result = redisTemplate.opsForZSet().remove(key, members);
        return result != null ? result : 0L;
    }

    @Override
    public double incrementScore(String key, Object value, double delta) {
        Double result = redisTemplate.opsForZSet().incrementScore(key, value, delta);
        return result != null ? result : 0.0;
    }

    @Override
    public Double score(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    @Override
    public Long rank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    @Override
    public Long reverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> range(String key, long start, long end, Class<T> type) {
        Set<Object> values = redisTemplate.opsForZSet().range(key, start, end);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return values.stream()
                .map(value -> {
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                    return objectMapper.convertValue(value, type);
                })
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> reverseRange(String key, long start, long end, Class<T> type) {
        Set<Object> values = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return values.stream()
                .map(value -> {
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                    return objectMapper.convertValue(value, type);
                })
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> rangeByScore(String key, double minScore, double maxScore, Class<T> type) {
        Set<Object> values = redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return values.stream()
                .map(value -> {
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                    return objectMapper.convertValue(value, type);
                })
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> reverseRangeByScore(String key, double minScore, double maxScore, Class<T> type) {
        Set<Object> values = redisTemplate.opsForZSet().reverseRangeByScore(key, minScore, maxScore);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return values.stream()
                .map(value -> {
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                    return objectMapper.convertValue(value, type);
                })
                .collect(Collectors.toSet());
    }

    @Override
    public long size(String key) {
        Long result = redisTemplate.opsForZSet().size(key);
        return result != null ? result : 0L;
    }

    @Override
    public long count(String key, double minScore, double maxScore) {
        Long result = redisTemplate.opsForZSet().count(key, minScore, maxScore);
        return result != null ? result : 0L;
    }

    @Override
    public long removeRange(String key, long start, long end) {
        Long result = redisTemplate.opsForZSet().removeRange(key, start, end);
        return result != null ? result : 0L;
    }

    @Override
    public long removeRangeByScore(String key, double minScore, double maxScore) {
        Long result = redisTemplate.opsForZSet().removeRangeByScore(key, minScore, maxScore);
        return result != null ? result : 0L;
    }
}

package com.github.mehrdadfalahati.redisutils.operations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutils.operations.RedisSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of RedisSetOperations using Spring's RedisTemplate.
 */
public class DefaultRedisSetOperations implements RedisSetOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DefaultRedisSetOperations(RedisTemplate<String, Object> redisTemplate,
                                     ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public long add(String key, Object... members) {
        Long result = redisTemplate.opsForSet().add(key, members);
        return result != null ? result : 0L;
    }

    @Override
    public long remove(String key, Object... members) {
        Long result = redisTemplate.opsForSet().remove(key, members);
        return result != null ? result : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T pop(String key, Class<T> type) {
        Object value = redisTemplate.opsForSet().pop(key);
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
    public <T> List<T> pop(String key, long count, Class<T> type) {
        List<Object> values = redisTemplate.opsForSet().pop(key, count);
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
    @SuppressWarnings("unchecked")
    public <T> Set<T> members(String key, Class<T> type) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return Set.of();
        }

        return members.stream()
                .map(value -> {
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                    return objectMapper.convertValue(value, type);
                })
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isMember(String key, Object member) {
        Boolean result = redisTemplate.opsForSet().isMember(key, member);
        return result != null && result;
    }

    @Override
    public long size(String key) {
        Long result = redisTemplate.opsForSet().size(key);
        return result != null ? result : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T randomMember(String key, Class<T> type) {
        Object value = redisTemplate.opsForSet().randomMember(key);
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
    public <T> List<T> randomMembers(String key, long count, Class<T> type) {
        List<Object> values = redisTemplate.opsForSet().randomMembers(key, count);
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
    @SuppressWarnings("unchecked")
    public <T> Set<T> distinctRandomMembers(String key, long count, Class<T> type) {
        Set<Object> values = redisTemplate.opsForSet().distinctRandomMembers(key, count);
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
    public <T> Set<T> difference(List<String> keys, Class<T> type) {
        Set<Object> values = redisTemplate.opsForSet().difference(keys);
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
    public <T> Set<T> intersect(List<String> keys, Class<T> type) {
        Set<Object> values = redisTemplate.opsForSet().intersect(keys);
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
    public <T> Set<T> union(List<String> keys, Class<T> type) {
        Set<Object> values = redisTemplate.opsForSet().union(keys);
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
}

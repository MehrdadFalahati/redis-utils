package com.github.mehrdadfalahati.redisutills.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutills.service.RedisDto;
import com.github.mehrdadfalahati.redisutills.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component("redisService")
@RequiredArgsConstructor
public class RedisServiceImpl<K, V> implements RedisService<K, V> {

    private final RedisTemplate<K, V> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void setIfAbsent(RedisDto<K> redisDto, V value) {
        getOpsForValue().setIfAbsent(redisDto.key(), value, redisDto.timeout(), redisDto.timeUnit());
    }

    @Override
    public void set(RedisDto<K> redisDto, V value) {
        getOpsForValue().set(redisDto.key(), value, redisDto.timeout(), redisDto.timeUnit());
    }

    @Override
    public V get(K key, TypeReference<V> typeReference) {
        return objectMapper.convertValue(getOpsForValue().get(key), typeReference);
    }

    @Override
    public void delete(K key) {
        redisTemplate.delete(key);
    }

    @Override
    public Boolean hasKey(K key) {
        return redisTemplate.hasKey(key);
    }

    private ValueOperations<K, V> getOpsForValue() {
        return redisTemplate.opsForValue();
    }
}
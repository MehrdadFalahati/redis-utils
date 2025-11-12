package com.github.mehrdadfalahati.redisutils;

import com.github.mehrdadfalahati.redisutils.config.RedisClientAutoConfiguration;
import com.github.mehrdadfalahati.redisutils.operations.*;
import com.github.mehrdadfalahati.redisutils.lettuce.LettuceStringOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mehrdadfalahati.redisutils.operations.impl.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication(exclude = {RedisClientAutoConfiguration.class})
public class RedisTestConfiguration {

    @Bean
    public RedisKeyOperations redisKeyOperations(RedisTemplate<String, Object> redisTemplate) {
        return new DefaultRedisKeyOperations(redisTemplate);
    }

    @Bean
    public RedisValueOperations redisValueOperations(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new LettuceStringOperations(redisTemplate, objectMapper);
    }

    @Bean
    public RedisHashOperations redisHashOperations(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new DefaultRedisHashOperations(redisTemplate, objectMapper);
    }

    @Bean
    public RedisListOperations redisListOperations(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new DefaultRedisListOperations(redisTemplate, objectMapper);
    }

    @Bean
    public RedisSetOperations redisSetOperations(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new DefaultRedisSetOperations(redisTemplate, objectMapper);
    }

    @Bean
    public RedisZSetOperations redisZSetOperations(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new DefaultRedisZSetOperations(redisTemplate, objectMapper);
    }
}

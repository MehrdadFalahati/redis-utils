package com.github.mehrdadfalahati.redisutils;

import com.github.mehrdadfalahati.redisutils.config.RedisClientAutoConfiguration;
import com.github.mehrdadfalahati.redisutils.operations.DefaultRedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import com.github.mehrdadfalahati.redisutils.lettuce.LettuceStringOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}

package com.github.mehrdadfalahati.redisutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mehrdadfalahati.redisutils.lettuce.LettuceStringOperations;
import com.github.mehrdadfalahati.redisutils.operations.*;
import com.github.mehrdadfalahati.redisutils.operations.impl.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@SpringBootApplication
public class RedisTestConfiguration {

    @Bean
    public ObjectMapper redisObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public RedisSerializer<Object> redisSerializer(ObjectMapper redisObjectMapper) {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            RedisSerializer<Object> redisSerializer) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serialization: always String
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // Value serialization: JSON
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, byte[]> byteArrayRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serialization: String
        template.setKeySerializer(RedisSerializer.string());

        // Value serialization: byte array (pass-through)
        template.setValueSerializer(RedisSerializer.byteArray());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisKeyOperations redisKeyOperations(RedisTemplate<String, Object> redisTemplate) {
        return new DefaultRedisKeyOperations(redisTemplate);
    }

    @Bean
    public RedisStringOperations redisStringOperations(
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

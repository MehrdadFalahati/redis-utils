package com.github.mehrdadfalahati.redisutils.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Default serialization configuration for Redis values.
 * Provides a JSON serializer with Java 8 time support.
 */
@Configuration
public class RedisSerializationConfiguration {

    /**
     * ObjectMapper configured for Redis serialization.
     * - Supports Java 8+ date/time types (Instant, LocalDateTime, etc.)
     * - ISO-8601 date format (not timestamps)
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Default JSON serializer for Redis values.
     * Uses GenericJackson2JsonRedisSerializer to preserve type information.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisSerializer<Object> redisSerializer(ObjectMapper redisObjectMapper) {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }
}

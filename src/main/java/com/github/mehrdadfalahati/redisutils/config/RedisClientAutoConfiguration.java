package com.github.mehrdadfalahati.redisutils.config;

import com.github.mehrdadfalahati.redisutils.client.RedisClient;
import com.github.mehrdadfalahati.redisutils.client.RedisConnectionManager;
import com.github.mehrdadfalahati.redisutils.lettuce.LettuceRedisClient;
import com.github.mehrdadfalahati.redisutils.lettuce.LettuceStringOperations;
import com.github.mehrdadfalahati.redisutils.operations.DefaultRedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import com.github.mehrdadfalahati.redisutils.util.RedisCommandExecutor;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Auto-configuration for Redis client and operations.
 * Automatically configures Redis beans when Lettuce is on the classpath.
 * <p>
 * This configuration runs after Spring Boot's RedisAutoConfiguration,
 * allowing it to use the configured connection factory.
 * <p>
 * To customize, provide your own beans with the same names.
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({io.lettuce.core.RedisClient.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisClientAutoConfiguration {

    /**
     * Create the connection manager for lifecycle management.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionManager redisConnectionManager(
            RedisConnectionFactory connectionFactory,
            RedisProperties properties) {
        log.info("Initializing Redis connection manager");
        return new RedisConnectionManager(connectionFactory, properties);
    }

    /**
     * Create Redis key operations.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisKeyOperations redisKeyOperations(
            org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        log.info("Creating DefaultRedisKeyOperations");
        return new DefaultRedisKeyOperations(redisTemplate);
    }

    /**
     * Create the unified Redis client facade.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisClient redisClient(
            RedisKeyOperations keyOperations,
            RedisValueOperations valueOperations,
            RedisConnectionManager connectionManager) {
        log.info("Creating LettuceRedisClient");

        StatefulRedisConnection<String, Object> connection = connectionManager.getConnection();
        return new LettuceRedisClient(keyOperations, valueOperations, connection);
    }

    /**
     * Create Redis string operations (extended value operations).
     * This bean is also available as RedisValueOperations.
     */
    @Bean(name = {"redisStringOperations", "redisValueOperations"})
    @ConditionalOnMissingBean(name = "redisValueOperations")
    public LettuceStringOperations redisStringOperations(
            org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        log.info("Creating LettuceStringOperations (also available as RedisValueOperations)");
        return new LettuceStringOperations(redisTemplate, objectMapper);
    }

    /**
     * Create the command executor for retry logic.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisCommandExecutor redisCommandExecutor(RedisProperties properties) {
        log.info("Creating RedisCommandExecutor with retry config: enabled={}, maxAttempts={}",
            properties.getRetry().isEnabled(), properties.getRetry().getMaxAttempts());
        return new RedisCommandExecutor(properties);
    }
}

package com.github.mehrdadfalahati.redisutils.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for Redis client.
 * Binds to application.yml/properties with prefix "redis.client".
 *
 * <p>This class wraps the core {@link RedisProperties} and adds Spring Boot's
 * {@code @ConfigurationProperties} annotation for automatic property binding.
 *
 * <p>Example application.yml:
 * <pre>
 * redis:
 *   client:
 *     enabled: true
 *     timeout: 5s
 *     pool:
 *       max-total: 8
 *     retry:
 *       enabled: true
 *       max-attempts: 3
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "redis.client")
public class RedisClientProperties extends RedisProperties {
    // Inherits all properties from core RedisProperties
    // Spring Boot automatically binds properties from application.yml/properties
}

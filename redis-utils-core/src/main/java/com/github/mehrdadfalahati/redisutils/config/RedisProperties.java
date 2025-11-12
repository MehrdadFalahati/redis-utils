package com.github.mehrdadfalahati.redisutils.config;

import lombok.Data;

import java.time.Duration;

/**
 * Configuration properties for Redis client.
 * This is a plain POJO without Spring Boot dependencies for use in redis-utils-core.
 * <p>
 * For Spring Boot applications, use the @ConfigurationProperties version in redis-utils-spring-boot-starter.
 */
@Data
public class RedisProperties {

    /**
     * Enable Redis client auto-configuration. Default: true.
     */
    private boolean enabled = true;

    /**
     * Default command timeout. Default: 5 seconds.
     */
    private Duration timeout = Duration.ofSeconds(5);

    /**
     * Enable connection pooling. Default: true.
     */
    private boolean poolEnabled = true;

    /**
     * Pool configuration.
     */
    private PoolConfig pool = new PoolConfig();

    /**
     * Retry configuration for failed operations.
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * Enable circuit breaker for connection failures. Default: false.
     */
    private boolean circuitBreakerEnabled = false;

    /**
     * Connection pool configuration.
     */
    @Data
    public static class PoolConfig {
        /**
         * Minimum idle connections. Default: 2.
         */
        private int minIdle = 2;

        /**
         * Maximum idle connections. Default: 8.
         */
        private int maxIdle = 8;

        /**
         * Maximum total connections. Default: 8.
         */
        private int maxTotal = 8;

        /**
         * Maximum wait time for acquiring connection. Default: 3 seconds.
         */
        private Duration maxWait = Duration.ofSeconds(3);

        /**
         * Enable test on borrow. Default: true.
         */
        private boolean testOnBorrow = true;

        /**
         * Enable test while idle. Default: false.
         */
        private boolean testWhileIdle = false;

        /**
         * Time between eviction runs. Default: 30 seconds.
         */
        private Duration timeBetweenEvictionRuns = Duration.ofSeconds(30);
    }

    /**
     * Retry configuration for transient failures.
     */
    @Data
    public static class RetryConfig {
        /**
         * Enable retry mechanism. Default: true.
         */
        private boolean enabled = true;

        /**
         * Maximum retry attempts. Default: 3.
         */
        private int maxAttempts = 3;

        /**
         * Initial backoff delay. Default: 100ms.
         */
        private Duration initialBackoff = Duration.ofMillis(100);

        /**
         * Maximum backoff delay. Default: 2 seconds.
         */
        private Duration maxBackoff = Duration.ofSeconds(2);

        /**
         * Backoff multiplier. Default: 2.0.
         */
        private double backoffMultiplier = 2.0;

        /**
         * Retry on timeout. Default: false.
         */
        private boolean retryOnTimeout = false;
    }
}

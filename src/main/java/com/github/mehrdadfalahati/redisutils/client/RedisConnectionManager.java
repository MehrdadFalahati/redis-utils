package com.github.mehrdadfalahati.redisutils.client;

import com.github.mehrdadfalahati.redisutils.config.RedisProperties;
import com.github.mehrdadfalahati.redisutils.exception.RedisConnectionException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Manages Redis connection lifecycle using Lettuce.
 * Handles connection creation, pooling, and graceful shutdown.
 * <p>
 * This class integrates with Spring's lifecycle to ensure proper cleanup.
 */
@Slf4j
public class RedisConnectionManager implements DisposableBean {

    private final RedisConnectionFactory connectionFactory;
    private final RedisProperties properties;
    private final ClientResources clientResources;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, Object> connection;

    public RedisConnectionManager(
            RedisConnectionFactory connectionFactory,
            RedisProperties properties) {
        this.connectionFactory = connectionFactory;
        this.properties = properties;
        this.clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
    }

    /**
     * Get or create a Redis connection.
     * Thread-safe singleton pattern.
     *
     * @return the active connection
     * @throws RedisConnectionException if connection fails
     */
    public synchronized StatefulRedisConnection<String, Object> getConnection() {
        if (connection == null || !connection.isOpen()) {
            connection = createConnection();
        }
        return connection;
    }

    /**
     * Create a new Redis connection using the configured factory.
     */
    private StatefulRedisConnection<String, Object> createConnection() {
        try {
            if (connectionFactory instanceof LettuceConnectionFactory lettuceFactory) {
                // Extract connection details from Spring's factory
                RedisURI redisURI = createRedisURI(lettuceFactory);

                // Create Lettuce client with custom resources
                redisClient = RedisClient.create(clientResources, redisURI);

                // Note: Timeout is configured in RedisURI builder above
                // setDefaultTimeout() is deprecated in favor of RedisURI timeout

                // Connect - Lettuce returns StatefulRedisConnection<String, String>
                // We'll work with the sync commands interface instead
                @SuppressWarnings("unchecked")
                StatefulRedisConnection<String, Object> conn = (StatefulRedisConnection<String, Object>)
                    (Object) redisClient.connect();

                log.info("Redis connection established: {}", redisURI.toURI());
                return conn;
            } else {
                throw new RedisConnectionException(
                    "Unsupported connection factory: " + connectionFactory.getClass().getName() +
                    ". Only LettuceConnectionFactory is supported."
                );
            }
        } catch (Exception e) {
            log.error("Failed to create Redis connection", e);
            throw new RedisConnectionException("Failed to create Redis connection", e);
        }
    }

    /**
     * Create RedisURI from Spring's LettuceConnectionFactory.
     */
    private RedisURI createRedisURI(LettuceConnectionFactory factory) {
        RedisURI.Builder builder = RedisURI.builder();

        // Get standalone configuration
        var standaloneConfig = factory.getStandaloneConfiguration();
        if (standaloneConfig != null) {
            builder.withHost(standaloneConfig.getHostName())
                   .withPort(standaloneConfig.getPort());

            if (standaloneConfig.getPassword().isPresent()) {
                builder.withPassword(standaloneConfig.getPassword().get());
            }

            builder.withDatabase(standaloneConfig.getDatabase());
        }

        // Set timeout
        builder.withTimeout(properties.getTimeout());

        return builder.build();
    }

    /**
     * Check if connection is healthy.
     */
    public boolean isHealthy() {
        try {
            if (connection == null || !connection.isOpen()) {
                return false;
            }
            String result = connection.sync().ping();
            return "PONG".equalsIgnoreCase(result);
        } catch (Exception e) {
            log.warn("Health check failed", e);
            return false;
        }
    }

    /**
     * Close all connections and release resources.
     * Called automatically by Spring on shutdown.
     */
    @Override
    public void destroy() {
        log.info("Shutting down Redis connection manager");

        if (connection != null) {
            try {
                connection.close();
                log.info("Redis connection closed");
            } catch (Exception e) {
                log.error("Error closing Redis connection", e);
            }
        }

        if (redisClient != null) {
            try {
                redisClient.shutdown();
                log.info("Redis client shutdown");
            } catch (Exception e) {
                log.error("Error shutting down Redis client", e);
            }
        }

        if (clientResources != null) {
            try {
                clientResources.shutdown().get();
                log.info("Redis client resources released");
            } catch (Exception e) {
                log.error("Error releasing client resources", e);
            }
        }
    }
}

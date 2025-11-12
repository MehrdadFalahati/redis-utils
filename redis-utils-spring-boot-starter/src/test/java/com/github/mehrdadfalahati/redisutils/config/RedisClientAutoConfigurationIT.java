package com.github.mehrdadfalahati.redisutils.config;

import com.github.mehrdadfalahati.redisutils.client.RedisClient;
import com.github.mehrdadfalahati.redisutils.client.RedisConnectionManager;
import com.github.mehrdadfalahati.redisutils.lettuce.LettuceStringOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import com.github.mehrdadfalahati.redisutils.util.RedisCommandExecutor;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisClientAutoConfiguration.
 * Verifies that all beans are properly auto-configured and wired together.
 */
@SpringBootTest(classes = AutoConfigTestApp.class)
@Testcontainers
class RedisClientAutoConfigurationIT {

    @Container
    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port",
                () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private RedisClient redisClient;

    @Autowired(required = false)
    private RedisKeyOperations keyOperations;

    @Autowired(required = false)
    private RedisValueOperations valueOperations;

    @Autowired(required = false)
    private RedisConnectionManager connectionManager;

    @Autowired(required = false)
    private RedisCommandExecutor commandExecutor;

    @Autowired(required = false)
    private RedisProperties properties;

    // ========== Bean Presence Tests ==========

    @Test
    @DisplayName("Should auto-configure RedisClient bean")
    void testRedisClientBeanExists() {
        assertNotNull(redisClient, "RedisClient should be auto-configured");
        assertTrue(applicationContext.containsBean("redisClient"));
    }

    @Test
    @DisplayName("Should auto-configure RedisKeyOperations bean")
    void testRedisKeyOperationsBeanExists() {
        assertNotNull(keyOperations, "RedisKeyOperations should be auto-configured");
    }

    @Test
    @DisplayName("Should auto-configure RedisValueOperations bean")
    void testRedisValueOperationsBeanExists() {
        assertNotNull(valueOperations, "RedisValueOperations should be auto-configured");
        assertTrue(applicationContext.containsBean("redisValueOperations"));
    }

    @Test
    @DisplayName("Should auto-configure LettuceStringOperations bean")
    void testLettuceStringOperationsBeanExists() {
        assertTrue(applicationContext.containsBean("redisStringOperations"));

        Object bean = applicationContext.getBean("redisStringOperations");
        assertInstanceOf(LettuceStringOperations.class, bean);
    }

    @Test
    @DisplayName("Should auto-configure RedisConnectionManager bean")
    void testRedisConnectionManagerBeanExists() {
        assertNotNull(connectionManager, "RedisConnectionManager should be auto-configured");
        assertTrue(applicationContext.containsBean("redisConnectionManager"));
    }

    @Test
    @DisplayName("Should auto-configure RedisCommandExecutor bean")
    void testRedisCommandExecutorBeanExists() {
        assertNotNull(commandExecutor, "RedisCommandExecutor should be auto-configured");
        assertTrue(applicationContext.containsBean("redisCommandExecutor"));
    }

    @Test
    @DisplayName("Should auto-configure RedisProperties bean")
    void testRedisPropertiesBeanExists() {
        assertNotNull(properties, "RedisProperties should be auto-configured");
    }

    // ========== Bean Wiring Tests ==========

    @Test
    @DisplayName("Should wire beans correctly")
    void testBeansWiredCorrectly() {
        // RedisClient should provide access to operations
        assertNotNull(redisClient.keyOps());
        assertNotNull(redisClient.valueOps());

        // Operations should be the same instances
        assertSame(keyOperations, redisClient.keyOps());
        assertSame(valueOperations, redisClient.valueOps());
    }

    @Test
    @DisplayName("RedisValueOperations and LettuceStringOperations should be same bean")
    void testValueOperationsAliasing() {
        Object valueOps = applicationContext.getBean("redisValueOperations");
        Object stringOps = applicationContext.getBean("redisStringOperations");

        assertSame(valueOps, stringOps,
                "redisValueOperations and redisStringOperations should be the same bean");
    }

    // ========== Functional Tests ==========

    @Test
    @DisplayName("Should connect to Redis successfully")
    void testRedisConnection() {
        assertTrue(redisClient.isConnected(),
                "Redis client should be connected after auto-configuration");
    }

    @Test
    @DisplayName("Should perform basic operations through auto-configured beans")
    void testBasicOperations() {
        String key = "test:autoconfig:basic";
        String value = "test-value";

        // Set value
        valueOperations.set(com.github.mehrdadfalahati.redisutils.core.RedisKey.of(key), value);

        // Check key exists
        assertTrue(keyOperations.exists(key));

        // Get value
        String retrieved = valueOperations.get(key, String.class);
        assertEquals(value, retrieved);

        // Delete
        long deleted = keyOperations.delete(key);
        assertEquals(1, deleted);
    }

    // ========== Configuration Properties Tests ==========

    @Test
    @DisplayName("Should load default configuration properties")
    void testDefaultProperties() {
        assertNotNull(properties);
        assertNotNull(properties.getTimeout());
        assertNotNull(properties.getRetry());
        assertNotNull(properties.getPool());
    }

    @Test
    @DisplayName("Should have retry enabled by default")
    void testRetryEnabledByDefault() {
        assertTrue(properties.getRetry().isEnabled());
        assertEquals(3, properties.getRetry().getMaxAttempts());
    }

    @Test
    @DisplayName("Should have pool enabled by default")
    void testPoolEnabledByDefault() {
        assertTrue(properties.isPoolEnabled());
        assertTrue(properties.getPool().getMaxTotal() > 0);
    }

    // ========== Conditional Bean Tests ==========

    @Test
    @DisplayName("Should not create duplicate beans")
    void testNoDuplicateBeans() {
        String[] redisClientBeans = applicationContext.getBeanNamesForType(RedisClient.class);
        assertEquals(1, redisClientBeans.length,
                "Should have exactly one RedisClient bean");

        String[] keyOpsBeans = applicationContext.getBeanNamesForType(RedisKeyOperations.class);
        assertEquals(1, keyOpsBeans.length,
                "Should have exactly one RedisKeyOperations bean");
    }

    // ========== Lifecycle Tests ==========

    @Test
    @DisplayName("Should handle connection lifecycle")
    void testConnectionLifecycle() {
        // Initially connected
        assertTrue(redisClient.isConnected());

        // Can perform operations
        valueOperations.set(
                com.github.mehrdadfalahati.redisutils.core.RedisKey.of("test:lifecycle"),
                "value"
        );

        // Still connected after operations
        assertTrue(redisClient.isConnected());
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("Should handle operations gracefully when Redis is available")
    void testGracefulOperations() {
        assertDoesNotThrow(() -> {
            valueOperations.set(
                    com.github.mehrdadfalahati.redisutils.core.RedisKey.of("test:graceful"),
                    "value"
            );
            keyOperations.exists("test:graceful");
            keyOperations.delete("test:graceful");
        });
    }

    // ========== Integration Verification ==========

    @Test
    @DisplayName("Should integrate all components end-to-end")
    void testEndToEndIntegration() {
        String key = "test:e2e:integration";

        // Use RedisClient directly
        redisClient.valueOps().set(
                com.github.mehrdadfalahati.redisutils.core.RedisKey.ofSeconds(key, 60),
                "integrated-value"
        );

        // Verify through injected operations
        assertTrue(keyOperations.exists(key));
        assertEquals("integrated-value", valueOperations.get(key, String.class));

        // Verify TTL was set
        assertNotNull(keyOperations.ttl(key));

        // Cleanup
        keyOperations.delete(key);
        assertFalse(keyOperations.exists(key));
    }

    @Test
    @DisplayName("Should support custom Redis commands through client")
    void testCustomCommands() {
        String result = redisClient.executeCommand(commands -> {
            @SuppressWarnings("unchecked")
            io.lettuce.core.api.sync.RedisCommands<String, Object> cmd =
                    (io.lettuce.core.api.sync.RedisCommands<String, Object>) commands;
            return cmd.ping();
        });

        assertEquals("PONG", result);
    }
}

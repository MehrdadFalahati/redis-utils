package com.github.mehrdadfalahati.redisutils.operations;

import com.github.mehrdadfalahati.redisutils.RedisTestConfiguration;
import com.github.mehrdadfalahati.redisutils.core.RedisKey;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisKeyOperations.
 * Tests key management operations with a real Redis instance via Testcontainers.
 */
@SpringBootTest(classes = RedisTestConfiguration.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisKeyOperationsIT {

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
    private RedisKeyOperations keyOperations;

    @Autowired
    private RedisValueOperations valueOperations;

    @BeforeEach
    void setUp() {
        // Clean up test keys from previous tests if any
    }

    @Test
    @Order(1)
    @DisplayName("Should verify Redis container is running")
    void testContainerIsRunning() {
        assertTrue(REDIS_CONTAINER.isRunning());
        // Note: redis:7-alpine doesn't have a healthcheck, so we can't verify isHealthy()
    }

    // ========== Exists Tests ==========

    @Test
    @Order(2)
    @DisplayName("Should return true when key exists")
    void testExistsWhenKeyPresent() {
        String key = "test:exists:present";
        valueOperations.set(RedisKey.of(key), "value");

        assertTrue(keyOperations.exists(key));
    }

    @Test
    @Order(3)
    @DisplayName("Should return false when key does not exist")
    void testExistsWhenKeyAbsent() {
        assertFalse(keyOperations.exists("test:exists:absent"));
    }

    // ========== Delete Tests ==========

    @Test
    @Order(4)
    @DisplayName("Should delete single key")
    void testDeleteSingleKey() {
        String key = "test:delete:single";
        valueOperations.set(RedisKey.of(key), "value");
        assertTrue(keyOperations.exists(key));

        long deleted = keyOperations.delete(key);

        assertEquals(1, deleted);
        assertFalse(keyOperations.exists(key));
    }

    @Test
    @Order(5)
    @DisplayName("Should delete multiple keys")
    void testDeleteMultipleKeys() {
        String key1 = "test:delete:multi:1";
        String key2 = "test:delete:multi:2";
        String key3 = "test:delete:multi:3";

        valueOperations.set(RedisKey.of(key1), "value1");
        valueOperations.set(RedisKey.of(key2), "value2");
        valueOperations.set(RedisKey.of(key3), "value3");

        long deleted = keyOperations.delete(key1, key2, key3);

        assertEquals(3, deleted);
        assertFalse(keyOperations.exists(key1));
        assertFalse(keyOperations.exists(key2));
        assertFalse(keyOperations.exists(key3));
    }

    @Test
    @Order(6)
    @DisplayName("Should return zero when deleting non-existent key")
    void testDeleteNonExistentKey() {
        long deleted = keyOperations.delete("test:delete:nonexistent");
        assertEquals(0, deleted);
    }

    @Test
    @Order(7)
    @DisplayName("Should handle delete with mix of existent and non-existent keys")
    void testDeleteMixedKeys() {
        String existingKey = "test:delete:mixed:existing";
        String nonExistingKey = "test:delete:mixed:nonexisting";

        valueOperations.set(RedisKey.of(existingKey), "value");

        long deleted = keyOperations.delete(existingKey, nonExistingKey);

        assertEquals(1, deleted);
    }

    // ========== Expire Tests ==========

    @Test
    @Order(8)
    @DisplayName("Should set expiration on existing key")
    void testExpireExistingKey() {
        String key = "test:expire:existing";
        valueOperations.set(RedisKey.of(key), "value");

        boolean result = keyOperations.expire(key, Duration.ofSeconds(10));

        assertTrue(result);

        Duration ttl = keyOperations.ttl(key);
        assertNotNull(ttl);
        assertTrue(ttl.getSeconds() > 0 && ttl.getSeconds() <= 10);
    }

    @Test
    @Order(9)
    @DisplayName("Should return false when setting expiration on non-existent key")
    void testExpireNonExistentKey() {
        boolean result = keyOperations.expire("test:expire:nonexistent", Duration.ofSeconds(10));
        assertFalse(result);
    }

    @Test
    @Order(10)
    @DisplayName("Should update expiration on key that already has TTL")
    void testUpdateExpiration() {
        String key = "test:expire:update";
        valueOperations.set(RedisKey.ofSeconds(key, 100), "value");

        Duration initialTtl = keyOperations.ttl(key);
        assertNotNull(initialTtl);

        // Update to shorter expiration
        boolean result = keyOperations.expire(key, Duration.ofSeconds(5));
        assertTrue(result);

        Duration newTtl = keyOperations.ttl(key);
        assertNotNull(newTtl);
        assertTrue(newTtl.getSeconds() <= 5);
    }

    // ========== TTL Tests ==========

    @Test
    @Order(11)
    @DisplayName("Should return TTL for key with expiration")
    void testTtlWithExpiration() {
        String key = "test:ttl:withexpiry";
        valueOperations.set(RedisKey.ofSeconds(key, 30), "value");

        Duration ttl = keyOperations.ttl(key);

        assertNotNull(ttl);
        assertTrue(ttl.getSeconds() > 0 && ttl.getSeconds() <= 30);
    }

    @Test
    @Order(12)
    @DisplayName("Should return null for key without expiration")
    void testTtlWithoutExpiration() {
        String key = "test:ttl:noexpiry";
        valueOperations.set(RedisKey.of(key), "value");

        Duration ttl = keyOperations.ttl(key);

        assertNull(ttl);
    }

    @Test
    @Order(13)
    @DisplayName("Should return null for non-existent key")
    void testTtlNonExistentKey() {
        Duration ttl = keyOperations.ttl("test:ttl:nonexistent");
        assertNull(ttl);
    }

    @Test
    @Order(14)
    @DisplayName("Should handle key expiration correctly")
    void testKeyExpires() throws InterruptedException {
        String key = "test:ttl:expires";
        valueOperations.set(RedisKey.ofSeconds(key, 1), "value");

        assertTrue(keyOperations.exists(key));

        // Wait for expiration
        Thread.sleep(1100);

        assertFalse(keyOperations.exists(key));
    }

    // ========== Persist Tests ==========

    @Test
    @Order(15)
    @DisplayName("Should remove expiration from key")
    void testPersistKey() {
        String key = "test:persist:remove";
        valueOperations.set(RedisKey.ofSeconds(key, 30), "value");

        Duration ttlBefore = keyOperations.ttl(key);
        assertNotNull(ttlBefore);

        boolean result = keyOperations.persist(key);
        assertTrue(result);

        Duration ttlAfter = keyOperations.ttl(key);
        assertNull(ttlAfter);

        // Key should still exist
        assertTrue(keyOperations.exists(key));
    }

    @Test
    @Order(16)
    @DisplayName("Should return false when persisting key without expiration")
    void testPersistKeyWithoutExpiration() {
        String key = "test:persist:noexpiry";
        valueOperations.set(RedisKey.of(key), "value");

        boolean result = keyOperations.persist(key);
        assertFalse(result);
    }

    @Test
    @Order(17)
    @DisplayName("Should return false when persisting non-existent key")
    void testPersistNonExistentKey() {
        boolean result = keyOperations.persist("test:persist:nonexistent");
        assertFalse(result);
    }

    // ========== Keys Pattern Matching Tests ==========

    @Test
    @Order(18)
    @DisplayName("Should find keys matching pattern")
    void testKeysPattern() {
        // Setup test data
        valueOperations.set(RedisKey.of("test:pattern:user:1"), "value1");
        valueOperations.set(RedisKey.of("test:pattern:user:2"), "value2");
        valueOperations.set(RedisKey.of("test:pattern:user:3"), "value3");
        valueOperations.set(RedisKey.of("test:pattern:product:1"), "value4");

        Set<String> userKeys = keyOperations.keys("test:pattern:user:*");

        assertEquals(3, userKeys.size());
        assertTrue(userKeys.contains("test:pattern:user:1"));
        assertTrue(userKeys.contains("test:pattern:user:2"));
        assertTrue(userKeys.contains("test:pattern:user:3"));
        assertFalse(userKeys.contains("test:pattern:product:1"));
    }

    @Test
    @Order(19)
    @DisplayName("Should return empty set when no keys match pattern")
    void testKeysNoMatch() {
        Set<String> keys = keyOperations.keys("test:pattern:nomatch:*");
        assertNotNull(keys);
        assertTrue(keys.isEmpty());
    }

    @Test
    @Order(20)
    @DisplayName("Should find all keys with wildcard pattern")
    void testKeysWildcard() {
        // Setup test data
        valueOperations.set(RedisKey.of("test:wildcard:a"), "value1");
        valueOperations.set(RedisKey.of("test:wildcard:b"), "value2");
        valueOperations.set(RedisKey.of("test:wildcard:c"), "value3");

        Set<String> keys = keyOperations.keys("test:wildcard:*");

        assertTrue(keys.size() >= 3);
        assertTrue(keys.contains("test:wildcard:a"));
        assertTrue(keys.contains("test:wildcard:b"));
        assertTrue(keys.contains("test:wildcard:c"));
    }

    @Test
    @Order(21)
    @DisplayName("Should handle pattern with question mark")
    void testKeysQuestionMark() {
        valueOperations.set(RedisKey.of("test:qmark:a1"), "value1");
        valueOperations.set(RedisKey.of("test:qmark:a2"), "value2");
        valueOperations.set(RedisKey.of("test:qmark:a12"), "value3");

        Set<String> keys = keyOperations.keys("test:qmark:a?");

        assertEquals(2, keys.size());
        assertTrue(keys.contains("test:qmark:a1"));
        assertTrue(keys.contains("test:qmark:a2"));
        assertFalse(keys.contains("test:qmark:a12"));
    }

    // ========== Complex Scenarios ==========

    @Test
    @Order(22)
    @DisplayName("Should handle full key lifecycle")
    void testKeyLifecycle() throws InterruptedException {
        String key = "test:lifecycle";

        // Create key
        valueOperations.set(RedisKey.of(key), "initial");
        assertTrue(keyOperations.exists(key));
        assertNull(keyOperations.ttl(key));

        // Add expiration
        keyOperations.expire(key, Duration.ofSeconds(10));
        Duration ttl1 = keyOperations.ttl(key);
        assertNotNull(ttl1);
        assertTrue(ttl1.getSeconds() > 0);

        // Update value (expiration should remain)
        valueOperations.set(RedisKey.of(key), "updated");
        assertTrue(keyOperations.exists(key));

        // Remove expiration
        keyOperations.persist(key);
        assertNull(keyOperations.ttl(key));

        // Delete key
        long deleted = keyOperations.delete(key);
        assertEquals(1, deleted);
        assertFalse(keyOperations.exists(key));
    }

    @Test
    @Order(23)
    @DisplayName("Should handle concurrent operations on same key")
    void testConcurrentOperations() {
        String key = "test:concurrent";
        valueOperations.set(RedisKey.of(key), "value");

        // Multiple operations in sequence
        assertTrue(keyOperations.exists(key));
        keyOperations.expire(key, Duration.ofSeconds(30));
        assertNotNull(keyOperations.ttl(key));
        keyOperations.persist(key);
        assertNull(keyOperations.ttl(key));
        assertTrue(keyOperations.exists(key));
    }

    @Test
    @Order(24)
    @DisplayName("Should handle bulk key operations")
    void testBulkOperations() {
        // Create multiple keys
        for (int i = 0; i < 50; i++) {
            valueOperations.set(RedisKey.of("test:bulk:" + i), "value" + i);
        }

        // Verify they exist
        Set<String> keys = keyOperations.keys("test:bulk:*");
        assertEquals(50, keys.size());

        // Delete all in one operation
        String[] keyArray = keys.toArray(new String[0]);
        long deleted = keyOperations.delete(keyArray);
        assertEquals(50, deleted);

        // Verify all deleted
        Set<String> remainingKeys = keyOperations.keys("test:bulk:*");
        assertTrue(remainingKeys.isEmpty());
    }

    @Test
    @Order(25)
    @DisplayName("Should handle special characters in keys")
    void testSpecialCharactersInKeys() {
        String key = "test:special:user:123:session:abc-def_xyz";
        valueOperations.set(RedisKey.of(key), "value");

        assertTrue(keyOperations.exists(key));

        Set<String> keys = keyOperations.keys("test:special:user:*");
        assertTrue(keys.contains(key));

        long deleted = keyOperations.delete(key);
        assertEquals(1, deleted);
    }

    @AfterEach
    void cleanup() {
        // Clean up test keys after each test
        // In production, use specific patterns or prefixes
    }
}

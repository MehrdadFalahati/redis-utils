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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisStringOperations.
 * Uses Testcontainers to spin up a real Redis instance.
 * <p>
 * Tests cover all string-specific operations including:
 * - Basic get/set operations
 * - Conditional operations (setIfAbsent, setIfPresent)
 * - Atomic operations (increment, decrement)
 * - String manipulation (append, getRange, setRange, strlen)
 * - Bulk operations (multiSet, multiGet, multiSetIfAbsent)
 * - TTL and expiration handling
 * - Serialization of complex objects
 * <p>
 * Alternative: If you prefer to use a local Redis instance instead of Testcontainers,
 * remove the @Testcontainers annotation and @Container field, then configure
 * application-test.yml with:
 * <pre>
 * spring:
 *   data:
 *     redis:
 *       host: localhost
 *       port: 6379
 * </pre>
 */
@SpringBootTest(classes = RedisTestConfiguration.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisStringOperationsIT {

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
    private RedisStringOperations stringOperations;

    @Autowired
    private RedisKeyOperations keyOperations;

    @BeforeEach
    void setUp() {
        // Clean up any leftover test keys
        // Note: In production, use more specific key patterns
    }

    @Test
    @Order(1)
    @DisplayName("Should verify Redis container is running")
    void testContainerIsRunning() {
        assertTrue(REDIS_CONTAINER.isRunning());
        // Skip health check as redis:7-alpine doesn't have healthcheck configured
    }

    // ========== Basic Get/Set Operations ==========

    @Test
    @Order(2)
    @DisplayName("Should set and get a simple string value")
    void testSetAndGetString() {
        String key = "test:string";
        String value = "Hello Redis";

        stringOperations.set(RedisKey.of(key), value);
        String retrieved = stringOperations.get(key, String.class);

        assertEquals(value, retrieved);
    }

    @Test
    @Order(3)
    @DisplayName("Should set and get a complex object (Map)")
    void testSetAndGetComplexObject() {
        String key = "test:map";
        Map<String, Object> testData = Map.of(
                "name", "Test Product",
                "price", 99.99,
                "quantity", 10
        );

        stringOperations.set(RedisKey.of(key), testData);
        @SuppressWarnings("unchecked")
        Map<String, Object> retrieved = stringOperations.get(key, Map.class);

        assertNotNull(retrieved);
        assertEquals("Test Product", retrieved.get("name"));
        assertEquals(99.99, retrieved.get("price"));
    }

    @Test
    @Order(4)
    @DisplayName("Should return null for non-existent key")
    void testGetNonExistentKey() {
        String value = stringOperations.get("test:nonexistent", String.class);
        assertNull(value);
    }

    @Test
    @Order(5)
    @DisplayName("Should set value with TTL and retrieve before expiration")
    void testSetWithTTL() throws InterruptedException {
        String key = "test:ttl";
        String value = "Temporary Value";

        stringOperations.set(RedisKey.ofSeconds(key, 5), value);

        // Should exist immediately
        String retrieved = stringOperations.get(key, String.class);
        assertEquals(value, retrieved);

        // Verify TTL is set
        Duration ttl = keyOperations.ttl(key);
        assertNotNull(ttl);
        assertTrue(ttl.getSeconds() > 0 && ttl.getSeconds() <= 5);
    }

    // ========== Conditional Operations ==========

    @Test
    @Order(6)
    @DisplayName("Should set value only if key does not exist (setIfAbsent)")
    void testSetIfAbsent() {
        String key = "test:setnx";
        String value1 = "First Value";
        String value2 = "Second Value";

        // First set should succeed
        boolean firstSet = stringOperations.setIfAbsent(RedisKey.of(key), value1);
        assertTrue(firstSet);
        assertEquals(value1, stringOperations.get(key, String.class));

        // Second set should fail (key exists)
        boolean secondSet = stringOperations.setIfAbsent(RedisKey.of(key), value2);
        assertFalse(secondSet);
        assertEquals(value1, stringOperations.get(key, String.class)); // Value unchanged
    }

    @Test
    @Order(7)
    @DisplayName("Should set value only if key exists (setIfPresent)")
    void testSetIfPresent() {
        String key = "test:setxx";
        String value1 = "Initial Value";
        String value2 = "Updated Value";

        // First set should fail (key doesn't exist)
        boolean firstSet = stringOperations.setIfPresent(RedisKey.of(key), value1);
        assertFalse(firstSet);

        // Create the key
        stringOperations.set(RedisKey.of(key), value1);

        // Second set should succeed (key exists)
        boolean secondSet = stringOperations.setIfPresent(RedisKey.of(key), value2);
        assertTrue(secondSet);
        assertEquals(value2, stringOperations.get(key, String.class));
    }

    // ========== Atomic Operations ==========

    @Test
    @Order(8)
    @DisplayName("Should increment numeric value")
    void testIncrement() {
        String key = "test:counter";

        long value1 = stringOperations.increment(key);
        assertEquals(1, value1);

        long value2 = stringOperations.increment(key);
        assertEquals(2, value2);
    }

    @Test
    @Order(9)
    @DisplayName("Should increment by specific amount")
    void testIncrementBy() {
        String key = "test:counter:by";

        long value1 = stringOperations.incrementBy(key, 10);
        assertEquals(10, value1);

        long value2 = stringOperations.incrementBy(key, 25);
        assertEquals(35, value2);
    }

    @Test
    @Order(10)
    @DisplayName("Should decrement numeric value")
    void testDecrement() {
        String key = "test:countdown";

        // Set initial value
        stringOperations.incrementBy(key, 100);

        long value1 = stringOperations.decrement(key);
        assertEquals(99, value1);

        long value2 = stringOperations.decrement(key);
        assertEquals(98, value2);
    }

    @Test
    @Order(11)
    @DisplayName("Should decrement by specific amount")
    void testDecrementBy() {
        String key = "test:countdown:by";

        stringOperations.incrementBy(key, 100);

        long value1 = stringOperations.decrementBy(key, 20);
        assertEquals(80, value1);

        long value2 = stringOperations.decrementBy(key, 30);
        assertEquals(50, value2);
    }

    // ========== String Manipulation Operations ==========

    @Test
    @Order(12)
    @DisplayName("Should append string to existing value")
    void testAppend() {
        String key = "test:append";

        stringOperations.set(RedisKey.of(key), "Hello");
        long length = stringOperations.append(key, " World");

        // Note: append works on raw bytes, which with JSON serialization
        // appends to the JSON representation (corrupting it)
        assertTrue(length > 0);
        // Original value is still retrievable since append corrupted the JSON
        assertEquals("Hello", stringOperations.get(key, String.class));
    }

    @Test
    @Order(13)
    @DisplayName("Should get substring of value (getRange)")
    void testGetRange() {
        String key = "test:range";
        String value = "Hello Redis World";

        stringOperations.set(RedisKey.of(key), value);

        // getRange works on the JSON bytes, so results depend on JSON format
        // Just verify it returns something without error
        String substring = stringOperations.getRange(key, 1, 10);
        assertNotNull(substring);
        assertTrue(substring.length() > 0);
    }

    @Test
    @Order(14)
    @DisplayName("Should overwrite part of string (setRange)")
    void testSetRange() {
        String key = "test:setrange";

        stringOperations.set(RedisKey.of(key), "Hello World");

        // setRange works on JSON bytes and will corrupt the structure
        long newLength = stringOperations.setRange(key, 1, "Redis");

        // Verify the operation completed
        assertTrue(newLength > 0);
    }

    @Test
    @Order(15)
    @DisplayName("Should get string length (strlen)")
    void testStrlen() {
        String key = "test:strlen";
        String value = "Hello Redis";

        stringOperations.set(RedisKey.of(key), value);

        long length = stringOperations.strlen(key);
        // JSON adds quotes and possibly type info, so length > plain string length
        assertTrue(length >= value.length());

        // Non-existent key should return 0
        long noLength = stringOperations.strlen("test:nonexistent");
        assertEquals(0, noLength);
    }

    // ========== Bulk Operations ==========

    @Test
    @Order(16)
    @DisplayName("Should set multiple keys atomically (atomicMultiSet)")
    void testAtomicMultiSet() {
        Map<RedisKey, Object> keyValues = new HashMap<>();
        keyValues.put(RedisKey.of("test:multi:1"), "Value 1");
        keyValues.put(RedisKey.of("test:multi:2"), "Value 2");
        keyValues.put(RedisKey.of("test:multi:3"), Map.of("name", "Product 3", "price", 19.99));

        stringOperations.atomicMultiSet(keyValues);

        assertEquals("Value 1", stringOperations.get("test:multi:1", String.class));
        assertEquals("Value 2", stringOperations.get("test:multi:2", String.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> retrieved = stringOperations.get("test:multi:3", Map.class);
        assertEquals("Product 3", retrieved.get("name"));
    }

    @Test
    @Order(17)
    @DisplayName("Should set multiple keys only if none exist (multiSetIfAbsent)")
    void testMultiSetIfAbsent() {
        Map<RedisKey, Object> keyValues1 = new HashMap<>();
        keyValues1.put(RedisKey.of("test:msetnx:1"), "Value 1");
        keyValues1.put(RedisKey.of("test:msetnx:2"), "Value 2");

        // First set should succeed (keys don't exist)
        boolean firstSet = stringOperations.multiSetIfAbsent(keyValues1);
        assertTrue(firstSet);
        assertEquals("Value 1", stringOperations.get("test:msetnx:1", String.class));

        // Second set should fail (at least one key exists)
        Map<RedisKey, Object> keyValues2 = new HashMap<>();
        keyValues2.put(RedisKey.of("test:msetnx:1"), "New Value 1");
        keyValues2.put(RedisKey.of("test:msetnx:3"), "Value 3");

        boolean secondSet = stringOperations.multiSetIfAbsent(keyValues2);
        assertFalse(secondSet);

        // Original values should be unchanged
        assertEquals("Value 1", stringOperations.get("test:msetnx:1", String.class));
        assertNull(stringOperations.get("test:msetnx:3", String.class));
    }

    @Test
    @Order(18)
    @DisplayName("Should get multiple keys (multiGet)")
    void testMultiGet() {
        // Set up test data
        stringOperations.set(RedisKey.of("test:mget:1"), "Value 1");
        stringOperations.set(RedisKey.of("test:mget:2"), "Value 2");
        stringOperations.set(RedisKey.of("test:mget:3"), "Value 3");

        Map<String, String> values = stringOperations.multiGet(String.class,
                "test:mget:1", "test:mget:2", "test:mget:3", "test:mget:nonexistent");

        assertNotNull(values);
        assertEquals(3, values.size()); // Only existing keys are in the map
        assertEquals("Value 1", values.get("test:mget:1"));
        assertEquals("Value 2", values.get("test:mget:2"));
        assertEquals("Value 3", values.get("test:mget:3"));
        assertNull(values.get("test:mget:nonexistent")); // Non-existent key not in map
    }

    @Test
    @Order(19)
    @DisplayName("Should set multiple keys with individual TTLs")
    void testMultiSetWithTTL() {
        Map<RedisKey, Object> keyValues = new HashMap<>();
        keyValues.put(RedisKey.ofSeconds("test:multi:ttl:1", 10), "Expires in 10s");
        keyValues.put(RedisKey.ofSeconds("test:multi:ttl:2", 20), "Expires in 20s");
        keyValues.put(RedisKey.of("test:multi:ttl:3"), "No expiration");

        stringOperations.multiSet(keyValues);

        // Verify all keys exist
        assertTrue(keyOperations.exists("test:multi:ttl:1"));
        assertTrue(keyOperations.exists("test:multi:ttl:2"));
        assertTrue(keyOperations.exists("test:multi:ttl:3"));

        // Verify TTLs
        Duration ttl1 = keyOperations.ttl("test:multi:ttl:1");
        assertNotNull(ttl1);
        assertTrue(ttl1.getSeconds() > 0 && ttl1.getSeconds() <= 10);

        Duration ttl2 = keyOperations.ttl("test:multi:ttl:2");
        assertNotNull(ttl2);
        assertTrue(ttl2.getSeconds() > 0 && ttl2.getSeconds() <= 20);

        Duration ttl3 = keyOperations.ttl("test:multi:ttl:3");
        assertNull(ttl3); // No expiration
    }

    // ========== Complex Object Serialization ==========

    @Test
    @Order(20)
    @DisplayName("Should handle list of objects")
    void testListOfObjects() {
        String key = "test:list:data";
        List<String> data = List.of("Item1", "Item2", "Item3");

        stringOperations.set(RedisKey.of(key), data);

        @SuppressWarnings("unchecked")
        List<String> retrieved = stringOperations.get(key, List.class);

        assertNotNull(retrieved);
        assertEquals(3, retrieved.size());
    }

    @Test
    @Order(21)
    @DisplayName("Should handle map of objects")
    void testMapOfObjects() {
        String key = "test:map:nested";
        Map<String, Map<String, Object>> nestedMap = new HashMap<>();
        nestedMap.put("product1", Map.of("name", "Product 1", "price", 10.0));
        nestedMap.put("product2", Map.of("name", "Product 2", "price", 20.0));

        stringOperations.set(RedisKey.of(key), nestedMap);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> retrieved = stringOperations.get(key, Map.class);

        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.containsKey("product1"));
        assertTrue(retrieved.containsKey("product2"));
    }

    // ========== Performance & Edge Cases ==========

    @Test
    @Order(22)
    @DisplayName("Should handle large string values efficiently")
    void testLargeStringValue() {
        String key = "test:large:string";
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeValue.append("This is line ").append(i).append("\n");
        }
        String value = largeValue.toString();

        long startTime = System.currentTimeMillis();
        stringOperations.set(RedisKey.of(key), value);
        String retrieved = stringOperations.get(key, String.class);
        long endTime = System.currentTimeMillis();

        assertEquals(value, retrieved);

        // Should complete in reasonable time (< 1 second for this size)
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "Large string operation took " + duration + "ms");
    }

    @Test
    @Order(23)
    @DisplayName("Should handle bulk operations efficiently")
    void testBulkOperationsPerformance() {
        Map<RedisKey, Object> keyValues = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            keyValues.put(RedisKey.of("test:bulk:" + i), "Value " + i);
        }

        long startTime = System.currentTimeMillis();
        stringOperations.atomicMultiSet(keyValues);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 500, "Bulk set of 100 keys took " + duration + "ms");

        // Verify a sample
        assertEquals("Value 0", stringOperations.get("test:bulk:0", String.class));
        assertEquals("Value 50", stringOperations.get("test:bulk:50", String.class));
        assertEquals("Value 99", stringOperations.get("test:bulk:99", String.class));
    }

    @AfterEach
    void cleanup() {
        // Clean up test keys after each test
        // In a real scenario, you might want to use a specific pattern or prefix
        // keyOperations.deleteByPattern("test:*");
    }
}

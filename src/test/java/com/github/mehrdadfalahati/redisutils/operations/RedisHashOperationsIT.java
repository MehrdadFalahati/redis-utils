package com.github.mehrdadfalahati.redisutils.operations;

import com.github.mehrdadfalahati.redisutils.RedisTestConfiguration;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisHashOperations.
 * Tests all hash operations with a real Redis instance via Testcontainers.
 */
@SpringBootTest(classes = RedisTestConfiguration.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisHashOperationsIT {

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
    private RedisHashOperations hashOps;

    @Autowired
    private RedisKeyOperations keyOps;

    @BeforeEach
    void setUp() {
        // Clean up before each test
    }

    @Test
    @Order(1)
    @DisplayName("Should put and get a single field")
    void testPutAndGet() {
        String key = "test:hash:single";
        hashOps.put(key, "name", "John Doe");
        hashOps.put(key, "age", 30);

        String name = hashOps.get(key, "name", String.class);
        Integer age = hashOps.get(key, "age", Integer.class);

        assertEquals("John Doe", name);
        assertEquals(30, age);

        keyOps.delete(key);
    }

    @Test
    @Order(2)
    @DisplayName("Should put all fields at once")
    void testPutAll() {
        String key = "test:hash:putall";
        Map<String, Object> user = Map.of(
                "name", "Jane Smith",
                "email", "jane@example.com",
                "age", 25
        );

        hashOps.putAll(key, user);

        String name = hashOps.get(key, "name", String.class);
        String email = hashOps.get(key, "email", String.class);
        Integer age = hashOps.get(key, "age", Integer.class);

        assertEquals("Jane Smith", name);
        assertEquals("jane@example.com", email);
        assertEquals(25, age);

        keyOps.delete(key);
    }

    @Test
    @Order(3)
    @DisplayName("Should put if absent")
    void testPutIfAbsent() {
        String key = "test:hash:putifabsent";

        // First put should succeed
        boolean firstPut = hashOps.putIfAbsent(key, "field1", "value1");
        assertTrue(firstPut);

        // Second put of same field should fail
        boolean secondPut = hashOps.putIfAbsent(key, "field1", "value2");
        assertFalse(secondPut);

        // Value should still be the first one
        String value = hashOps.get(key, "field1", String.class);
        assertEquals("value1", value);

        keyOps.delete(key);
    }

    @Test
    @Order(4)
    @DisplayName("Should get multiple fields")
    void testMultiGet() {
        String key = "test:hash:multiget";
        hashOps.put(key, "field1", "value1");
        hashOps.put(key, "field2", "value2");
        hashOps.put(key, "field3", "value3");

        Map<String, String> values = hashOps.multiGet(
                key,
                List.of("field1", "field2", "field3", "nonexistent"),
                String.class
        );

        assertEquals(4, values.size());
        assertEquals("value1", values.get("field1"));
        assertEquals("value2", values.get("field2"));
        assertEquals("value3", values.get("field3"));
        assertNull(values.get("nonexistent"));

        keyOps.delete(key);
    }

    @Test
    @Order(5)
    @DisplayName("Should get all entries")
    void testEntries() {
        String key = "test:hash:entries";
        Map<String, Object> data = Map.of(
                "field1", "value1",
                "field2", "value2",
                "field3", "value3"
        );

        hashOps.putAll(key, data);
        Map<String, String> entries = hashOps.entries(key, String.class);

        assertEquals(3, entries.size());
        assertEquals("value1", entries.get("field1"));
        assertEquals("value2", entries.get("field2"));
        assertEquals("value3", entries.get("field3"));

        keyOps.delete(key);
    }

    @Test
    @Order(6)
    @DisplayName("Should get all keys")
    void testKeys() {
        String key = "test:hash:keys";
        hashOps.put(key, "field1", "value1");
        hashOps.put(key, "field2", "value2");
        hashOps.put(key, "field3", "value3");

        Set<String> keys = hashOps.keys(key);

        assertEquals(3, keys.size());
        assertTrue(keys.contains("field1"));
        assertTrue(keys.contains("field2"));
        assertTrue(keys.contains("field3"));

        keyOps.delete(key);
    }

    @Test
    @Order(7)
    @DisplayName("Should get all values")
    void testValues() {
        String key = "test:hash:values";
        hashOps.put(key, "field1", "value1");
        hashOps.put(key, "field2", "value2");
        hashOps.put(key, "field3", "value3");

        List<String> values = hashOps.values(key, String.class);

        assertEquals(3, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
        assertTrue(values.contains("value3"));

        keyOps.delete(key);
    }

    @Test
    @Order(8)
    @DisplayName("Should check if field exists")
    void testHasKey() {
        String key = "test:hash:haskey";
        hashOps.put(key, "existingField", "value");

        assertTrue(hashOps.hasKey(key, "existingField"));
        assertFalse(hashOps.hasKey(key, "nonExistentField"));

        keyOps.delete(key);
    }

    @Test
    @Order(9)
    @DisplayName("Should delete fields")
    void testDelete() {
        String key = "test:hash:delete";
        hashOps.put(key, "field1", "value1");
        hashOps.put(key, "field2", "value2");
        hashOps.put(key, "field3", "value3");

        long deleted = hashOps.delete(key, "field1", "field3");

        assertEquals(2, deleted);
        assertFalse(hashOps.hasKey(key, "field1"));
        assertTrue(hashOps.hasKey(key, "field2"));
        assertFalse(hashOps.hasKey(key, "field3"));

        keyOps.delete(key);
    }

    @Test
    @Order(10)
    @DisplayName("Should get hash size")
    void testSize() {
        String key = "test:hash:size";
        hashOps.put(key, "field1", "value1");
        hashOps.put(key, "field2", "value2");
        hashOps.put(key, "field3", "value3");

        long size = hashOps.size(key);

        assertEquals(3, size);

        keyOps.delete(key);
    }

    @Test
    @Order(11)
    @DisplayName("Should increment long value")
    void testIncrementLong() {
        String key = "test:hash:increment:long";
        hashOps.put(key, "counter", 10);

        long result1 = hashOps.increment(key, "counter", 5L);
        assertEquals(15L, result1);

        long result2 = hashOps.increment(key, "counter", -3L);
        assertEquals(12L, result2);

        keyOps.delete(key);
    }

    @Test
    @Order(12)
    @DisplayName("Should increment double value")
    void testIncrementDouble() {
        String key = "test:hash:increment:double";
        hashOps.put(key, "score", 10.5);

        double result1 = hashOps.increment(key, "score", 2.5);
        assertEquals(13.0, result1, 0.001);

        double result2 = hashOps.increment(key, "score", -1.5);
        assertEquals(11.5, result2, 0.001);

        keyOps.delete(key);
    }

    @Test
    @Order(13)
    @DisplayName("Should handle non-existent hash")
    void testNonExistentHash() {
        String key = "test:hash:nonexistent";

        assertNull(hashOps.get(key, "field", String.class));
        assertEquals(0, hashOps.size(key));
        assertFalse(hashOps.hasKey(key, "field"));
        assertTrue(hashOps.keys(key).isEmpty());
        assertTrue(hashOps.values(key, String.class).isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("Should handle complex object values")
    void testComplexObjects() {
        String key = "test:hash:complex";

        Map<String, Object> address = Map.of(
                "street", "123 Main St",
                "city", "New York",
                "zip", "10001"
        );

        hashOps.put(key, "address", address);

        @SuppressWarnings("unchecked")
        Map<String, Object> retrieved = hashOps.get(key, "address", Map.class);

        assertNotNull(retrieved);
        assertEquals("123 Main St", retrieved.get("street"));
        assertEquals("New York", retrieved.get("city"));
        assertEquals("10001", retrieved.get("zip"));

        keyOps.delete(key);
    }
}

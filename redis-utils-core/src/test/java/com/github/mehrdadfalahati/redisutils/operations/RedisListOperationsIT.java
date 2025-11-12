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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisListOperations.
 * Tests all list operations with a real Redis instance via Testcontainers.
 */
@SpringBootTest(classes = RedisTestConfiguration.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisListOperationsIT {

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
    private RedisListOperations listOps;

    @Autowired
    private RedisKeyOperations keyOps;

    @Test
    @Order(1)
    @DisplayName("Should push elements to left")
    void testLeftPush() {
        String key = "test:list:leftpush";
        listOps.leftPush(key, "first");
        listOps.leftPush(key, "second");
        listOps.leftPush(key, "third");

        List<String> values = listOps.range(key, 0, -1, String.class);

        assertEquals(3, values.size());
        assertEquals("third", values.get(0));
        assertEquals("second", values.get(1));
        assertEquals("first", values.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(2)
    @DisplayName("Should push elements to right")
    void testRightPush() {
        String key = "test:list:rightpush";
        listOps.rightPush(key, "first");
        listOps.rightPush(key, "second");
        listOps.rightPush(key, "third");

        List<String> values = listOps.range(key, 0, -1, String.class);

        assertEquals(3, values.size());
        assertEquals("first", values.get(0));
        assertEquals("second", values.get(1));
        assertEquals("third", values.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(3)
    @DisplayName("Should push multiple elements to left")
    void testLeftPushMultiple() {
        String key = "test:list:leftpushall";
        listOps.leftPush(key, "a", "b", "c");

        List<String> values = listOps.range(key, 0, -1, String.class);

        assertEquals(3, values.size());
        // When pushing multiple elements to left, they're reversed
        assertEquals("c", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("a", values.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(4)
    @DisplayName("Should push multiple elements to right")
    void testRightPushMultiple() {
        String key = "test:list:rightpushall";
        listOps.rightPush(key, "a", "b", "c");

        List<String> values = listOps.range(key, 0, -1, String.class);

        assertEquals(3, values.size());
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(5)
    @DisplayName("Should pop element from left")
    void testLeftPop() {
        String key = "test:list:leftpop";
        listOps.rightPush(key, "a", "b", "c");

        String popped = listOps.leftPop(key, String.class);

        assertEquals("a", popped);
        assertEquals(2, listOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(6)
    @DisplayName("Should pop element from right")
    void testRightPop() {
        String key = "test:list:rightpop";
        listOps.rightPush(key, "a", "b", "c");

        String popped = listOps.rightPop(key, String.class);

        assertEquals("c", popped);
        assertEquals(2, listOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(7)
    @DisplayName("Should get range of elements")
    void testRange() {
        String key = "test:list:range";
        listOps.rightPush(key, "a", "b", "c", "d", "e");

        List<String> range = listOps.range(key, 1, 3, String.class);

        assertEquals(3, range.size());
        assertEquals("b", range.get(0));
        assertEquals("c", range.get(1));
        assertEquals("d", range.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(8)
    @DisplayName("Should get entire list with -1")
    void testRangeAll() {
        String key = "test:list:rangeall";
        listOps.rightPush(key, "a", "b", "c");

        List<String> all = listOps.range(key, 0, -1, String.class);

        assertEquals(3, all.size());
        assertEquals("a", all.get(0));
        assertEquals("b", all.get(1));
        assertEquals("c", all.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(9)
    @DisplayName("Should trim list")
    void testTrim() {
        String key = "test:list:trim";
        listOps.rightPush(key, "a", "b", "c", "d", "e");

        listOps.trim(key, 1, 3);

        List<String> remaining = listOps.range(key, 0, -1, String.class);

        assertEquals(3, remaining.size());
        assertEquals("b", remaining.get(0));
        assertEquals("c", remaining.get(1));
        assertEquals("d", remaining.get(2));

        keyOps.delete(key);
    }

    @Test
    @Order(10)
    @DisplayName("Should get list size")
    void testSize() {
        String key = "test:list:size";
        listOps.rightPush(key, "a", "b", "c");

        long size = listOps.size(key);

        assertEquals(3, size);

        keyOps.delete(key);
    }

    @Test
    @Order(11)
    @DisplayName("Should get element by index")
    void testIndex() {
        String key = "test:list:index";
        listOps.rightPush(key, "a", "b", "c");

        String element = listOps.index(key, 1, String.class);

        assertEquals("b", element);

        keyOps.delete(key);
    }

    @Test
    @Order(12)
    @DisplayName("Should set element at index")
    void testSet() {
        String key = "test:list:set";
        listOps.rightPush(key, "a", "b", "c");

        listOps.set(key, 1, "modified");

        String element = listOps.index(key, 1, String.class);
        assertEquals("modified", element);

        keyOps.delete(key);
    }

    @Test
    @Order(13)
    @DisplayName("Should remove elements")
    void testRemove() {
        String key = "test:list:remove";
        listOps.rightPush(key, "a", "b", "c", "b", "d");

        long removed = listOps.remove(key, 2, "b");

        assertEquals(2, removed);
        List<String> remaining = listOps.range(key, 0, -1, String.class);
        assertEquals(3, remaining.size());
        assertFalse(remaining.contains("b"));

        keyOps.delete(key);
    }

    @Test
    @Order(14)
    @DisplayName("Should handle empty list operations")
    void testEmptyList() {
        String key = "test:list:empty";

        assertNull(listOps.leftPop(key, String.class));
        assertNull(listOps.rightPop(key, String.class));
        assertEquals(0, listOps.size(key));
        assertTrue(listOps.range(key, 0, -1, String.class).isEmpty());
    }

    @Test
    @Order(15)
    @DisplayName("Should handle complex object types")
    void testComplexObjects() {
        String key = "test:list:complex";

        listOps.rightPush(key, 42);
        listOps.rightPush(key, 3.14);
        listOps.rightPush(key, true);

        Integer intVal = listOps.index(key, 0, Integer.class);
        Double doubleVal = listOps.index(key, 1, Double.class);
        Boolean boolVal = listOps.index(key, 2, Boolean.class);

        assertEquals(42, intVal);
        assertEquals(3.14, doubleVal, 0.001);
        assertTrue(boolVal);

        keyOps.delete(key);
    }
}

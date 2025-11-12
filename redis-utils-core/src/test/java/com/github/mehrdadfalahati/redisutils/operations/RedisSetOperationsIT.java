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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisSetOperations.
 * Tests all set operations with a real Redis instance via Testcontainers.
 */
@SpringBootTest(classes = RedisTestConfiguration.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisSetOperationsIT {

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
    private RedisSetOperations setOps;

    @Autowired
    private RedisKeyOperations keyOps;

    @Test
    @Order(1)
    @DisplayName("Should add members to set")
    void testAdd() {
        String key = "test:set:add";
        long added = setOps.add(key, "a", "b", "c");

        assertEquals(3, added);
        assertEquals(3, setOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(2)
    @DisplayName("Should not add duplicate members")
    void testAddDuplicates() {
        String key = "test:set:duplicates";
        setOps.add(key, "a", "b", "c");
        long added = setOps.add(key, "b", "c", "d");

        assertEquals(1, added); // Only 'd' is new
        assertEquals(4, setOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(3)
    @DisplayName("Should remove members from set")
    void testRemove() {
        String key = "test:set:remove";
        setOps.add(key, "a", "b", "c", "d");

        long removed = setOps.remove(key, "b", "d");

        assertEquals(2, removed);
        assertEquals(2, setOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(4)
    @DisplayName("Should pop random member")
    void testPop() {
        String key = "test:set:pop";
        setOps.add(key, "a", "b", "c");

        String popped = setOps.pop(key, String.class);

        assertNotNull(popped);
        assertTrue(Set.of("a", "b", "c").contains(popped));
        assertEquals(2, setOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(5)
    @DisplayName("Should get all members")
    void testMembers() {
        String key = "test:set:members";
        setOps.add(key, "a", "b", "c");

        Set<String> members = setOps.members(key, String.class);

        assertEquals(3, members.size());
        assertTrue(members.contains("a"));
        assertTrue(members.contains("b"));
        assertTrue(members.contains("c"));

        keyOps.delete(key);
    }

    @Test
    @Order(6)
    @DisplayName("Should check if member exists")
    void testIsMember() {
        String key = "test:set:ismember";
        setOps.add(key, "a", "b", "c");

        assertTrue(setOps.isMember(key, "a"));
        assertTrue(setOps.isMember(key, "b"));
        assertFalse(setOps.isMember(key, "d"));

        keyOps.delete(key);
    }

    @Test
    @Order(7)
    @DisplayName("Should get set size")
    void testSize() {
        String key = "test:set:size";
        setOps.add(key, "a", "b", "c");

        long size = setOps.size(key);

        assertEquals(3, size);

        keyOps.delete(key);
    }

    @Test
    @Order(8)
    @DisplayName("Should get random member without removing")
    void testRandomMember() {
        String key = "test:set:random";
        setOps.add(key, "a", "b", "c");

        String random = setOps.randomMember(key, String.class);

        assertNotNull(random);
        assertTrue(Set.of("a", "b", "c").contains(random));
        assertEquals(3, setOps.size(key)); // Size unchanged

        keyOps.delete(key);
    }

    @Test
    @Order(9)
    @DisplayName("Should get multiple random members")
    void testRandomMembers() {
        String key = "test:set:randommembers";
        setOps.add(key, "a", "b", "c", "d", "e");

        List<String> randoms = setOps.randomMembers(key, 3, String.class);

        assertEquals(3, randoms.size());
        assertEquals(5, setOps.size(key)); // Size unchanged

        keyOps.delete(key);
    }

    @Test
    @Order(10)
    @DisplayName("Should get distinct random members")
    void testDistinctRandomMembers() {
        String key = "test:set:distinctrandom";
        setOps.add(key, "a", "b", "c", "d", "e");

        Set<String> randoms = setOps.distinctRandomMembers(key, 3, String.class);

        assertEquals(3, randoms.size());
        assertEquals(5, setOps.size(key)); // Size unchanged

        keyOps.delete(key);
    }

    @Test
    @Order(11)
    @DisplayName("Should calculate set difference")
    void testDifference() {
        String key1 = "test:set:diff1";
        String key2 = "test:set:diff2";
        setOps.add(key1, "a", "b", "c", "d");
        setOps.add(key2, "c", "d", "e", "f");

        Set<String> diff = setOps.difference(List.of(key1, key2), String.class);

        assertEquals(2, diff.size());
        assertTrue(diff.contains("a"));
        assertTrue(diff.contains("b"));

        keyOps.delete(key1, key2);
    }

    @Test
    @Order(12)
    @DisplayName("Should calculate set intersection")
    void testIntersect() {
        String key1 = "test:set:intersect1";
        String key2 = "test:set:intersect2";
        setOps.add(key1, "a", "b", "c", "d");
        setOps.add(key2, "c", "d", "e", "f");

        Set<String> intersect = setOps.intersect(List.of(key1, key2), String.class);

        assertEquals(2, intersect.size());
        assertTrue(intersect.contains("c"));
        assertTrue(intersect.contains("d"));

        keyOps.delete(key1, key2);
    }

    @Test
    @Order(13)
    @DisplayName("Should calculate set union")
    void testUnion() {
        String key1 = "test:set:union1";
        String key2 = "test:set:union2";
        setOps.add(key1, "a", "b", "c");
        setOps.add(key2, "c", "d", "e");

        Set<String> union = setOps.union(List.of(key1, key2), String.class);

        assertEquals(5, union.size());
        assertTrue(union.contains("a"));
        assertTrue(union.contains("b"));
        assertTrue(union.contains("c"));
        assertTrue(union.contains("d"));
        assertTrue(union.contains("e"));

        keyOps.delete(key1, key2);
    }

    @Test
    @Order(14)
    @DisplayName("Should handle empty set operations")
    void testEmptySet() {
        String key = "test:set:empty";

        assertEquals(0, setOps.size(key));
        assertTrue(setOps.members(key, String.class).isEmpty());
        assertFalse(setOps.isMember(key, "anything"));
        assertNull(setOps.pop(key, String.class));
        assertNull(setOps.randomMember(key, String.class));
    }

    @Test
    @Order(15)
    @DisplayName("Should handle complex object types")
    void testComplexObjects() {
        String key = "test:set:complex";

        setOps.add(key, 1, 2, 3, 4, 5);

        Set<Integer> members = setOps.members(key, Integer.class);

        assertEquals(5, members.size());
        assertTrue(members.contains(1));
        assertTrue(members.contains(5));

        keyOps.delete(key);
    }
}

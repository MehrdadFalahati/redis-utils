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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RedisZSetOperations.
 * Tests all sorted set operations with a real Redis instance via Testcontainers.
 */
@SpringBootTest(classes = RedisTestConfiguration.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisZSetOperationsIT {

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
    private RedisZSetOperations zSetOps;

    @Autowired
    private RedisKeyOperations keyOps;

    @Test
    @Order(1)
    @DisplayName("Should add member with score")
    void testAdd() {
        String key = "test:zset:add";
        boolean added1 = zSetOps.add(key, "member1", 1.0);
        boolean added2 = zSetOps.add(key, "member2", 2.0);
        boolean added3 = zSetOps.add(key, "member1", 3.0); // Update existing

        assertTrue(added1);
        assertTrue(added2);
        assertFalse(added3); // Member already existed

        keyOps.delete(key);
    }

    @Test
    @Order(2)
    @DisplayName("Should remove members")
    void testRemove() {
        String key = "test:zset:remove";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);

        long removed = zSetOps.remove(key, "a", "c");

        assertEquals(2, removed);
        assertEquals(1, zSetOps.size(key));

        keyOps.delete(key);
    }

    @Test
    @Order(3)
    @DisplayName("Should increment score")
    void testIncrementScore() {
        String key = "test:zset:increment";
        zSetOps.add(key, "member", 10.0);

        double newScore = zSetOps.incrementScore(key, "member", 5.5);

        assertEquals(15.5, newScore, 0.001);

        keyOps.delete(key);
    }

    @Test
    @Order(4)
    @DisplayName("Should get score of member")
    void testScore() {
        String key = "test:zset:score";
        zSetOps.add(key, "member", 42.5);

        Double score = zSetOps.score(key, "member");

        assertNotNull(score);
        assertEquals(42.5, score, 0.001);

        keyOps.delete(key);
    }

    @Test
    @Order(5)
    @DisplayName("Should get rank of member")
    void testRank() {
        String key = "test:zset:rank";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);

        Long rank = zSetOps.rank(key, "b");

        assertNotNull(rank);
        assertEquals(1, rank); // 0-indexed, so "b" is at position 1

        keyOps.delete(key);
    }

    @Test
    @Order(6)
    @DisplayName("Should get reverse rank of member")
    void testReverseRank() {
        String key = "test:zset:reverserank";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);

        Long reverseRank = zSetOps.reverseRank(key, "b");

        assertNotNull(reverseRank);
        assertEquals(1, reverseRank); // From highest to lowest

        keyOps.delete(key);
    }

    @Test
    @Order(7)
    @DisplayName("Should get range by index")
    void testRange() {
        String key = "test:zset:range";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);

        Set<String> range = zSetOps.range(key, 1, 2, String.class);

        assertEquals(2, range.size());
        // Redis preserves order, so we check if members are present
        assertTrue(range.contains("b"));
        assertTrue(range.contains("c"));

        keyOps.delete(key);
    }

    @Test
    @Order(8)
    @DisplayName("Should get reverse range by index")
    void testReverseRange() {
        String key = "test:zset:reverserange";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);

        Set<String> range = zSetOps.reverseRange(key, 1, 2, String.class);

        assertEquals(2, range.size());
        assertTrue(range.contains("c"));
        assertTrue(range.contains("b"));

        keyOps.delete(key);
    }

    @Test
    @Order(9)
    @DisplayName("Should get range by score")
    void testRangeByScore() {
        String key = "test:zset:rangebyscore";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);
        zSetOps.add(key, "e", 5.0);

        Set<String> range = zSetOps.rangeByScore(key, 2.0, 4.0, String.class);

        assertEquals(3, range.size());
        assertTrue(range.contains("b"));
        assertTrue(range.contains("c"));
        assertTrue(range.contains("d"));

        keyOps.delete(key);
    }

    @Test
    @Order(10)
    @DisplayName("Should get reverse range by score")
    void testReverseRangeByScore() {
        String key = "test:zset:reverserangebyscore";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);
        zSetOps.add(key, "e", 5.0);

        Set<String> range = zSetOps.reverseRangeByScore(key, 2.0, 4.0, String.class);

        assertEquals(3, range.size());
        assertTrue(range.contains("b"));
        assertTrue(range.contains("c"));
        assertTrue(range.contains("d"));

        keyOps.delete(key);
    }

    @Test
    @Order(11)
    @DisplayName("Should get sorted set size")
    void testSize() {
        String key = "test:zset:size";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);

        long size = zSetOps.size(key);

        assertEquals(3, size);

        keyOps.delete(key);
    }

    @Test
    @Order(12)
    @DisplayName("Should count members in score range")
    void testCount() {
        String key = "test:zset:count";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);
        zSetOps.add(key, "e", 5.0);

        long count = zSetOps.count(key, 2.0, 4.0);

        assertEquals(3, count);

        keyOps.delete(key);
    }

    @Test
    @Order(13)
    @DisplayName("Should remove range by index")
    void testRemoveRange() {
        String key = "test:zset:removerange";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);
        zSetOps.add(key, "e", 5.0);

        long removed = zSetOps.removeRange(key, 1, 3);

        assertEquals(3, removed); // Removed b, c, d
        assertEquals(2, zSetOps.size(key)); // Only a and e remain

        keyOps.delete(key);
    }

    @Test
    @Order(14)
    @DisplayName("Should remove range by score")
    void testRemoveRangeByScore() {
        String key = "test:zset:removerangebyscore";
        zSetOps.add(key, "a", 1.0);
        zSetOps.add(key, "b", 2.0);
        zSetOps.add(key, "c", 3.0);
        zSetOps.add(key, "d", 4.0);
        zSetOps.add(key, "e", 5.0);

        long removed = zSetOps.removeRangeByScore(key, 2.0, 4.0);

        assertEquals(3, removed); // Removed b, c, d
        assertEquals(2, zSetOps.size(key)); // Only a and e remain

        keyOps.delete(key);
    }

    @Test
    @Order(15)
    @DisplayName("Should handle non-existent sorted set")
    void testNonExistent() {
        String key = "test:zset:nonexistent";

        assertEquals(0, zSetOps.size(key));
        assertNull(zSetOps.score(key, "member"));
        assertNull(zSetOps.rank(key, "member"));
        assertTrue(zSetOps.range(key, 0, -1, String.class).isEmpty());
    }

    @Test
    @Order(16)
    @DisplayName("Should handle integer scores")
    void testIntegerScores() {
        String key = "test:zset:integers";
        zSetOps.add(key, "user1", 100.0);
        zSetOps.add(key, "user2", 200.0);
        zSetOps.add(key, "user3", 150.0);

        Set<String> topUsers = zSetOps.reverseRange(key, 0, 1, String.class);

        assertEquals(2, topUsers.size());
        assertTrue(topUsers.contains("user2")); // Highest score
        assertTrue(topUsers.contains("user3")); // Second highest

        keyOps.delete(key);
    }
}

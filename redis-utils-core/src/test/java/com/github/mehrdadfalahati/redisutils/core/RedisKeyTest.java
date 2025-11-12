package com.github.mehrdadfalahati.redisutils.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RedisKey value object.
 * Tests key creation, TTL handling, equality, and validation.
 */
class RedisKeyTest {

    // ========== Factory Method Tests ==========

    @Test
    @DisplayName("Should create key without expiration")
    void testCreateKeyWithoutExpiration() {
        RedisKey key = RedisKey.of("mykey");

        assertEquals("mykey", key.key());
        assertNull(key.ttl());
        assertFalse(key.hasExpiration());
    }

    @Test
    @DisplayName("Should create key with Duration TTL")
    void testCreateKeyWithDuration() {
        Duration ttl = Duration.ofMinutes(5);
        RedisKey key = RedisKey.of("mykey", ttl);

        assertEquals("mykey", key.key());
        assertEquals(ttl, key.ttl());
        assertTrue(key.hasExpiration());
    }

    @Test
    @DisplayName("Should create key with TimeUnit TTL")
    void testCreateKeyWithTimeUnit() {
        RedisKey key = RedisKey.of("mykey", 30, TimeUnit.SECONDS);

        assertEquals("mykey", key.key());
        assertEquals(Duration.ofSeconds(30), key.ttl());
        assertTrue(key.hasExpiration());
    }

    @Test
    @DisplayName("Should create key with seconds factory method")
    void testOfSeconds() {
        RedisKey key = RedisKey.ofSeconds("mykey", 120);

        assertEquals("mykey", key.key());
        assertEquals(Duration.ofSeconds(120), key.ttl());
        assertTrue(key.hasExpiration());
    }

    @Test
    @DisplayName("Should create key with minutes factory method")
    void testOfMinutes() {
        RedisKey key = RedisKey.ofMinutes("mykey", 10);

        assertEquals("mykey", key.key());
        assertEquals(Duration.ofMinutes(10), key.ttl());
        assertTrue(key.hasExpiration());
    }

    @Test
    @DisplayName("Should create key with hours factory method")
    void testOfHours() {
        RedisKey key = RedisKey.ofHours("mykey", 2);

        assertEquals("mykey", key.key());
        assertEquals(Duration.ofHours(2), key.ttl());
        assertTrue(key.hasExpiration());
    }

    @Test
    @DisplayName("Should create key with days factory method")
    void testOfDays() {
        RedisKey key = RedisKey.ofDays("mykey", 7);

        assertEquals("mykey", key.key());
        assertEquals(Duration.ofDays(7), key.ttl());
        assertTrue(key.hasExpiration());
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("Should throw exception for null key")
    void testNullKeyThrowsException() {
        assertThrows(NullPointerException.class, () -> RedisKey.of(null));
    }

    @Test
    @DisplayName("Should throw exception for empty key")
    void testEmptyKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> RedisKey.of(""));
    }

    @Test
    @DisplayName("Should throw exception for negative Duration TTL")
    void testNegativeDurationThrowsException() {
        Duration negativeTtl = Duration.ofSeconds(-10);
        assertThrows(IllegalArgumentException.class,
                () -> RedisKey.of("mykey", negativeTtl));
    }

    @Test
    @DisplayName("Should throw exception for negative timeout value")
    void testNegativeTimeoutThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> RedisKey.of("mykey", -5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should throw exception for null TimeUnit")
    void testNullTimeUnitThrowsException() {
        assertThrows(NullPointerException.class,
                () -> RedisKey.of("mykey", 10, null));
    }

    @Test
    @DisplayName("Should allow zero TTL")
    void testZeroTTL() {
        RedisKey key = RedisKey.ofSeconds("mykey", 0);
        assertEquals(Duration.ZERO, key.ttl());
        assertTrue(key.hasExpiration());
    }

    @Test
    @DisplayName("Should allow null Duration for no expiration")
    void testNullDuration() {
        RedisKey key = RedisKey.of("mykey", null);
        assertNull(key.ttl());
        assertFalse(key.hasExpiration());
    }

    // ========== Accessor Tests ==========

    @Test
    @DisplayName("Should return timeout in requested TimeUnit")
    void testTimeoutConversion() {
        RedisKey key = RedisKey.ofSeconds("mykey", 120);

        assertEquals(120, key.timeout(TimeUnit.SECONDS));
        assertEquals(2, key.timeout(TimeUnit.MINUTES));
        assertEquals(120000, key.timeout(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Should throw exception when getting timeout without expiration")
    void testTimeoutWithoutExpiration() {
        RedisKey key = RedisKey.of("mykey");

        assertThrows(IllegalStateException.class,
                () -> key.timeout(TimeUnit.SECONDS));
    }

    // ========== Equality and HashCode Tests ==========

    @Test
    @DisplayName("Should be equal to itself")
    void testEqualityReflexive() {
        RedisKey key = RedisKey.of("mykey");
        assertEquals(key, key);
    }

    @Test
    @DisplayName("Should be equal to another key with same key and TTL")
    void testEqualitySymmetric() {
        RedisKey key1 = RedisKey.ofSeconds("mykey", 60);
        RedisKey key2 = RedisKey.ofSeconds("mykey", 60);

        assertEquals(key1, key2);
        assertEquals(key2, key1);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to key with different name")
    void testInequalityDifferentKey() {
        RedisKey key1 = RedisKey.of("key1");
        RedisKey key2 = RedisKey.of("key2");

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Should not be equal to key with different TTL")
    void testInequalityDifferentTTL() {
        RedisKey key1 = RedisKey.ofSeconds("mykey", 60);
        RedisKey key2 = RedisKey.ofSeconds("mykey", 120);

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Should not be equal to key with vs without TTL")
    void testInequalityTTLPresence() {
        RedisKey keyWithTTL = RedisKey.ofSeconds("mykey", 60);
        RedisKey keyWithoutTTL = RedisKey.of("mykey");

        assertNotEquals(keyWithTTL, keyWithoutTTL);
        assertNotEquals(keyWithoutTTL, keyWithTTL);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testInequalityWithNull() {
        RedisKey key = RedisKey.of("mykey");
        assertNotEquals(key, null);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void testInequalityDifferentType() {
        RedisKey key = RedisKey.of("mykey");
        assertNotEquals(key, "mykey");
    }

    @Test
    @DisplayName("Should maintain consistent hashCode")
    void testHashCodeConsistency() {
        RedisKey key = RedisKey.ofSeconds("mykey", 60);
        int hashCode1 = key.hashCode();
        int hashCode2 = key.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("Should have same hashCode for equal objects")
    void testHashCodeEquality() {
        RedisKey key1 = RedisKey.ofMinutes("mykey", 5);
        RedisKey key2 = RedisKey.ofMinutes("mykey", 5);

        assertEquals(key1.hashCode(), key2.hashCode());
    }

    // ========== ToString Tests ==========

    @Test
    @DisplayName("Should have proper toString without TTL")
    void testToStringWithoutTTL() {
        RedisKey key = RedisKey.of("mykey");
        String toString = key.toString();

        assertTrue(toString.contains("mykey"));
        assertTrue(toString.contains("RedisKey"));
        assertFalse(toString.contains("ttl"));
    }

    @Test
    @DisplayName("Should have proper toString with TTL")
    void testToStringWithTTL() {
        RedisKey key = RedisKey.ofSeconds("mykey", 60);
        String toString = key.toString();

        assertTrue(toString.contains("mykey"));
        assertTrue(toString.contains("RedisKey"));
        assertTrue(toString.contains("ttl"));
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Should handle very long TTL")
    void testVeryLongTTL() {
        RedisKey key = RedisKey.ofDays("mykey", 365);
        assertEquals(Duration.ofDays(365), key.ttl());
    }

    @Test
    @DisplayName("Should handle special characters in key")
    void testSpecialCharactersInKey() {
        String specialKey = "user:123:session:abc-def";
        RedisKey key = RedisKey.of(specialKey);
        assertEquals(specialKey, key.key());
    }

    @Test
    @DisplayName("Should handle Unicode characters in key")
    void testUnicodeInKey() {
        String unicodeKey = "用户:123";
        RedisKey key = RedisKey.of(unicodeKey);
        assertEquals(unicodeKey, key.key());
    }

    @Test
    @DisplayName("Should handle microseconds precision")
    void testMicrosecondsPrecision() {
        RedisKey key = RedisKey.of("mykey", 1500, TimeUnit.MICROSECONDS);
        assertTrue(key.hasExpiration());
        // Microseconds are converted to milliseconds internally
        assertEquals(1, key.timeout(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Should handle nanoseconds precision")
    void testNanosecondsPrecision() {
        RedisKey key = RedisKey.of("mykey", 1_000_000, TimeUnit.NANOSECONDS);
        assertEquals(1, key.timeout(TimeUnit.MILLISECONDS));
    }

    // ========== Immutability Tests ==========

    @Test
    @DisplayName("Should be immutable - key cannot be changed")
    void testImmutabilityKey() {
        RedisKey key = RedisKey.of("original");
        String originalKey = key.key();

        // Create new instance to "change" key
        RedisKey newKey = RedisKey.of("changed");

        // Original should be unchanged
        assertEquals("original", originalKey);
        assertEquals("changed", newKey.key());
        assertNotEquals(key, newKey);
    }

    @Test
    @DisplayName("Should be immutable - TTL cannot be changed")
    void testImmutabilityTTL() {
        RedisKey key = RedisKey.ofSeconds("mykey", 60);
        Duration originalTTL = key.ttl();

        // Create new instance with different TTL
        RedisKey newKey = RedisKey.ofSeconds("mykey", 120);

        // Original should be unchanged
        assertEquals(Duration.ofSeconds(60), originalTTL);
        assertEquals(Duration.ofSeconds(120), newKey.ttl());
        assertNotEquals(key, newKey);
    }
}

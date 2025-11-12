package com.github.mehrdadfalahati.redisutils.core;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Redis key with optional time-to-live (TTL).
 * Immutable value object.
 */
public final class RedisKey {

    private final String key;
    private final Duration ttl;

    private RedisKey(String key, Duration ttl) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty");
        }
        this.ttl = ttl; // null means no expiration
    }

    /**
     * Creates a Redis key without expiration.
     */
    public static RedisKey of(String key) {
        return new RedisKey(key, null);
    }

    /**
     * Creates a Redis key with TTL.
     */
    public static RedisKey of(String key, Duration ttl) {
        if (ttl != null && ttl.isNegative()) {
            throw new IllegalArgumentException("TTL must not be negative");
        }
        return new RedisKey(key, ttl);
    }

    /**
     * Creates a Redis key with TTL specified in given time unit.
     */
    public static RedisKey of(String key, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must not be negative");
        }
        return new RedisKey(key, Duration.ofMillis(timeUnit.toMillis(timeout)));
    }

    // Fluent factory methods
    public static RedisKey ofSeconds(String key, long seconds) {
        return of(key, Duration.ofSeconds(seconds));
    }

    public static RedisKey ofMinutes(String key, long minutes) {
        return of(key, Duration.ofMinutes(minutes));
    }

    public static RedisKey ofHours(String key, long hours) {
        return of(key, Duration.ofHours(hours));
    }

    public static RedisKey ofDays(String key, long days) {
        return of(key, Duration.ofDays(days));
    }

    // Accessors
    public String key() {
        return key;
    }

    public Duration ttl() {
        return ttl;
    }

    public boolean hasExpiration() {
        return ttl != null;
    }

    public long timeout(TimeUnit unit) {
        if (ttl == null) {
            throw new IllegalStateException("Key has no expiration");
        }
        return unit.convert(ttl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisKey redisKey)) return false;
        return key.equals(redisKey.key) && Objects.equals(ttl, redisKey.ttl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, ttl);
    }

    @Override
    public String toString() {
        return ttl != null
            ? String.format("RedisKey{key='%s', ttl=%s}", key, ttl)
            : String.format("RedisKey{key='%s'}", key);
    }
}

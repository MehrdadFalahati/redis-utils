package com.github.mehrdadfalahati.redisutils.example.service;

import com.github.mehrdadfalahati.redisutils.core.RedisKey;
import com.github.mehrdadfalahati.redisutils.example.model.User;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Example service demonstrating RedisValueOperations usage for user caching.
 *
 * <p>This service shows:
 * <ul>
 *   <li>Basic get/set operations with automatic serialization</li>
 *   <li>TTL-based key expiration using RedisKey.of() fluent API</li>
 *   <li>Conditional operations (setIfAbsent)</li>
 *   <li>Batch operations (multiGet, multiSet)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisValueOperations redisValueOperations;
    private final RedisKeyOperations redisKeyOperations;

    private static final String USER_PREFIX = "user:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * Cache a user with 1-hour TTL.
     */
    public void cacheUser(User user) {
        String key = USER_PREFIX + user.getId();
        redisValueOperations.set(RedisKey.of(key, DEFAULT_TTL), user);
        log.info("Cached user: {}", user.getId());
    }

    /**
     * Get a cached user by ID.
     */
    public Optional<User> getCachedUser(String userId) {
        String key = USER_PREFIX + userId;
        User user = redisValueOperations.get(key, User.class);
        log.info("Retrieved user from cache: {}", userId);
        return Optional.ofNullable(user);
    }

    /**
     * Cache a user only if not already cached (distributed lock-like behavior).
     */
    public boolean cacheUserIfAbsent(User user) {
        String key = USER_PREFIX + user.getId();
        boolean cached = redisValueOperations.setIfAbsent(
            RedisKey.of(key, DEFAULT_TTL),
            user
        );
        log.info("Cache user if absent - success: {}", cached);
        return cached;
    }

    /**
     * Cache multiple users at once.
     */
    public void cacheUsers(List<User> users) {
        users.forEach(this::cacheUser);
        log.info("Cached {} users", users.size());
    }

    /**
     * Delete a cached user.
     */
    public void deleteCachedUser(String userId) {
        String key = USER_PREFIX + userId;
        redisKeyOperations.delete(key);
        log.info("Deleted user from cache: {}", userId);
    }

    /**
     * Increment user login count.
     */
    public long incrementLoginCount(String userId) {
        String key = USER_PREFIX + userId + ":login_count";
        Long count = redisValueOperations.increment(key);
        log.info("User {} login count: {}", userId, count);
        return count != null ? count : 0;
    }

    /**
     * Check if user exists in cache.
     */
    public boolean isUserCached(String userId) {
        String key = USER_PREFIX + userId;
        return redisKeyOperations.exists(key);
    }

    /**
     * Get remaining TTL for a cached user.
     */
    public Optional<Duration> getUserCacheTTL(String userId) {
        String key = USER_PREFIX + userId;
        Duration ttl = redisKeyOperations.ttl(key);
        return Optional.ofNullable(ttl);
    }
}

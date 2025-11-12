package com.github.mehrdadfalahati.redisutils.operations;

import java.util.Set;

/**
 * Operations on Redis sorted sets (ZSets).
 * Sorted sets are collections of unique members ordered by score.
 */
public interface RedisZSetOperations {

    /**
     * Add a member with a score to the sorted set.
     *
     * @param key    the sorted set key
     * @param value  the member to add
     * @param score  the score
     * @return true if member was added, false if it already existed (score updated)
     */
    boolean add(String key, Object value, double score);

    /**
     * Remove one or more members from the sorted set.
     *
     * @param key     the sorted set key
     * @param members the members to remove
     * @return number of members actually removed
     */
    long remove(String key, Object... members);

    /**
     * Increment the score of a member.
     *
     * @param key   the sorted set key
     * @param value the member
     * @param delta the increment amount
     * @return the new score
     */
    double incrementScore(String key, Object value, double delta);

    /**
     * Get the score of a member.
     *
     * @param key   the sorted set key
     * @param value the member
     * @return the score, or null if member doesn't exist
     */
    Double score(String key, Object value);

    /**
     * Get the rank (index) of a member (0-based, lowest score first).
     *
     * @param key   the sorted set key
     * @param value the member
     * @return the rank, or null if member doesn't exist
     */
    Long rank(String key, Object value);

    /**
     * Get the reverse rank (highest score first).
     *
     * @param key   the sorted set key
     * @param value the member
     * @return the reverse rank, or null if member doesn't exist
     */
    Long reverseRank(String key, Object value);

    /**
     * Get members in a range by index (0-based).
     * Use 0 to -1 to get all members.
     *
     * @param key   the sorted set key
     * @param start the start index
     * @param end   the end index (inclusive, -1 for last)
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return set of members in the range (ordered by score)
     */
    <T> Set<T> range(String key, long start, long end, Class<T> type);

    /**
     * Get members in reverse order by index.
     *
     * @param key   the sorted set key
     * @param start the start index
     * @param end   the end index (inclusive)
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return set of members in reverse order (highest score first)
     */
    <T> Set<T> reverseRange(String key, long start, long end, Class<T> type);

    /**
     * Get members by score range.
     *
     * @param key      the sorted set key
     * @param minScore minimum score (inclusive)
     * @param maxScore maximum score (inclusive)
     * @param type     the expected value type
     * @param <T>      the type parameter
     * @return set of members with scores in the range
     */
    <T> Set<T> rangeByScore(String key, double minScore, double maxScore, Class<T> type);

    /**
     * Get members by score range in reverse order.
     *
     * @param key      the sorted set key
     * @param minScore minimum score (inclusive)
     * @param maxScore maximum score (inclusive)
     * @param type     the expected value type
     * @param <T>      the type parameter
     * @return set of members in reverse order
     */
    <T> Set<T> reverseRangeByScore(String key, double minScore, double maxScore, Class<T> type);

    /**
     * Get the number of members in the sorted set.
     *
     * @param key the sorted set key
     * @return the size of the sorted set
     */
    long size(String key);

    /**
     * Count members with scores in a range.
     *
     * @param key      the sorted set key
     * @param minScore minimum score (inclusive)
     * @param maxScore maximum score (inclusive)
     * @return number of members in the score range
     */
    long count(String key, double minScore, double maxScore);

    /**
     * Remove members by rank range.
     *
     * @param key   the sorted set key
     * @param start the start rank
     * @param end   the end rank (inclusive)
     * @return number of members removed
     */
    long removeRange(String key, long start, long end);

    /**
     * Remove members by score range.
     *
     * @param key      the sorted set key
     * @param minScore minimum score (inclusive)
     * @param maxScore maximum score (inclusive)
     * @return number of members removed
     */
    long removeRangeByScore(String key, double minScore, double maxScore);
}

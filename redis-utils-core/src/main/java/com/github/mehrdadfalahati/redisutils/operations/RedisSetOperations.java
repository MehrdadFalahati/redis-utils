package com.github.mehrdadfalahati.redisutils.operations;

import java.util.List;
import java.util.Set;

/**
 * Operations on Redis sets.
 * Sets are unordered collections of unique strings.
 */
public interface RedisSetOperations {

    /**
     * Add one or more members to a set.
     *
     * @param key     the set key
     * @param members the members to add
     * @return number of members actually added (excluding already existing ones)
     */
    long add(String key, Object... members);

    /**
     * Remove one or more members from a set.
     *
     * @param key     the set key
     * @param members the members to remove
     * @return number of members actually removed
     */
    long remove(String key, Object... members);

    /**
     * Pop (remove and return) a random member from the set.
     *
     * @param key  the set key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return the popped member, or null if set is empty
     */
    <T> T pop(String key, Class<T> type);

    /**
     * Pop multiple random members from the set.
     *
     * @param key   the set key
     * @param count number of members to pop
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return list of popped members
     */
    <T> List<T> pop(String key, long count, Class<T> type);

    /**
     * Get all members of the set.
     *
     * @param key  the set key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return set of all members
     */
    <T> Set<T> members(String key, Class<T> type);

    /**
     * Check if a member exists in the set.
     *
     * @param key    the set key
     * @param member the member to check
     * @return true if member exists in the set
     */
    boolean isMember(String key, Object member);

    /**
     * Get the number of members in the set.
     *
     * @param key the set key
     * @return size of the set
     */
    long size(String key);

    /**
     * Get one or more random members without removing them.
     *
     * @param key  the set key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return a random member, or null if set is empty
     */
    <T> T randomMember(String key, Class<T> type);

    /**
     * Get multiple random members without removing them.
     *
     * @param key   the set key
     * @param count number of members to get
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return list of random members (may contain duplicates if count > size)
     */
    <T> List<T> randomMembers(String key, long count, Class<T> type);

    /**
     * Get multiple distinct random members without removing them.
     *
     * @param key   the set key
     * @param count number of distinct members to get
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return set of random members (limited by set size)
     */
    <T> Set<T> distinctRandomMembers(String key, long count, Class<T> type);

    /**
     * Get the difference between the first set and all successive sets.
     *
     * @param keys the set keys (first key minus all others)
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return set containing the difference
     */
    <T> Set<T> difference(List<String> keys, Class<T> type);

    /**
     * Get the intersection of all given sets.
     *
     * @param keys the set keys to intersect
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return set containing the intersection
     */
    <T> Set<T> intersect(List<String> keys, Class<T> type);

    /**
     * Get the union of all given sets.
     *
     * @param keys the set keys to union
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return set containing the union
     */
    <T> Set<T> union(List<String> keys, Class<T> type);
}

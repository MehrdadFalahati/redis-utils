package com.github.mehrdadfalahati.redisutils.operations;

import java.util.List;

/**
 * Operations on Redis lists.
 * Lists are ordered collections of strings, supporting operations at both ends.
 */
public interface RedisListOperations {

    /**
     * Push values to the left (head) of the list.
     *
     * @param key    the list key
     * @param values the values to push
     * @return the length of the list after push
     */
    long leftPush(String key, Object... values);

    /**
     * Push a value to the left only if the list exists.
     *
     * @param key   the list key
     * @param value the value to push
     * @return the length of the list after push
     */
    long leftPushIfPresent(String key, Object value);

    /**
     * Push values to the right (tail) of the list.
     *
     * @param key    the list key
     * @param values the values to push
     * @return the length of the list after push
     */
    long rightPush(String key, Object... values);

    /**
     * Push a value to the right only if the list exists.
     *
     * @param key   the list key
     * @param value the value to push
     * @return the length of the list after push
     */
    long rightPushIfPresent(String key, Object value);

    /**
     * Pop (remove and return) a value from the left (head) of the list.
     *
     * @param key  the list key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return the popped value, or null if list is empty
     */
    <T> T leftPop(String key, Class<T> type);

    /**
     * Pop (remove and return) a value from the right (tail) of the list.
     *
     * @param key  the list key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return the popped value, or null if list is empty
     */
    <T> T rightPop(String key, Class<T> type);

    /**
     * Get a range of values from the list.
     * Use 0 to -1 to get all elements.
     *
     * @param key   the list key
     * @param start the start index (0-based)
     * @param end   the end index (inclusive, use -1 for last element)
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return list of values in the specified range
     */
    <T> List<T> range(String key, long start, long end, Class<T> type);

    /**
     * Trim the list to the specified range.
     * Elements outside the range are removed.
     *
     * @param key   the list key
     * @param start the start index
     * @param end   the end index
     */
    void trim(String key, long start, long end);

    /**
     * Get the length of the list.
     *
     * @param key the list key
     * @return the length of the list
     */
    long size(String key);

    /**
     * Get an element at a specific index.
     *
     * @param key   the list key
     * @param index the index (0-based)
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return the value at the index, or null if index is out of range
     */
    <T> T index(String key, long index, Class<T> type);

    /**
     * Set the value at a specific index.
     *
     * @param key   the list key
     * @param index the index
     * @param value the value to set
     */
    void set(String key, long index, Object value);

    /**
     * Remove the first N occurrences of a value from the list.
     * Count > 0: Remove from head to tail
     * Count < 0: Remove from tail to head
     * Count = 0: Remove all occurrences
     *
     * @param key   the list key
     * @param count number of occurrences to remove
     * @param value the value to remove
     * @return number of removed elements
     */
    long remove(String key, long count, Object value);
}

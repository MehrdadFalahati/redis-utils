package com.github.mehrdadfalahati.redisutils.operations;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Operations on Redis hashes.
 * Hashes are maps between string fields and values, ideal for representing objects.
 */
public interface RedisHashOperations {

    /**
     * Set a single field in a hash.
     *
     * @param key   the hash key
     * @param field the field name
     * @param value the value to set
     */
    void put(String key, String field, Object value);

    /**
     * Set multiple fields in a hash.
     *
     * @param key    the hash key
     * @param values map of field names to values
     */
    void putAll(String key, Map<String, Object> values);

    /**
     * Set field only if it doesn't exist.
     *
     * @param key   the hash key
     * @param field the field name
     * @param value the value to set
     * @return true if field was set, false if it already exists
     */
    boolean putIfAbsent(String key, String field, Object value);

    /**
     * Get a single field from a hash.
     *
     * @param key   the hash key
     * @param field the field name
     * @param type  the expected value type
     * @param <T>   the type parameter
     * @return the value, or null if field doesn't exist
     */
    <T> T get(String key, String field, Class<T> type);

    /**
     * Get multiple fields from a hash.
     *
     * @param key    the hash key
     * @param fields the field names to get
     * @param type   the expected value type
     * @param <T>    the type parameter
     * @return map of field names to values (null for non-existent fields)
     */
    <T> Map<String, T> multiGet(String key, List<String> fields, Class<T> type);

    /**
     * Get all fields and values from a hash.
     *
     * @param key  the hash key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return map of all field names to values
     */
    <T> Map<String, T> entries(String key, Class<T> type);

    /**
     * Get all field names in a hash.
     *
     * @param key the hash key
     * @return set of field names
     */
    Set<String> keys(String key);

    /**
     * Get all values in a hash.
     *
     * @param key  the hash key
     * @param type the expected value type
     * @param <T>  the type parameter
     * @return list of values
     */
    <T> List<T> values(String key, Class<T> type);

    /**
     * Check if a field exists in a hash.
     *
     * @param key   the hash key
     * @param field the field name
     * @return true if field exists
     */
    boolean hasKey(String key, String field);

    /**
     * Delete one or more fields from a hash.
     *
     * @param key    the hash key
     * @param fields the fields to delete
     * @return number of fields deleted
     */
    long delete(String key, String... fields);

    /**
     * Get the number of fields in a hash.
     *
     * @param key the hash key
     * @return number of fields
     */
    long size(String key);

    /**
     * Increment a numeric field in a hash.
     *
     * @param key   the hash key
     * @param field the field name
     * @param delta the increment amount
     * @return the value after increment
     */
    long increment(String key, String field, long delta);

    /**
     * Increment a floating-point field in a hash.
     *
     * @param key   the hash key
     * @param field the field name
     * @param delta the increment amount
     * @return the value after increment
     */
    double increment(String key, String field, double delta);
}

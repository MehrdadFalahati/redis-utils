package com.github.mehrdadfalahati.redisutils.serialization;

/**
 * Custom abstraction for Redis value serialization.
 * Provides a simple interface for serializing and deserializing objects.
 *
 * @param <T> the type of object to serialize/deserialize
 */
public interface RedisValueSerializer<T> {

    /**
     * Serialize an object to bytes for storage in Redis.
     *
     * @param value the object to serialize
     * @return byte array representation
     * @throws SerializationException if serialization fails
     */
    byte[] serialize(T value) throws SerializationException;

    /**
     * Deserialize bytes from Redis into an object.
     *
     * @param bytes the byte array to deserialize
     * @return the deserialized object
     * @throws SerializationException if deserialization fails
     */
    T deserialize(byte[] bytes) throws SerializationException;

    /**
     * Get the type this serializer handles.
     *
     * @return the target class type
     */
    Class<T> getType();
}

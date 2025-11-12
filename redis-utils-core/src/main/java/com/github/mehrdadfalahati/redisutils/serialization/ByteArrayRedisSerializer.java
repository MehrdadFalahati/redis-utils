package com.github.mehrdadfalahati.redisutils.serialization;

/**
 * Pass-through serializer for byte arrays.
 * Simply returns the byte array as-is without any transformation.
 */
public class ByteArrayRedisSerializer implements RedisValueSerializer<byte[]> {

    @Override
    public byte[] serialize(byte[] value) {
        return value;
    }

    @Override
    public byte[] deserialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }
}

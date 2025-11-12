package com.github.mehrdadfalahati.redisutils.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ByteArrayRedisSerializer.
 * Demonstrates how to use the byte array serializer for binary data.
 */
@DisplayName("ByteArrayRedisSerializer Tests")
class ByteArrayRedisSerializerTest {

    private ByteArrayRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new ByteArrayRedisSerializer();
    }

    @Test
    @DisplayName("Should serialize and deserialize byte array")
    void testSerializeDeserializeByteArray() throws SerializationException {
        // Given
        byte[] value = new byte[]{1, 2, 3, 4, 5};

        // When
        byte[] serialized = serializer.serialize(value);
        byte[] deserialized = serializer.deserialize(serialized);

        // Then
        assertNotNull(serialized);
        assertArrayEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should handle null values during serialization")
    void testSerializeNull() throws SerializationException {
        // When
        byte[] result = serializer.serialize(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null values during deserialization")
    void testDeserializeNull() throws SerializationException {
        // When
        byte[] result = serializer.deserialize(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty byte array")
    void testEmptyByteArray() throws SerializationException {
        // Given
        byte[] value = new byte[0];

        // When
        byte[] serialized = serializer.serialize(value);
        byte[] deserialized = serializer.deserialize(serialized);

        // Then
        assertNotNull(serialized);
        assertEquals(0, serialized.length);
        assertArrayEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should return the same byte array reference")
    void testReturnsSameReference() throws SerializationException {
        // Given
        byte[] value = new byte[]{1, 2, 3};

        // When
        byte[] serialized = serializer.serialize(value);

        // Then - Should return the same reference (pass-through)
        assertSame(value, serialized);
    }

    @Test
    @DisplayName("Should handle large byte arrays")
    void testLargeByteArray() throws SerializationException {
        // Given
        byte[] value = new byte[10000];
        Arrays.fill(value, (byte) 42);

        // When
        byte[] serialized = serializer.serialize(value);
        byte[] deserialized = serializer.deserialize(serialized);

        // Then
        assertArrayEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should preserve all byte values (0-255)")
    void testAllByteValues() throws SerializationException {
        // Given
        byte[] value = new byte[256];
        for (int i = 0; i < 256; i++) {
            value[i] = (byte) i;
        }

        // When
        byte[] serialized = serializer.serialize(value);
        byte[] deserialized = serializer.deserialize(serialized);

        // Then
        assertArrayEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should return correct type")
    void testGetType() {
        // When
        Class<byte[]> type = serializer.getType();

        // Then
        assertEquals(byte[].class, type);
    }
}

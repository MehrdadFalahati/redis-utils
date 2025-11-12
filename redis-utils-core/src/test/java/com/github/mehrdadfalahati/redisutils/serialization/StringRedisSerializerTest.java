package com.github.mehrdadfalahati.redisutils.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringRedisSerializer.
 * Demonstrates how to use the string serializer for Redis keys and simple values.
 */
@DisplayName("StringRedisSerializer Tests")
class StringRedisSerializerTest {

    private StringRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new StringRedisSerializer();
    }

    @Test
    @DisplayName("Should serialize and deserialize simple string")
    void testSerializeDeserializeSimpleString() throws SerializationException {
        // Given
        String value = "Hello Redis!";

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertNotNull(serialized);
        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should serialize and deserialize empty string")
    void testSerializeDeserializeEmptyString() throws SerializationException {
        // Given
        String value = "";

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertNotNull(serialized);
        assertEquals(0, serialized.length);
        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should serialize and deserialize Unicode characters")
    void testSerializeDeserializeUnicode() throws SerializationException {
        // Given
        String value = "Hello 世界 🌍 مرحبا";

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertNotNull(serialized);
        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should serialize and deserialize special characters")
    void testSerializeDeserializeSpecialCharacters() throws SerializationException {
        // Given
        String value = "Special: \n\t\r\\\"'{}[]";

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should handle null value during serialization")
    void testSerializeNull() throws SerializationException {
        // When
        byte[] result = serializer.serialize(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null value during deserialization")
    void testDeserializeNull() throws SerializationException {
        // When
        String result = serializer.deserialize(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty byte array during deserialization")
    void testDeserializeEmptyArray() throws SerializationException {
        // When
        String result = serializer.deserialize(new byte[0]);

        // Then
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should use UTF-8 encoding")
    void testUsesUtf8Encoding() throws SerializationException {
        // Given
        String value = "Test UTF-8";

        // When
        byte[] serialized = serializer.serialize(value);
        byte[] expected = value.getBytes(StandardCharsets.UTF_8);

        // Then
        assertArrayEquals(expected, serialized);
    }

    @Test
    @DisplayName("Should serialize and deserialize long strings")
    void testSerializeDeserializeLongString() throws SerializationException {
        // Given
        String value = "A".repeat(10000);

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertEquals(value, deserialized);
        assertEquals(10000, deserialized.length());
    }

    @Test
    @DisplayName("Should preserve string during multiple round trips")
    void testMultipleRoundTrips() throws SerializationException {
        // Given
        String original = "Multi-trip test";

        // When - Multiple serialization/deserialization cycles
        byte[] serialized1 = serializer.serialize(original);
        String deserialized1 = serializer.deserialize(serialized1);
        byte[] serialized2 = serializer.serialize(deserialized1);
        String deserialized2 = serializer.deserialize(serialized2);

        // Then
        assertEquals(original, deserialized2);
    }

    @Test
    @DisplayName("Should handle numeric strings")
    void testNumericStrings() throws SerializationException {
        // Given
        String value = "123456789";

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should handle JSON-like strings")
    void testJsonLikeStrings() throws SerializationException {
        // Given
        String value = "{\"key\":\"value\",\"number\":123}";

        // When
        byte[] serialized = serializer.serialize(value);
        String deserialized = serializer.deserialize(serialized);

        // Then
        assertEquals(value, deserialized);
    }
}

package com.github.mehrdadfalahati.redisutils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonRedisSerializer.
 * Demonstrates how to use the JSON serializer for custom Redis serialization.
 */
@DisplayName("JsonRedisSerializer Tests")
class JsonRedisSerializerTest {

    private ObjectMapper objectMapper;
    private JsonRedisSerializer<TestUser> userSerializer;
    private JsonRedisSerializer<TestProduct> productSerializer;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper with Java 8 time support
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        userSerializer = new JsonRedisSerializer<>(objectMapper, TestUser.class);
        productSerializer = new JsonRedisSerializer<>(objectMapper, TestProduct.class);
    }

    @Test
    @DisplayName("Should serialize and deserialize simple object")
    void testSerializeDeserializeSimpleObject() throws SerializationException {
        // Given
        TestUser user = new TestUser("user123", "John Doe", "john@example.com");

        // When
        byte[] serialized = userSerializer.serialize(user);
        TestUser deserialized = userSerializer.deserialize(serialized);

        // Then
        assertNotNull(serialized);
        assertNotNull(deserialized);
        assertEquals(user.getId(), deserialized.getId());
        assertEquals(user.getName(), deserialized.getName());
        assertEquals(user.getEmail(), deserialized.getEmail());
    }

    @Test
    @DisplayName("Should serialize and deserialize object with Java 8 time types")
    void testSerializeDeserializeWithJavaTime() throws SerializationException {
        // Given
        TestProduct product = new TestProduct(
                "prod123",
                "Laptop",
                999.99,
                LocalDateTime.of(2024, 1, 15, 10, 30)
        );

        // When
        byte[] serialized = productSerializer.serialize(product);
        TestProduct deserialized = productSerializer.deserialize(serialized);

        // Then
        assertNotNull(deserialized);
        assertEquals(product.getId(), deserialized.getId());
        assertEquals(product.getName(), deserialized.getName());
        assertEquals(product.getPrice(), deserialized.getPrice());
        assertEquals(product.getCreatedAt(), deserialized.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle null values during serialization")
    void testSerializeNull() throws SerializationException {
        // When
        byte[] result = userSerializer.serialize(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null values during deserialization")
    void testDeserializeNull() throws SerializationException {
        // When
        TestUser result = userSerializer.deserialize(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty byte array during deserialization")
    void testDeserializeEmptyArray() throws SerializationException {
        // When
        TestUser result = userSerializer.deserialize(new byte[0]);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should produce valid JSON bytes")
    void testProducesValidJson() throws SerializationException {
        // Given
        TestUser user = new TestUser("user123", "John Doe", "john@example.com");

        // When
        byte[] serialized = userSerializer.serialize(user);
        String json = new String(serialized);

        // Then
        assertTrue(json.contains("\"id\":\"user123\""));
        assertTrue(json.contains("\"name\":\"John Doe\""));
        assertTrue(json.contains("\"email\":\"john@example.com\""));
    }

    @Test
    @DisplayName("Should throw SerializationException for invalid JSON during deserialization")
    void testDeserializeInvalidJson() {
        // Given
        byte[] invalidJson = "not a valid json".getBytes();

        // When/Then
        assertThrows(SerializationException.class, () -> {
            userSerializer.deserialize(invalidJson);
        });
    }

    @Test
    @DisplayName("Should preserve all fields during round-trip serialization")
    void testRoundTripPreservesFields() throws SerializationException {
        // Given
        TestUser original = new TestUser("user123", "John Doe", "john@example.com");

        // When - Multiple round trips
        byte[] serialized1 = userSerializer.serialize(original);
        TestUser deserialized1 = userSerializer.deserialize(serialized1);
        byte[] serialized2 = userSerializer.serialize(deserialized1);
        TestUser deserialized2 = userSerializer.deserialize(serialized2);

        // Then
        assertEquals(original.getId(), deserialized2.getId());
        assertEquals(original.getName(), deserialized2.getName());
        assertEquals(original.getEmail(), deserialized2.getEmail());
    }

    // Test DTOs
    static class TestUser {
        private String id;
        private String name;
        private String email;

        public TestUser() {
        }

        public TestUser(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    static class TestProduct {
        private String id;
        private String name;
        private Double price;
        private LocalDateTime createdAt;

        public TestProduct() {
        }

        public TestProduct(String id, String name, Double price, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}

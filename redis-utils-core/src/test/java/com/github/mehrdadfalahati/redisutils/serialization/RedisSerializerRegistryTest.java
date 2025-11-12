package com.github.mehrdadfalahati.redisutils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RedisSerializerRegistry.
 * Demonstrates how to use the serializer registry to manage multiple serializers.
 */
@DisplayName("RedisSerializerRegistry Tests")
class RedisSerializerRegistryTest {

    private RedisSerializerRegistry registry;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        registry = new RedisSerializerRegistry(objectMapper);
    }

    @Test
    @DisplayName("Should register and retrieve String serializer")
    void testRegisterStringSerializer() {
        // Given
        StringRedisSerializer serializer = new StringRedisSerializer();

        // When
        registry.register(serializer);
        RedisValueSerializer<String> retrieved = registry.getSerializer(String.class);

        // Then
        assertNotNull(retrieved);
        assertSame(serializer, retrieved);
    }

    @Test
    @DisplayName("Should register and retrieve custom object serializer")
    void testRegisterCustomSerializer() {
        // Given
        JsonRedisSerializer<TestOrder> serializer = new JsonRedisSerializer<>(objectMapper, TestOrder.class);

        // When
        registry.register(serializer);
        RedisValueSerializer<TestOrder> retrieved = registry.getSerializer(TestOrder.class);

        // Then
        assertNotNull(retrieved);
        assertSame(serializer, retrieved);
    }

    @Test
    @DisplayName("Should use registered serializer for serialization")
    void testSerializationWithRegistry() throws SerializationException {
        // Given
        JsonRedisSerializer<TestOrder> serializer = new JsonRedisSerializer<>(objectMapper, TestOrder.class);
        registry.register(serializer);

        TestOrder order = new TestOrder(
                "order123",
                "John Doe",
                299.99,
                LocalDateTime.of(2024, 1, 15, 14, 30)
        );

        // When
        RedisValueSerializer<TestOrder> retrieved = registry.getSerializer(TestOrder.class);
        byte[] serialized = retrieved.serialize(order);
        TestOrder deserialized = retrieved.deserialize(serialized);

        // Then
        assertNotNull(deserialized);
        assertEquals(order.getId(), deserialized.getId());
        assertEquals(order.getCustomerName(), deserialized.getCustomerName());
        assertEquals(order.getTotalAmount(), deserialized.getTotalAmount());
        assertEquals(order.getOrderDate(), deserialized.getOrderDate());
    }

    @Test
    @DisplayName("Should auto-create JSON serializer for unregistered type")
    void testGetSerializerForUnregisteredType() {
        // When - TestOrder is not explicitly registered
        RedisValueSerializer<TestOrder> serializer = registry.getSerializer(TestOrder.class);

        // Then - Registry auto-creates a JSON serializer
        assertNotNull(serializer);
        assertTrue(serializer instanceof JsonRedisSerializer);
    }

    @Test
    @DisplayName("Should check if type is registered")
    void testHasSerializer() {
        // Given - String is registered by default, TestOrder is not

        // When/Then - String should be registered
        assertTrue(registry.hasSerializer(String.class));

        // TestOrder should not be registered initially
        assertFalse(registry.hasSerializer(TestOrder.class));

        // After registering
        JsonRedisSerializer<TestOrder> serializer = new JsonRedisSerializer<>(objectMapper, TestOrder.class);
        registry.register(serializer);
        assertTrue(registry.hasSerializer(TestOrder.class));
    }

    @Test
    @DisplayName("Should support multiple serializer types")
    void testMultipleSerializerTypes() {
        // Given
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        ByteArrayRedisSerializer byteArraySerializer = new ByteArrayRedisSerializer();
        JsonRedisSerializer<TestOrder> orderSerializer = new JsonRedisSerializer<>(objectMapper, TestOrder.class);

        // When
        registry.register(stringSerializer);
        registry.register(byteArraySerializer);
        registry.register(orderSerializer);

        // Then
        assertTrue(registry.hasSerializer(String.class));
        assertTrue(registry.hasSerializer(byte[].class));
        assertTrue(registry.hasSerializer(TestOrder.class));

        assertSame(stringSerializer, registry.getSerializer(String.class));
        assertSame(byteArraySerializer, registry.getSerializer(byte[].class));
        assertSame(orderSerializer, registry.getSerializer(TestOrder.class));
    }

    @Test
    @DisplayName("Should replace existing serializer when registering same type")
    void testReplaceSerializer() {
        // Given
        StringRedisSerializer serializer1 = new StringRedisSerializer();
        StringRedisSerializer serializer2 = new StringRedisSerializer();

        // When
        registry.register(serializer1);
        RedisValueSerializer<String> first = registry.getSerializer(String.class);

        registry.register(serializer2);
        RedisValueSerializer<String> second = registry.getSerializer(String.class);

        // Then
        assertSame(serializer1, first);
        assertSame(serializer2, second);
        assertNotSame(first, second);
    }

    @Test
    @DisplayName("Should handle null serializer parameter")
    void testNullSerializerParameter() {
        // When/Then - Registering null serializer should throw exception
        assertThrows(NullPointerException.class, () -> {
            registry.register(null);
        });
    }

    @Test
    @DisplayName("Should unregister serializer")
    void testUnregisterSerializer() {
        // Given
        JsonRedisSerializer<TestOrder> serializer = new JsonRedisSerializer<>(objectMapper, TestOrder.class);
        registry.register(serializer);
        assertTrue(registry.hasSerializer(TestOrder.class));

        // When
        registry.unregister(TestOrder.class);

        // Then
        assertFalse(registry.hasSerializer(TestOrder.class));
    }

    @Test
    @DisplayName("Should clear non-default serializers")
    void testClearSerializers() {
        // Given
        JsonRedisSerializer<TestOrder> serializer = new JsonRedisSerializer<>(objectMapper, TestOrder.class);
        registry.register(serializer);

        // When
        registry.clear();

        // Then - Custom serializer should be removed
        assertFalse(registry.hasSerializer(TestOrder.class));

        // But defaults should still be there
        assertTrue(registry.hasSerializer(String.class));
        assertTrue(registry.hasSerializer(byte[].class));
    }

    // Test DTO
    static class TestOrder {
        private String id;
        private String customerName;
        private Double totalAmount;
        private LocalDateTime orderDate;

        public TestOrder() {
        }

        public TestOrder(String id, String customerName, Double totalAmount, LocalDateTime orderDate) {
            this.id = id;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.orderDate = orderDate;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
        }
    }
}

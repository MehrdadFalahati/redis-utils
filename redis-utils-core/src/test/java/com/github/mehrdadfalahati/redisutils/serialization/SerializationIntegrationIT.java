package com.github.mehrdadfalahati.redisutils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating real-world usage of custom serialization classes
 * with actual Redis operations.
 *
 * This test shows how to:
 * 1. Use different serializers for different data types
 * 2. Store and retrieve data from Redis
 * 3. Manage multiple serializers with a registry
 * 4. Handle complex objects with JSON serialization
 */
@SpringBootTest
@Testcontainers
@DisplayName("Serialization Integration Tests with Real Redis")
class SerializationIntegrationIT {

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port",
                () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    private RedisSerializerRegistry serializerRegistry;
    private ObjectMapper objectMapper;

    // Serializers
    private StringRedisSerializer stringSerializer;
    private JsonRedisSerializer<Product> productSerializer;
    private JsonRedisSerializer<Order> orderSerializer;
    private ByteArrayRedisSerializer byteArraySerializer;

    @BeforeEach
    void setUp() {
        // Setup ObjectMapper with Java 8 time support
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        // Initialize serializer registry
        serializerRegistry = new RedisSerializerRegistry(objectMapper);

        // Create serializers
        stringSerializer = new StringRedisSerializer();
        productSerializer = new JsonRedisSerializer<>(objectMapper, Product.class);
        orderSerializer = new JsonRedisSerializer<>(objectMapper, Order.class);
        byteArraySerializer = new ByteArrayRedisSerializer();

        // Register custom serializers
        serializerRegistry.register(productSerializer);
        serializerRegistry.register(orderSerializer);

        // Clear Redis before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("Should store and retrieve simple string values")
    void testStringSerializerWithRedis() throws SerializationException {
        // Given
        String key = "config:app-name";
        String value = "Redis Utils Application";

        // When - Serialize and store
        byte[] serialized = stringSerializer.serialize(value);
        redisTemplate.opsForValue().set(key, serialized);

        // Then - Retrieve and deserialize
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        String deserialized = stringSerializer.deserialize(retrieved);

        assertNotNull(deserialized);
        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should store and retrieve complex Product objects")
    void testProductJsonSerializerWithRedis() throws SerializationException {
        // Given
        String key = "product:laptop-001";
        Product product = new Product(
                "laptop-001",
                "Dell XPS 15",
                1299.99,
                "Electronics",
                LocalDateTime.of(2024, 1, 15, 10, 30)
        );

        // When - Serialize and store
        byte[] serialized = productSerializer.serialize(product);
        redisTemplate.opsForValue().set(key, serialized);

        // Then - Retrieve and deserialize
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        Product deserialized = productSerializer.deserialize(retrieved);

        assertNotNull(deserialized);
        assertEquals(product.getId(), deserialized.getId());
        assertEquals(product.getName(), deserialized.getName());
        assertEquals(product.getPrice(), deserialized.getPrice());
        assertEquals(product.getCategory(), deserialized.getCategory());
        assertEquals(product.getCreatedAt(), deserialized.getCreatedAt());
    }

    @Test
    @DisplayName("Should store and retrieve Order with items list")
    void testOrderJsonSerializerWithRedis() throws SerializationException {
        // Given
        String key = "order:order-123";
        Order order = new Order(
                "order-123",
                "john.doe@example.com",
                Arrays.asList("item1", "item2", "item3"),
                599.99,
                "PENDING",
                LocalDateTime.of(2024, 1, 20, 14, 45)
        );

        // When - Serialize and store
        byte[] serialized = orderSerializer.serialize(order);
        redisTemplate.opsForValue().set(key, serialized);

        // Then - Retrieve and deserialize
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        Order deserialized = orderSerializer.deserialize(retrieved);

        assertNotNull(deserialized);
        assertEquals(order.getId(), deserialized.getId());
        assertEquals(order.getCustomerEmail(), deserialized.getCustomerEmail());
        assertEquals(order.getItems().size(), deserialized.getItems().size());
        assertEquals(order.getTotalAmount(), deserialized.getTotalAmount());
        assertEquals(order.getStatus(), deserialized.getStatus());
        assertEquals(order.getOrderDate(), deserialized.getOrderDate());
    }

    @Test
    @DisplayName("Should store and retrieve binary data")
    void testByteArraySerializerWithRedis() throws SerializationException {
        // Given
        String key = "file:image-001";
        byte[] imageData = new byte[]{0x00, 0x01, 0x02, 0x03, (byte) 0xFF};

        // When - Serialize and store (pass-through)
        byte[] serialized = byteArraySerializer.serialize(imageData);
        redisTemplate.opsForValue().set(key, serialized);

        // Then - Retrieve and deserialize
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        byte[] deserialized = byteArraySerializer.deserialize(retrieved);

        assertNotNull(deserialized);
        assertArrayEquals(imageData, deserialized);
    }

    @Test
    @DisplayName("Should handle multiple data types using registry")
    void testRegistryWithMultipleTypesInRedis() throws SerializationException {
        // Given - Multiple objects of different types
        Product product = new Product(
                "prod-001",
                "Wireless Mouse",
                29.99,
                "Accessories",
                LocalDateTime.now()
        );

        Order order = new Order(
                "order-456",
                "jane@example.com",
                Arrays.asList("prod-001"),
                29.99,
                "SHIPPED",
                LocalDateTime.now()
        );

        // When - Store different types
        RedisValueSerializer<Product> prodSerializer = serializerRegistry.getSerializer(Product.class);
        byte[] prodBytes = prodSerializer.serialize(product);
        redisTemplate.opsForValue().set("product:prod-001", prodBytes);

        RedisValueSerializer<Order> ordSerializer = serializerRegistry.getSerializer(Order.class);
        byte[] orderBytes = ordSerializer.serialize(order);
        redisTemplate.opsForValue().set("order:order-456", orderBytes);

        // Then - Retrieve and verify both types
        byte[] retrievedProdBytes = redisTemplate.opsForValue().get("product:prod-001");
        Product retrievedProduct = prodSerializer.deserialize(retrievedProdBytes);

        byte[] retrievedOrderBytes = redisTemplate.opsForValue().get("order:order-456");
        Order retrievedOrder = ordSerializer.deserialize(retrievedOrderBytes);

        assertNotNull(retrievedProduct);
        assertEquals(product.getId(), retrievedProduct.getId());
        assertEquals(product.getName(), retrievedProduct.getName());

        assertNotNull(retrievedOrder);
        assertEquals(order.getId(), retrievedOrder.getId());
        assertEquals(order.getCustomerEmail(), retrievedOrder.getCustomerEmail());
    }

    @Test
    @DisplayName("Should handle Unicode strings in Redis")
    void testUnicodeStringWithRedis() throws SerializationException {
        // Given
        String key = "message:multilingual";
        String value = "Hello 世界 مرحبا 🌍";

        // When
        byte[] serialized = stringSerializer.serialize(value);
        redisTemplate.opsForValue().set(key, serialized);

        // Then
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        String deserialized = stringSerializer.deserialize(retrieved);

        assertEquals(value, deserialized);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValueHandling() throws SerializationException {
        // Given
        String key = "test:null-value";

        // When - Try to get non-existent key
        byte[] retrieved = redisTemplate.opsForValue().get(key);

        // Then - Should be null
        assertNull(retrieved);

        // And deserializing null should also return null
        Product deserialized = productSerializer.deserialize(retrieved);
        assertNull(deserialized);
    }

    @Test
    @DisplayName("Should verify data persistence in Redis")
    void testDataPersistence() throws SerializationException {
        // Given
        String key = "persistent:product";
        Product product = new Product(
                "persist-001",
                "Test Product",
                99.99,
                "Test",
                LocalDateTime.now()
        );

        // When - Store data
        byte[] serialized = productSerializer.serialize(product);
        redisTemplate.opsForValue().set(key, serialized);

        // Then - Verify data exists
        Boolean exists = redisTemplate.hasKey(key);
        assertTrue(exists);

        // And can be retrieved multiple times
        byte[] retrieved1 = redisTemplate.opsForValue().get(key);
        byte[] retrieved2 = redisTemplate.opsForValue().get(key);

        Product deserialized1 = productSerializer.deserialize(retrieved1);
        Product deserialized2 = productSerializer.deserialize(retrieved2);

        assertEquals(product.getId(), deserialized1.getId());
        assertEquals(product.getId(), deserialized2.getId());
    }

    @Test
    @DisplayName("Should update existing values in Redis")
    void testUpdateValueInRedis() throws SerializationException {
        // Given
        String key = "product:update-test";
        Product originalProduct = new Product(
                "update-001",
                "Original Name",
                100.00,
                "Category",
                LocalDateTime.now()
        );

        // When - Store original
        byte[] originalBytes = productSerializer.serialize(originalProduct);
        redisTemplate.opsForValue().set(key, originalBytes);

        // Then update
        Product updatedProduct = new Product(
                "update-001",
                "Updated Name",
                150.00,
                "New Category",
                LocalDateTime.now()
        );
        byte[] updatedBytes = productSerializer.serialize(updatedProduct);
        redisTemplate.opsForValue().set(key, updatedBytes);

        // Verify updated value
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        Product deserialized = productSerializer.deserialize(retrieved);

        assertEquals("Updated Name", deserialized.getName());
        assertEquals(150.00, deserialized.getPrice());
        assertEquals("New Category", deserialized.getCategory());
    }

    @Test
    @DisplayName("Should delete values from Redis")
    void testDeleteValueFromRedis() throws SerializationException {
        // Given
        String key = "product:delete-test";
        Product product = new Product(
                "delete-001",
                "Test Product",
                99.99,
                "Test",
                LocalDateTime.now()
        );

        // When - Store and verify
        byte[] serialized = productSerializer.serialize(product);
        redisTemplate.opsForValue().set(key, serialized);
        assertTrue(redisTemplate.hasKey(key));

        // Then delete
        redisTemplate.delete(key);

        // Verify deletion
        assertFalse(redisTemplate.hasKey(key));
        byte[] retrieved = redisTemplate.opsForValue().get(key);
        assertNull(retrieved);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    // Test DTOs
    static class Product {
        private String id;
        private String name;
        private Double price;
        private String category;
        private LocalDateTime createdAt;

        public Product() {
        }

        public Product(String id, String name, Double price, String category, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
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

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    static class Order {
        private String id;
        private String customerEmail;
        private List<String> items;
        private Double totalAmount;
        private String status;
        private LocalDateTime orderDate;

        public Order() {
        }

        public Order(String id, String customerEmail, List<String> items,
                     Double totalAmount, String status, LocalDateTime orderDate) {
            this.id = id;
            this.customerEmail = customerEmail;
            this.items = items;
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
        }
    }
}

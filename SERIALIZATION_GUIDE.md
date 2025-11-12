# Redis Serialization Guide

This guide demonstrates how to use the custom serialization classes in `redis-utils` for flexible Redis data serialization.

## Overview

The `redis-utils` library provides a custom serialization framework with the following components:

- **`RedisValueSerializer<T>`** - Interface for custom serializers
- **`JsonRedisSerializer<T>`** - JSON serialization using Jackson ObjectMapper
- **`StringRedisSerializer`** - String serialization with UTF-8 encoding
- **`ByteArrayRedisSerializer`** - Pass-through serializer for raw bytes
- **`RedisSerializerRegistry`** - Registry for managing multiple serializers (auto-creates JSON serializers for unregistered types)
- **`SerializationException`** - Exception for serialization errors

## When to Use Custom Serializers

### Default Behavior (Spring Integration)
By default, `redis-utils-spring-boot-starter` uses Spring's `GenericJackson2JsonRedisSerializer` for automatic JSON serialization with type preservation.

```java
@Autowired
private RedisValueOperations redisOps;

// Works out of the box with any serializable object
User user = new User("user123", "John Doe");
redisOps.set(RedisKey.of("user:123"), user);
User retrieved = redisOps.get("user:123", User.class);
```

### When You Need Custom Serializers

Use the custom serialization framework when you need:

1. **Fine-grained control** over serialization format
2. **Multiple serialization strategies** for different data types
3. **Custom formats** (e.g., Protocol Buffers, MessagePack)
4. **Performance optimization** with specific serializers
5. **Compatibility** with existing Redis data in a specific format

## Using JsonRedisSerializer

### Basic Usage

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mehrdadfalahati.redisutils.serialization.*;

// Configure ObjectMapper
ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

// Create serializer for your type
JsonRedisSerializer<User> userSerializer =
    new JsonRedisSerializer<>(objectMapper, User.class);

// Serialize
User user = new User("user123", "John Doe", "john@example.com");
byte[] bytes = userSerializer.serialize(user);

// Deserialize
User retrieved = userSerializer.deserialize(bytes);
```

### With Java 8 Date/Time Types

```java
import java.time.LocalDateTime;

public class Order {
    private String id;
    private String customerName;
    private Double totalAmount;
    private LocalDateTime orderDate;

    // constructors, getters, setters
}

// ObjectMapper with Java Time support
ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

JsonRedisSerializer<Order> serializer =
    new JsonRedisSerializer<>(objectMapper, Order.class);

Order order = new Order(
    "order123",
    "John Doe",
    299.99,
    LocalDateTime.now()
);

byte[] serialized = serializer.serialize(order);
Order deserialized = serializer.deserialize(serialized);
```

### Error Handling

```java
try {
    User user = userSerializer.deserialize(corruptedBytes);
} catch (SerializationException e) {
    logger.error("Failed to deserialize user: {}", e.getMessage());
    // Handle error - maybe return default or retry
}
```

## Using StringRedisSerializer

### Basic String Operations

```java
StringRedisSerializer serializer = new StringRedisSerializer();

// Serialize
String value = "Hello Redis!";
byte[] bytes = serializer.serialize(value);

// Deserialize
String retrieved = serializer.deserialize(bytes);
```

### Use Cases

**1. Redis Keys**
```java
// Serialize keys to ensure consistent encoding
String key = "user:123:profile";
byte[] keyBytes = stringSerializer.serialize(key);
```

**2. Simple String Values**
```java
// Store configuration values
String config = "timeout=5000;retries=3";
byte[] configBytes = stringSerializer.serialize(config);
```

**3. Unicode Support**
```java
// Works with Unicode characters
String multiLang = "Hello 世界 مرحبا 🌍";
byte[] bytes = stringSerializer.serialize(multiLang);
String retrieved = stringSerializer.deserialize(bytes); // Preserves Unicode
```

## Using ByteArrayRedisSerializer

### Binary Data

```java
ByteArrayRedisSerializer serializer = new ByteArrayRedisSerializer();

// Store image data
byte[] imageBytes = Files.readAllBytes(Paths.get("profile.jpg"));
byte[] stored = serializer.serialize(imageBytes); // Pass-through

// Retrieve
byte[] retrieved = serializer.deserialize(stored);
Files.write(Paths.get("retrieved.jpg"), retrieved);
```

### When to Use
- Storing binary files (images, PDFs, etc.)
- Protocol Buffer messages
- MessagePack serialized data
- Any pre-serialized binary data

## Using RedisSerializerRegistry

The registry allows you to manage multiple serializers for different types.

### Basic Setup

```java
import com.github.mehrdadfalahati.redisutils.serialization.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// Create ObjectMapper
ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

// Create registry with ObjectMapper
RedisSerializerRegistry registry = new RedisSerializerRegistry(objectMapper);

// Note: String and byte[] serializers are registered by default
// For unregistered types, the registry auto-creates JSON serializers

// You can optionally register custom serializers for your types
registry.register(new JsonRedisSerializer<>(objectMapper, User.class));
registry.register(new JsonRedisSerializer<>(objectMapper, Order.class));
```

### Using with Different Types

```java
// Get serializer for registered types
RedisValueSerializer<User> userSerializer = registry.getSerializer(User.class);
byte[] userBytes = userSerializer.serialize(user);

RedisValueSerializer<Order> orderSerializer = registry.getSerializer(Order.class);
byte[] orderBytes = orderSerializer.serialize(order);

RedisValueSerializer<String> stringSerializer = registry.getSerializer(String.class);
byte[] stringBytes = stringSerializer.serialize("config value");
```

### Type-Safe Operations

```java
public class SerializationService {
    private final RedisSerializerRegistry registry;
    private final ObjectMapper objectMapper;

    public SerializationService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        this.registry = new RedisSerializerRegistry(objectMapper);
        setupSerializers();
    }

    private void setupSerializers() {
        // String and byte[] are registered by default
        // Register custom types
        registry.register(new JsonRedisSerializer<>(objectMapper, User.class));
        registry.register(new JsonRedisSerializer<>(objectMapper, Order.class));
        registry.register(new JsonRedisSerializer<>(objectMapper, Product.class));
    }

    public <T> byte[] serialize(T object, Class<T> type) throws SerializationException {
        // Registry auto-creates JSON serializer for unregistered types
        RedisValueSerializer<T> serializer = registry.getSerializer(type);
        return serializer.serialize(object);
    }

    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        // Registry auto-creates JSON serializer for unregistered types
        RedisValueSerializer<T> serializer = registry.getSerializer(type);
        return serializer.deserialize(bytes);
    }
}
```

## Creating Custom Serializers

### Implementing RedisValueSerializer

```java
import com.github.mehrdadfalahati.redisutils.serialization.*;
import com.google.protobuf.Message;

/**
 * Example: Protocol Buffers serializer
 */
public class ProtobufRedisSerializer<T extends Message> implements RedisValueSerializer<T> {

    private final Class<T> type;
    private final Parser<T> parser;

    public ProtobufRedisSerializer(Class<T> type, Parser<T> parser) {
        this.type = type;
        this.parser = parser;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return null;
        }
        try {
            return value.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize protobuf", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return parser.parseFrom(bytes);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize protobuf", e);
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}
```

### Using Custom Serializer

```java
// Assuming you have a protobuf-generated class: UserProto
ProtobufRedisSerializer<UserProto> serializer =
    new ProtobufRedisSerializer<>(UserProto.class, UserProto.parser());

UserProto user = UserProto.newBuilder()
    .setId("user123")
    .setName("John Doe")
    .build();

byte[] bytes = serializer.serialize(user);
UserProto retrieved = serializer.deserialize(bytes);
```

## Integration with RedisTemplate

If you want to use custom serializers with Spring's `RedisTemplate`:

### Configuration

```java
@Configuration
public class CustomSerializationConfig {

    @Bean
    public RedisTemplate<String, Object> customRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use custom String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(new RedisSerializer<String>() {
            @Override
            public byte[] serialize(String s) {
                try {
                    return stringSerializer.serialize(s);
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String deserialize(byte[] bytes) {
                try {
                    return stringSerializer.deserialize(bytes);
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Use Spring's default for values (or customize similarly)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();

        return template;
    }
}
```

## Performance Considerations

### Serializer Comparison

| Serializer | Speed | Size | Type Safety | Use Case |
|------------|-------|------|-------------|----------|
| **String** | Fast | Medium | Low | Simple text, keys |
| **JSON** | Medium | Medium | High | Objects, complex data |
| **ByteArray** | Fastest | Smallest | None | Binary data, pre-serialized |
| **Protobuf** | Fast | Small | High | High-performance, cross-language |

### Best Practices

1. **Reuse Serializers** - Create once, use many times
```java
// Good - Reuse serializer
private final JsonRedisSerializer<User> serializer =
    new JsonRedisSerializer<>(objectMapper, User.class);

// Bad - Creating new serializer each time
public byte[] serialize(User user) {
    return new JsonRedisSerializer<>(objectMapper, User.class).serialize(user);
}
```

2. **Pool ObjectMapper** - Share ObjectMapper instances
```java
// Application-wide ObjectMapper
@Bean
public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
}
```

3. **Handle Nulls Properly**
```java
// Always check for null before operations
if (bytes == null || bytes.length == 0) {
    return null;
}
```

4. **Use Type-Specific Serializers**
```java
// Use StringSerializer for simple strings
StringRedisSerializer stringSerializer = new StringRedisSerializer();
byte[] bytes = stringSerializer.serialize("simple value");

// Use JsonSerializer only for complex objects
JsonRedisSerializer<Order> orderSerializer =
    new JsonRedisSerializer<>(mapper, Order.class);
byte[] orderBytes = orderSerializer.serialize(order);
```

## Testing Your Serializers

### Unit Test Example

```java
@Test
void testCustomSerializerRoundTrip() throws SerializationException {
    // Given
    MyCustomSerializer serializer = new MyCustomSerializer();
    MyObject original = new MyObject("test", 123);

    // When
    byte[] serialized = serializer.serialize(original);
    MyObject deserialized = serializer.deserialize(serialized);

    // Then
    assertEquals(original, deserialized);
}
```

### Integration Test with Redis

```java
@SpringBootTest
@Testcontainers
class CustomSerializationIT {

    @Container
    private static final RedisContainer REDIS =
        new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    @Test
    void testCustomSerializerWithRedis() throws SerializationException {
        // Given
        JsonRedisSerializer<User> serializer =
            new JsonRedisSerializer<>(new ObjectMapper(), User.class);
        User user = new User("user123", "John Doe");

        // When
        byte[] serialized = serializer.serialize(user);
        redisTemplate.opsForValue().set("test:user", serialized);

        byte[] retrieved = redisTemplate.opsForValue().get("test:user");
        User deserialized = serializer.deserialize(retrieved);

        // Then
        assertEquals(user.getId(), deserialized.getId());
        assertEquals(user.getName(), deserialized.getName());
    }
}
```

## Troubleshooting

### Common Issues

**1. SerializationException: Failed to deserialize**
```
Cause: Invalid JSON or incompatible type
Solution: Verify data format and class structure match
```

**2. NullPointerException**
```
Cause: Trying to serialize/deserialize null without checks
Solution: Always handle null values explicitly
```

**3. ClassCastException**
```
Cause: Deserializing to wrong type
Solution: Ensure serializer type matches data type
```

### Debug Tips

**1. Log Serialized Data**
```java
byte[] bytes = serializer.serialize(object);
logger.debug("Serialized data: {}", new String(bytes, StandardCharsets.UTF_8));
```

**2. Verify JSON Format**
```java
byte[] bytes = jsonSerializer.serialize(object);
String json = new String(bytes);
logger.debug("JSON: {}", json);
// Verify it's valid JSON
```

**3. Test Round Trip**
```java
// Always test serialization/deserialization cycle
Object original = ...;
byte[] bytes = serializer.serialize(original);
Object retrieved = serializer.deserialize(bytes);
assertEquals(original, retrieved);
```

## See Also

- [Main README](README.md) - Complete library documentation
- [Testing Guide](TESTING.md) - Testing strategies
- [Jackson Documentation](https://github.com/FasterXML/jackson) - JSON serialization
- [Spring Data Redis](https://spring.io/projects/spring-data-redis) - Redis integration

## Examples

For complete working examples, see:

**Unit Tests:**
- [JsonRedisSerializerTest.java](redis-utils-core/src/test/java/com/github/mehrdadfalahati/redisutils/serialization/JsonRedisSerializerTest.java) - 8 tests for JSON serialization
- [StringRedisSerializerTest.java](redis-utils-core/src/test/java/com/github/mehrdadfalahati/redisutils/serialization/StringRedisSerializerTest.java) - 12 tests for string serialization
- [ByteArrayRedisSerializerTest.java](redis-utils-core/src/test/java/com/github/mehrdadfalahati/redisutils/serialization/ByteArrayRedisSerializerTest.java) - 8 tests for binary data
- [RedisSerializerRegistryTest.java](redis-utils-core/src/test/java/com/github/mehrdadfalahati/redisutils/serialization/RedisSerializerRegistryTest.java) - 10 tests for registry management

**Integration Tests:**
- [SerializationIntegrationIT.java](redis-utils-core/src/test/java/com/github/mehrdadfalahati/redisutils/serialization/SerializationIntegrationIT.java) - 11 tests demonstrating real Redis operations with custom serializers

# Redis Utils

A Spring Boot utility library for Redis operations with a clean, type-safe API built on top of Lettuce.

## Features

- **Clean API**: Fluent, intuitive interface for Redis operations
- **Type-Safe**: Generic support with automatic serialization/deserialization
- **Lettuce-Based**: Built on industry-standard Lettuce client
- **Spring Boot Integration**: Auto-configuration with sensible defaults
- **Error Handling**: Consistent exception hierarchy with retry logic
- **Connection Management**: Automatic lifecycle management and health checks
- **Production Ready**: Configurable timeouts, pooling, and circuit breakers

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.github.mehrdadfalahati</groupId>
    <artifactId>redis-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
@Service
public class UserService {

    @Autowired
    private RedisValueOperations redisOps;

    public void cacheUser(User user) {
        // Set with 1 hour TTL
        redisOps.set(RedisKey.ofHours("user:" + user.getId(), 1), user);
    }

    public User getUser(String userId) {
        return redisOps.get("user:" + userId, User.class);
    }
}
```

## API Reference

### RedisKey - Fluent Key Builder

Create Redis keys with optional TTL:

```java
// Without expiration
RedisKey key = RedisKey.of("user:123");

// With Duration
RedisKey key = RedisKey.of("session:abc", Duration.ofMinutes(30));

// With TimeUnit
RedisKey key = RedisKey.of("cache:data", 5, TimeUnit.MINUTES);

// Fluent helpers
RedisKey.ofSeconds("key", 30);
RedisKey.ofMinutes("key", 5);
RedisKey.ofHours("key", 2);
RedisKey.ofDays("key", 7);
```

### RedisValueOperations - String/Value Operations

#### Set Operations

```java
// Simple set
redisOps.set(RedisKey.of("key"), "value");

// Set if absent (NX)
boolean wasSet = redisOps.setIfAbsent(RedisKey.ofSeconds("lock", 10), "owner-id");

// Set if present (XX)
boolean wasSet = redisOps.setIfPresent(RedisKey.of("key"), "new-value");
```

#### Get Operations

```java
// Get value
String value = redisOps.get("key", String.class);
User user = redisOps.get("user:1", User.class);

// Get and delete atomically
String value = redisOps.getAndDelete("key", String.class);

// Get and set atomically
String oldValue = redisOps.getAndSet(RedisKey.of("key"), "new-value", String.class);
```

#### Batch Operations

```java
// Get multiple values
Map<String, User> users = redisOps.multiGet(User.class, "user:1", "user:2", "user:3");

// Set multiple values
Map<RedisKey, Object> kvMap = Map.of(
    RedisKey.ofMinutes("key1", 5), "value1",
    RedisKey.ofMinutes("key2", 10), "value2"
);
redisOps.multiSet(kvMap);
```

#### Counter Operations

```java
// Increment by 1
long count = redisOps.increment("counter");

// Increment by delta
long newCount = redisOps.incrementBy("counter", 5);

// Decrement
long newCount = redisOps.decrement("counter");
```

### RedisStringOperations - Extended String Operations

```java
@Autowired
private RedisStringOperations stringOps;

// Append to string
long newLength = stringOps.append("key", "more text");

// Get substring
String substring = stringOps.getRange("key", 0, 10);

// Set range
long length = stringOps.setRange("key", 5, "replacement");

// String length
long length = stringOps.strlen("key");

// Atomic multi-set (MSET)
stringOps.atomicMultiSet(keyValueMap);

// Multi-set if absent (MSETNX)
boolean allSet = stringOps.multiSetIfAbsent(keyValueMap);
```

### RedisKeyOperations - Key Management

```java
@Autowired
private RedisKeyOperations keyOps;

// Check existence
boolean exists = keyOps.exists("key");

// Delete keys
long deleted = keyOps.delete("key1", "key2", "key3");

// Set expiration
boolean success = keyOps.expire("key", Duration.ofMinutes(5));

// Get TTL
Duration ttl = keyOps.ttl("key");

// Remove expiration
boolean success = keyOps.persist("key");

// Find keys by pattern (use with caution in production)
Set<String> keys = keyOps.keys("user:*");
```

### RedisClient - Unified Client Interface

```java
@Autowired
private RedisClient redisClient;

// Access operations
RedisKeyOperations keyOps = redisClient.keyOps();
RedisValueOperations valueOps = redisClient.valueOps();

// Health check
boolean isHealthy = redisClient.isConnected();

// Custom command (escape hatch)
String result = redisClient.executeCommand(commands -> {
    return commands.get("custom:key");
});

// Close (handled automatically by Spring)
redisClient.close();
```

## Configuration

### application.yml

```yaml
# Spring Data Redis Configuration
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: secret
      database: 0
      timeout: 5s
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: 3s

# Redis Utils Configuration
redis:
  client:
    timeout: 5s
    pool-enabled: true
    pool:
      min-idle: 2
      max-idle: 8
      max-total: 8
      max-wait: 3s
      test-on-borrow: true
      test-while-idle: false
      time-between-eviction-runs: 30s
    retry:
      enabled: true
      max-attempts: 3
      initial-backoff: 100ms
      max-backoff: 2s
      backoff-multiplier: 2.0
      retry-on-timeout: false
    circuit-breaker-enabled: false
```

### application.properties

```properties
# Spring Data Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=secret

# Redis Utils
redis.client.timeout=5s
redis.client.retry.enabled=true
redis.client.retry.max-attempts=3
```

## Error Handling

### Exception Hierarchy

```
RedisException (base)
├── RedisConnectionException - Connection failures
├── RedisTimeoutException - Operation timeouts
├── RedisSerializationException - Serialization errors
└── RedisOperationException - Command failures
```

### Retry Configuration

Operations automatically retry on transient failures:

```java
redis:
  client:
    retry:
      enabled: true              # Enable retry
      max-attempts: 3            # Retry up to 3 times
      initial-backoff: 100ms     # Start with 100ms delay
      max-backoff: 2s            # Cap delay at 2s
      backoff-multiplier: 2.0    # Exponential backoff
      retry-on-timeout: false    # Don't retry timeouts
```

### Custom Error Handling

```java
try {
    redisOps.set(RedisKey.of("key"), value);
} catch (RedisConnectionException e) {
    // Connection failed - check network/Redis availability
    log.error("Redis unavailable", e);
} catch (RedisTimeoutException e) {
    // Operation timed out
    log.error("Redis timeout after {}ms", e.getTimeoutMillis());
} catch (RedisSerializationException e) {
    // Serialization failed
    log.error("Cannot serialize type {}", e.getTargetType());
} catch (RedisException e) {
    // Generic Redis error
    log.error("Redis operation failed", e);
}
```

## Advanced Usage

### Custom Serialization

```java
@Configuration
public class CustomRedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
```

### Custom Operations

```java
@Service
public class CustomRedisService {

    @Autowired
    private RedisClient redisClient;

    public String customCommand() {
        return redisClient.executeCommand(commands -> {
            // Direct access to Lettuce commands
            return commands.get("special:key");
        });
    }
}
```

### Health Monitoring

```java
@Component
public class RedisHealthMonitor {

    @Autowired
    private RedisClient redisClient;

    @Scheduled(fixedRate = 30000)
    public void checkHealth() {
        if (!redisClient.isConnected()) {
            log.error("Redis is unhealthy!");
            // Send alert
        }
    }
}
```

## Best Practices

### 1. Use Appropriate TTLs

Always set expiration for cache data:

```java
// Good - cache expires
redisOps.set(RedisKey.ofMinutes("cache:product:" + id, 15), product);

// Bad - cache never expires
redisOps.set(RedisKey.of("cache:product:" + id), product);
```

### 2. Handle Null Values

```java
User user = redisOps.get("user:123", User.class);
if (user == null) {
    user = loadFromDatabase(123);
    redisOps.set(RedisKey.ofHours("user:123", 1), user);
}
```

### 3. Use Batch Operations

```java
// Good - single round trip
Map<String, User> users = redisOps.multiGet(User.class, "user:1", "user:2", "user:3");

// Bad - 3 round trips
User u1 = redisOps.get("user:1", User.class);
User u2 = redisOps.get("user:2", User.class);
User u3 = redisOps.get("user:3", User.class);
```

### 4. Avoid Keys Pattern in Production

```java
// Bad - blocks Redis
Set<String> allUsers = keyOps.keys("user:*");

// Good - use SCAN or maintain a set
// Implement using sorted sets or dedicated index keys
```

### 5. Use SetIfAbsent for Locks

```java
boolean lockAcquired = redisOps.setIfAbsent(
    RedisKey.ofSeconds("lock:resource:" + id, 30),
    Thread.currentThread().getName()
);

if (lockAcquired) {
    try {
        // Critical section
    } finally {
        keyOps.delete("lock:resource:" + id);
    }
}
```

## Testing

### With Testcontainers

```java
@SpringBootTest
public class RedisIntegrationTest extends AbstractRedisTestContainer {

    @Autowired
    private RedisValueOperations redisOps;

    @Test
    void testCaching() {
        redisOps.set(RedisKey.of("test"), "value");
        String value = redisOps.get("test", String.class);
        assertEquals("value", value);
    }
}
```

### Abstract Base Class

```java
@SpringBootTest
public abstract class AbstractRedisTestContainer {

    protected static final RedisContainer REDIS_CONTAINER =
        new RedisContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static {
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port",
            () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }
}
```

## Architecture

### Package Structure

```
com.github.mehrdadfalahati.redisutils
├── core/                    # Core abstractions
│   └── RedisKey            # Key with TTL support
├── operations/             # Operation interfaces
│   ├── RedisKeyOperations
│   ├── RedisValueOperations
│   ├── RedisStringOperations
│   ├── DefaultRedisKeyOperations
│   └── DefaultRedisValueOperations
├── client/                 # Client abstraction
│   ├── RedisClient
│   └── RedisConnectionManager
├── lettuce/                # Lettuce implementation
│   ├── LettuceRedisClient
│   └── LettuceStringOperations
├── config/                 # Configuration
│   ├── RedisProperties
│   ├── RedisSerializationConfiguration
│   ├── RedisTemplateConfiguration
│   └── RedisClientAutoConfiguration
├── exception/              # Exception hierarchy
│   ├── RedisException
│   ├── RedisConnectionException
│   ├── RedisTimeoutException
│   ├── RedisSerializationException
│   └── RedisOperationException
└── util/                   # Utilities
    └── RedisCommandExecutor
```

### Design Principles

1. **Abstraction**: Hide Lettuce complexity behind clean interfaces
2. **Immutability**: RedisKey is immutable value object
3. **Type Safety**: Generic support with ObjectMapper conversion
4. **Spring Integration**: Auto-configuration with defaults
5. **Testability**: Easy to test with Testcontainers

## Requirements

- Java 21+
- Spring Boot 3.3+
- Lettuce 6.x (via Spring Data Redis)
- Redis 5.0+

## License

[Your License]

## Contributing

Contributions welcome! Please see CONTRIBUTING.md for details.

## Support

For issues and questions:
- GitHub Issues: https://github.com/mehrdadfalahati/redis-utils/issues
- Documentation: https://github.com/mehrdadfalahati/redis-utils/wiki

# Redis Utils

[![Build](https://github.com/mehrdadfalahati/redis-utils/actions/workflows/build.yml/badge.svg)](https://github.com/mehrdadfalahati/redis-utils/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase-downloads.html)

A Spring Boot utility library for Redis operations with a clean, type-safe API built on top of Lettuce.

## Features

- **Clean API**: Fluent, intuitive interface for Redis operations
- **Complete Redis Support**: All major data structures (Strings, Hashes, Lists, Sets, Sorted Sets)
- **Type-Safe**: Generic support with automatic serialization/deserialization
- **Lettuce-Based**: Built on industry-standard Lettuce client
- **Spring Boot Integration**: Auto-configuration with sensible defaults
- **Error Handling**: Consistent exception hierarchy with retry logic
- **Connection Management**: Automatic lifecycle management and health checks
- **Production Ready**: Configurable timeouts, pooling, and circuit breakers
- **Comprehensive Testing**: 60+ integration tests with Testcontainers

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

### RedisHashOperations - Hash Operations

Redis hashes are maps between string fields and values, perfect for representing objects.

```java
@Autowired
private RedisHashOperations hashOps;

// Store user profile
Map<String, Object> user = Map.of(
    "name", "John Doe",
    "email", "john@example.com",
    "age", 30
);
hashOps.putAll("user:123", user);

// Get single field
String name = hashOps.get("user:123", "name", String.class);
Integer age = hashOps.get("user:123", "age", Integer.class);

// Get multiple fields
Map<String, String> fields = hashOps.multiGet(
    "user:123",
    List.of("name", "email", "age"),
    String.class
);

// Get all entries
Map<String, Object> allFields = hashOps.entries("user:123", Object.class);

// Update single field
hashOps.put("user:123", "age", 31);

// Conditional set (only if field doesn't exist)
boolean wasSet = hashOps.putIfAbsent("user:123", "country", "USA");

// Check if field exists
boolean hasEmail = hashOps.hasKey("user:123", "email");

// Increment numeric field
long views = hashOps.increment("post:1", "views", 1L);
double rating = hashOps.increment("post:1", "rating", 0.5);

// Get all field names
Set<String> fieldNames = hashOps.keys("user:123");

// Get all values
List<String> values = hashOps.values("user:123", String.class);

// Delete fields
long deleted = hashOps.delete("user:123", "tempField1", "tempField2");

// Get hash size
long size = hashOps.size("user:123");
```

### RedisListOperations - List Operations

Redis lists are ordered collections, ideal for queues and stacks.

```java
@Autowired
private RedisListOperations listOps;

// Push to right (append)
listOps.rightPush("tasks", "task1", "task2", "task3");

// Push to left (prepend)
listOps.leftPush("notifications", "urgent-notification");

// Pop from left (queue behavior - FIFO)
String nextTask = listOps.leftPop("tasks", String.class);

// Pop from right (stack behavior - LIFO)
String lastItem = listOps.rightPop("stack", String.class);

// Get range of elements
List<String> firstThree = listOps.range("tasks", 0, 2, String.class);
List<String> all = listOps.range("tasks", 0, -1, String.class);

// Get element at index
String third = listOps.index("tasks", 2, String.class);

// Set element at index
listOps.set("tasks", 2, "updated-task");

// Trim list (keep only specified range)
listOps.trim("tasks", 0, 99); // Keep first 100 elements

// Remove elements
long removed = listOps.remove("tasks", 2, "task-to-remove"); // Remove first 2 occurrences

// Get list size
long size = listOps.size("tasks");

// Push only if list exists
long length = listOps.rightPushIfPresent("existing-list", "new-item");
```

### RedisSetOperations - Set Operations

Redis sets are unordered collections of unique strings.

```java
@Autowired
private RedisSetOperations setOps;

// Add members
setOps.add("tags", "java", "spring", "redis");

// Check membership
boolean hasJava = setOps.isMember("tags", "java");

// Get all members
Set<String> allTags = setOps.members("tags", String.class);

// Remove members
long removed = setOps.remove("tags", "redis", "deprecated-tag");

// Pop random member (removes it)
String randomTag = setOps.pop("tags", String.class);

// Get random member (doesn't remove)
String random = setOps.randomMember("tags", String.class);

// Get multiple random members (may contain duplicates)
List<String> randoms = setOps.randomMembers("tags", 5, String.class);

// Get distinct random members
Set<String> distinctRandoms = setOps.distinctRandomMembers("tags", 3, String.class);

// Set operations
Set<String> diff = setOps.difference(List.of("set1", "set2"), String.class); // set1 - set2
Set<String> intersect = setOps.intersect(List.of("set1", "set2"), String.class); // set1 ∩ set2
Set<String> union = setOps.union(List.of("set1", "set2"), String.class); // set1 ∪ set2

// Get set size
long size = setOps.size("tags");
```

### RedisZSetOperations - Sorted Set Operations

Redis sorted sets are collections ordered by score, perfect for leaderboards and rankings.

```java
@Autowired
private RedisZSetOperations zSetOps;

// Add members with scores
zSetOps.add("leaderboard", "player1", 1000.0);
zSetOps.add("leaderboard", "player2", 1500.0);
zSetOps.add("leaderboard", "player3", 2000.0);

// Increment score
double newScore = zSetOps.incrementScore("leaderboard", "player1", 100.0);

// Get score of member
Double score = zSetOps.score("leaderboard", "player2");

// Get rank (0-indexed, lowest score first)
Long rank = zSetOps.rank("leaderboard", "player1"); // Returns 0 (lowest)

// Get reverse rank (highest score first)
Long reverseRank = zSetOps.reverseRank("leaderboard", "player3"); // Returns 0 (highest)

// Get range by index (ordered by score)
Set<String> bottom3 = zSetOps.range("leaderboard", 0, 2, String.class);
Set<String> top3 = zSetOps.reverseRange("leaderboard", 0, 2, String.class);

// Get range by score
Set<String> midRange = zSetOps.rangeByScore("leaderboard", 1000.0, 1500.0, String.class);
Set<String> topScorers = zSetOps.reverseRangeByScore("leaderboard", 1500.0, 3000.0, String.class);

// Count members in score range
long count = zSetOps.count("leaderboard", 1000.0, 2000.0);

// Remove members
long removed = zSetOps.remove("leaderboard", "player1", "player2");

// Remove by rank range
long removedByRank = zSetOps.removeRange("leaderboard", 0, 9); // Remove bottom 10

// Remove by score range
long removedByScore = zSetOps.removeRangeByScore("leaderboard", 0.0, 100.0);

// Get sorted set size
long size = zSetOps.size("leaderboard");
```

### RedisClient - Unified Client Interface

Access all Redis operations through a single fluent interface:

```java
@Autowired
private RedisClient redisClient;

// Access operations
RedisKeyOperations keyOps = redisClient.keyOps();
RedisValueOperations valueOps = redisClient.valueOps(); // or opsForValue()
RedisHashOperations hashOps = redisClient.opsForHash();
RedisListOperations listOps = redisClient.opsForList();
RedisSetOperations setOps = redisClient.opsForSet();
RedisZSetOperations zSetOps = redisClient.opsForZSet();

// Example: Build a complete user session
String userId = "user:123";

// Store user data in hash
redisClient.opsForHash().putAll(userId, Map.of(
    "name", "John Doe",
    "email", "john@example.com",
    "role", "admin"
));

// Track user's recent activities in a list
redisClient.opsForList().rightPush(userId + ":activities",
    "login", "view-dashboard", "edit-profile");

// Store user's tags in a set
redisClient.opsForSet().add(userId + ":tags",
    "premium", "verified", "active");

// Track user's points in sorted set
redisClient.opsForZSet().add("global-leaderboard", userId, 1500.0);

// Set expiration on keys
redisClient.keyOps().expire(userId, Duration.ofHours(24));

// Health check
boolean isHealthy = redisClient.isConnected();

// Custom command (escape hatch for advanced use cases)
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

This project has comprehensive test coverage with both unit and integration tests.

### Running Tests

```bash
# Run unit tests only
mvn test

# Run integration tests only
mvn verify -DskipTests

# Run all tests (unit + integration)
mvn verify
```

### Test Coverage

- **Unit Tests** (`*Test.java`): Test individual components with mocked dependencies
  - RedisKey value object
  - RedisCommandExecutor retry logic
  - LettuceRedisClient connection management
  - Configuration classes
  - Exception handling

- **Integration Tests** (`*IT.java`): End-to-end tests with real Redis via Testcontainers
  - RedisStringOperations - all string operations
  - RedisKeyOperations - key management
  - RedisHashOperations - 14 tests covering hash operations
  - RedisListOperations - 15 tests covering list operations
  - RedisSetOperations - 15 tests covering set operations
  - RedisZSetOperations - 16 tests covering sorted set operations
  - RedisClientAutoConfiguration - bean wiring
  - Performance benchmarks

**Total: 155+ tests** (88 unit tests + 67 integration tests) with 100% pass rate

### Writing Integration Tests

All integration tests use Testcontainers to automatically start a Redis instance:

```java
@SpringBootTest
@Testcontainers
public class MyRedisIT {

    @Container
    private static final RedisContainer REDIS_CONTAINER =
        new RedisContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port",
            () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

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

**Requirements:**
- Docker must be running for integration tests
- Testcontainers will automatically download and start Redis

**Alternative:** To use a local Redis instance instead:
1. Remove `@Testcontainers` and `@Container` annotations
2. Configure `application-test.yml` with your Redis connection
3. Start Redis locally: `redis-server`

For more details, see [TESTING.md](TESTING.md).

## Architecture

### Package Structure

```
com.github.mehrdadfalahati.redisutils
├── core/                    # Core abstractions
│   └── RedisKey            # Key with TTL support
├── operations/             # Operation interfaces & implementations
│   ├── RedisKeyOperations
│   ├── RedisValueOperations
│   ├── RedisStringOperations
│   ├── RedisHashOperations
│   ├── RedisListOperations
│   ├── RedisSetOperations
│   ├── RedisZSetOperations
│   ├── impl/
│       ├── DefaultRedisKeyOperations
│       ├── DefaultRedisValueOperations
│       ├── DefaultRedisHashOperations
│       ├── DefaultRedisListOperations
│       ├── DefaultRedisSetOperations
│       └── DefaultRedisZSetOperations
├── client/                 # Client abstraction
│   ├── RedisClient
│   └── RedisConnectionManager
├── lettuce/                # Lettuce implementation
│   ├── LettuceRedisClient
│   └── LettuceStringOperations
├── serialization/          # Custom serialization support
│   ├── RedisValueSerializer
│   ├── SerializationException
│   ├── StringRedisSerializer
│   ├── ByteArrayRedisSerializer
│   ├── JsonRedisSerializer
│   └── RedisSerializerRegistry
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

- Java 17+ (tested on Java 17 and 21)
- Spring Boot 3.3+
- Lettuce 6.x (via Spring Data Redis)
- Redis 5.0+

## Building from Source

```bash
# Clone the repository
git clone https://github.com/mehrdadfalahati/redis-utils.git
cd redis-utils

# Build and run tests (requires Docker for integration tests)
mvn clean verify

# Build without tests
mvn clean package -DskipTests

# Install to local Maven repository
mvn clean install
```

## License

[Your License]

## Contributing

Contributions welcome! Please see CONTRIBUTING.md for details.

## Support

For issues and questions:
- GitHub Issues: https://github.com/mehrdadfalahati/redis-utils/issues
- Documentation: https://github.com/mehrdadfalahati/redis-utils/wiki

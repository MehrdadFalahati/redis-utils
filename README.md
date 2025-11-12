# Redis Utils

[![Build](https://github.com/mehrdadfalahati/redis-utils/actions/workflows/build.yml/badge.svg)](https://github.com/mehrdadfalahati/redis-utils/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase-downloads.html)

A production-ready Spring Boot starter for Redis operations with a clean, type-safe API built on top of Lettuce.

## Features

- **Clean API**: Fluent, intuitive interface for Redis operations
- **Complete Redis Support**: All major data structures (Strings, Hashes, Lists, Sets, Sorted Sets)
- **Type-Safe**: Generic support with automatic serialization/deserialization
- **Lettuce-Based**: Built on industry-standard Lettuce client
- **Spring Boot Starter**: Auto-configuration with sensible defaults
- **Error Handling**: Consistent exception hierarchy with retry logic
- **Connection Management**: Automatic lifecycle management and health checks
- **Production Ready**: Configurable timeouts, pooling, and circuit breakers
- **Comprehensive Testing**: 269 tests with Testcontainers (117 unit + 152 integration)

## Project Modules

This project follows Spring Boot starter best practices with a multi-module structure:

- **[redis-utils-core](redis-utils-core/)** - Core Redis operations without Spring Boot dependencies
- **[redis-utils-spring-boot-starter](redis-utils-spring-boot-starter/)** - Spring Boot auto-configuration and starter
- **[redis-utils-examples](redis-utils-examples/)** - Example application with REST API demonstrations

## Quick Start

### Maven Dependency

Add the Spring Boot starter to your project:

```xml
<dependency>
    <groupId>com.github.mehrdadfalahati</groupId>
    <artifactId>redis-utils-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> **Note:** The starter automatically includes `redis-utils-core` and configures Spring Data Redis.

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

// Decrement by delta
long newCount = redisOps.decrementBy("counter", 5);
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

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: secret    # Optional

redis:
  client:
    enabled: true         # Enable/disable auto-configuration (default: true)
    timeout: 5s           # Command timeout (default: 5s)
    pool-enabled: true    # Enable connection pooling (default: true)

    # Connection pool settings
    pool:
      max-total: 8        # Maximum connections (default: 8)
      max-idle: 8         # Maximum idle connections (default: 8)
      min-idle: 2         # Minimum idle connections (default: 2)
      max-wait: 10s       # Max wait for connection (default: 10s)

    # Retry configuration
    retry:
      enabled: true       # Enable retry logic (default: true)
      max-attempts: 3     # Maximum retry attempts (default: 3)
      initial-backoff: 100ms # Initial delay before retry (default: 100ms)
      max-backoff: 2s       # Maximum delay between retries (default: 2s)
      backoff-multiplier: 2.0     # Exponential backoff multiplier (default: 2.0)
```

**Disabling Auto-Configuration:**

To disable Redis Utils auto-configuration:

```yaml
redis:
  client:
    enabled: false
```

Or exclude it programmatically:

```java
@SpringBootApplication(exclude = {RedisClientAutoConfiguration.class})
public class Application {
    // ...
}
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

**Total: 269 tests** (117 unit tests + 152 integration tests) with 100% pass rate

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

## Custom Serialization

`redis-utils` provides a flexible custom serialization framework for fine-grained control over how data is stored in Redis:

- **JsonRedisSerializer** - JSON serialization with Jackson
- **StringRedisSerializer** - UTF-8 string serialization
- **ByteArrayRedisSerializer** - Binary data pass-through
- **RedisSerializerRegistry** - Manage multiple serializers

```java
// Example: Custom JSON serializer
ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
JsonRedisSerializer<User> serializer = new JsonRedisSerializer<>(mapper, User.class);

User user = new User("user123", "John Doe");
byte[] bytes = serializer.serialize(user);
User retrieved = serializer.deserialize(bytes);
```

**When to use custom serializers:**
- Need multiple serialization strategies
- Working with binary data (images, Protocol Buffers)
- Performance-critical applications
- Legacy data format compatibility

For comprehensive examples and usage patterns, see [SERIALIZATION_GUIDE.md](SERIALIZATION_GUIDE.md).

## Architecture

### Multi-Module Structure

```
redis-utils/
├── redis-utils-core/                    # Core library (no Spring Boot dependencies)
│   ├── core/                            # Core abstractions
│   │   └── RedisKey                     # Immutable key with TTL support
│   ├── operations/                      # Operation interfaces
│   │   ├── RedisKeyOperations
│   │   ├── RedisValueOperations
│   │   ├── RedisStringOperations
│   │   ├── RedisHashOperations
│   │   ├── RedisListOperations
│   │   ├── RedisSetOperations
│   │   ├── RedisZSetOperations
│   │   └── impl/                        # Default implementations
│   │       ├── DefaultRedisKeyOperations
│   │       ├── DefaultRedisValueOperations
│   │       ├── DefaultRedisHashOperations
│   │       ├── DefaultRedisListOperations
│   │       ├── DefaultRedisSetOperations
│   │       └── DefaultRedisZSetOperations
│   ├── client/                          # Client abstraction
│   │   ├── RedisClient
│   │   └── RedisConnectionManager
│   ├── lettuce/                         # Lettuce-specific implementations
│   │   ├── LettuceRedisClient
│   │   └── LettuceStringOperations
│   ├── serialization/                   # Serialization support
│   ├── config/                          # Core configuration (RedisProperties)
│   ├── exception/                       # Exception hierarchy
│   │   ├── RedisException
│   │   ├── RedisConnectionException
│   │   ├── RedisTimeoutException
│   │   ├── RedisSerializationException
│   │   └── RedisOperationException
│   └── util/                            # Utilities
│       └── RedisCommandExecutor         # Retry and error handling
│
├── redis-utils-spring-boot-starter/     # Spring Boot auto-configuration
│   ├── config/                          # Auto-configuration classes
│   │   ├── RedisClientAutoConfiguration # Main auto-config
│   │   ├── RedisClientProperties        # @ConfigurationProperties
│   │   ├── RedisSerializationConfiguration
│   │   └── RedisTemplateConfiguration   # RedisTemplate bean setup
│   └── META-INF/spring/
│       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│
└── redis-utils-examples/                # Example Spring Boot application
    ├── controller/                      # REST API endpoints
    ├── service/                         # Business logic layer
    ├── model/                           # Domain models (DTOs)
    └── RedisUtilsExampleApplication     # Main Spring Boot class
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

This is a multi-module Maven project. Build from the root directory:

```bash
# Clone the repository
git clone https://github.com/mehrdadfalahati/redis-utils.git
cd redis-utils

# Build all modules and run tests (requires Docker for integration tests)
mvn clean verify

# Build without tests
mvn clean package -DskipTests

# Install to local Maven repository
mvn clean install

# Build a specific module
cd redis-utils-core
mvn clean install
```

### Running the Example Application

```bash
# Start Redis using Docker
docker run -d -p 6379:6379 redis:latest

# Run the example application
cd redis-utils-examples
mvn spring-boot:run

# The application will be available at http://localhost:8080
# See redis-utils-examples/README.md for API documentation
```

### Module Dependencies

```
redis-utils-parent (pom)
├── redis-utils-core
├── redis-utils-spring-boot-starter (depends on core)
└── redis-utils-examples (depends on starter)
```

To use only the core library without Spring Boot:

```xml
<dependency>
    <groupId>com.github.mehrdadfalahati</groupId>
    <artifactId>redis-utils-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Roadmap

### ✅ Implemented Features

#### Core Operations
- ✅ **String/Value Operations** - Complete implementation with atomic operations
  - Basic get/set with TTL support
  - Conditional operations (SETNX, SETXX)
  - Atomic counters (INCR, DECR, INCRBY, DECRBY)
  - Batch operations (MGET, MSET, MSETNX)
  - String manipulation (APPEND, GETRANGE, SETRANGE, STRLEN)
  - Get-and-set atomic operations

- ✅ **Hash Operations** - Full hash support for object storage
  - Single field operations (HGET, HSET, HSETNX)
  - Multi-field operations (HMGET, HMSET)
  - All entries retrieval (HGETALL)
  - Field existence checks (HEXISTS)
  - Atomic increments (HINCRBY, HINCRBYFLOAT)
  - Keys/values listing (HKEYS, HVALS)
  - Field deletion (HDEL)
  - Hash size (HLEN)

- ✅ **List Operations** - Queue and stack implementations
  - Push operations (LPUSH, RPUSH, LPUSHX, RPUSHX)
  - Pop operations (LPOP, RPOP)
  - Range operations (LRANGE, LINDEX)
  - List modification (LSET, LTRIM)
  - Element removal (LREM)
  - List size (LLEN)

- ✅ **Set Operations** - Unique collections with set algebra
  - Add/remove members (SADD, SREM)
  - Membership testing (SISMEMBER)
  - Member retrieval (SMEMBERS)
  - Random operations (SPOP, SRANDMEMBER)
  - Set operations (SDIFF, SINTER, SUNION)
  - Cardinality (SCARD)

- ✅ **Sorted Set Operations** - Leaderboards and rankings
  - Add with scores (ZADD)
  - Score operations (ZINCRBY, ZSCORE)
  - Rank operations (ZRANK, ZREVRANK)
  - Range queries (ZRANGE, ZREVRANGE, ZRANGEBYSCORE)
  - Count operations (ZCOUNT)
  - Remove operations (ZREM, ZREMRANGEBYRANK, ZREMRANGEBYSCORE)
  - Cardinality (ZCARD)

- ✅ **Key Operations** - Key lifecycle management
  - Existence checks (EXISTS)
  - Deletion (DEL)
  - Expiration (EXPIRE, EXPIREAT, TTL)
  - Persistence (PERSIST)
  - Pattern matching (KEYS) - with production warnings

#### Infrastructure
- ✅ **Multi-Module Architecture** - Clean separation of concerns
  - Core library without Spring Boot dependencies
  - Spring Boot starter with auto-configuration
  - Example application with REST API

- ✅ **Type Safety & Serialization**
  - Generic support with automatic type conversion
  - Jackson-based JSON serialization
  - Custom serialization support
  - Java 8+ date/time support

- ✅ **Error Handling & Resilience**
  - Comprehensive exception hierarchy
  - Automatic retry with exponential backoff
  - Configurable timeout handling
  - Circuit breaker support (optional)

- ✅ **Connection Management**
  - Lettuce-based connection pooling
  - Health checks and monitoring
  - Graceful shutdown
  - Connection factory abstraction

- ✅ **Testing**
  - 187 comprehensive tests (79 unit + 108 integration)
  - Unit tests with mocked dependencies
  - Integration tests with Testcontainers
  - Performance benchmarks

- ✅ **Configuration**
  - Spring Boot auto-configuration
  - Externalized configuration via properties/YAML
  - Conditional bean creation
  - Profile-based configuration

### 🚧 Planned Features (Future Releases)

#### Advanced Redis Features
- 🔲 **Transactions** (v1.1.0)
  - MULTI/EXEC support
  - WATCH for optimistic locking
  - Transaction rollback

- 🔲 **Pub/Sub Messaging** (v1.1.0)
  - Channel subscription/publication
  - Pattern-based subscriptions
  - Message listeners with Spring integration

- 🔲 **Pipelining** (v1.2.0)
  - Batch command execution
  - Reduced network round trips
  - Fluent pipeline API

- 🔲 **Lua Scripting** (v1.2.0)
  - Script loading and execution
  - Script caching (EVALSHA)
  - Predefined script library

- 🔲 **Geo-Spatial Operations** (v1.3.0)
  - GEOADD, GEORADIUS, GEODIST
  - Location-based queries
  - Distance calculations

- 🔲 **HyperLogLog** (v1.3.0)
  - Cardinality estimation
  - PFADD, PFCOUNT, PFMERGE

- 🔲 **Streams** (v1.4.0)
  - Event streaming
  - Consumer groups
  - Message acknowledgment

- 🔲 **Bitmap Operations** (v1.4.0)
  - SETBIT, GETBIT, BITCOUNT
  - Bitwise operations

#### Enhanced Features
- 🔲 **Distributed Locks** (v1.1.0)
  - Redlock algorithm implementation
  - Lock expiration and renewal
  - Deadlock prevention

- 🔲 **Rate Limiting** (v1.2.0)
  - Token bucket algorithm
  - Sliding window rate limiting
  - Distributed rate limiters

- 🔲 **Cache Patterns** (v1.2.0)
  - Cache-aside
  - Write-through/Write-behind
  - Refresh-ahead
  - Spring Cache abstraction integration

- 🔲 **Monitoring & Metrics** (v1.3.0)
  - Micrometer integration
  - Command execution metrics
  - Connection pool metrics
  - Spring Boot Actuator endpoints

- 🔲 **Redis Cluster Support** (v1.5.0)
  - Cluster topology discovery
  - Hash slot routing
  - Cluster failover handling

- 🔲 **Redis Sentinel Support** (v1.5.0)
  - Master/slave discovery
  - Automatic failover
  - Sentinel configuration

#### Developer Experience
- 🔲 **Spring Integration** (v1.2.0)
  - Spring Cache abstraction
  - Spring Session integration
  - WebFlux reactive support

- 🔲 **Kotlin Extensions** (v1.3.0)
  - Kotlin DSL
  - Coroutine support
  - Extension functions

- 🔲 **Documentation** (Ongoing)
  - Migration guides
  - Performance tuning guide
  - Best practices documentation
  - Architecture decision records

### Version Timeline

- **v1.0.0** (Current) - Core operations, Spring Boot starter, comprehensive testing
- **v1.1.0** (Q2 2025) - Transactions, Pub/Sub, Distributed locks
- **v1.2.0** (Q3 2025) - Pipelining, Lua scripts, Rate limiting, Cache patterns
- **v1.3.0** (Q4 2025) - Geo-spatial, HyperLogLog, Monitoring, Kotlin support
- **v1.4.0** (Q1 2026) - Streams, Bitmaps
- **v1.5.0** (Q2 2026) - Cluster and Sentinel support

For feature requests or to influence the roadmap, please open an issue on [GitHub](https://github.com/mehrdadfalahati/redis-utils/issues).

## Versioning & Release Strategy

This project follows [Semantic Versioning 2.0.0](https://semver.org/):

### Version Format: MAJOR.MINOR.PATCH

- **MAJOR** version for incompatible API changes
- **MINOR** version for backward-compatible functionality additions
- **PATCH** version for backward-compatible bug fixes

### What Constitutes a Breaking Change?

Breaking changes (requiring MAJOR version bump) include:

1. **API Changes**
   - Removing or renaming public methods/interfaces
   - Changing method signatures (parameters, return types)
   - Removing public classes or interfaces
   - Changing exception types thrown by methods

2. **Behavioral Changes**
   - Changing default configuration values that affect functionality
   - Modifying serialization format (incompatible JSON structure)
   - Changing retry or timeout behavior significantly

3. **Dependency Changes**
   - Upgrading to incompatible Spring Boot versions (e.g., 3.x to 4.x)
   - Removing support for Java versions (e.g., dropping Java 17)

4. **Configuration Changes**
   - Removing or renaming configuration properties
   - Changing property default values that break existing deployments

### What Does NOT Constitute a Breaking Change?

These changes are acceptable in MINOR or PATCH releases:

1. **Additions**
   - Adding new methods to interfaces (with default implementations)
   - Adding new configuration properties
   - Adding new exception types (as long as they extend existing ones)
   - Adding new optional parameters with defaults

2. **Internal Changes**
   - Refactoring internal implementation
   - Performance improvements
   - Bug fixes that restore intended behavior
   - Dependency updates (minor/patch versions)

3. **Deprecations**
   - Marking methods/classes as @Deprecated (with migration path)
   - Providing alternative APIs

### Release Criteria

#### Pre-1.0.0 Releases
- Development releases (0.x.x)
- API may change without notice
- Not recommended for production use

#### 1.0.0 Release Criteria
- ✅ All core Redis operations implemented
- ✅ Comprehensive test coverage (>80%)
- ✅ Complete documentation
- ✅ Spring Boot auto-configuration
- ✅ Production-ready error handling
- ✅ Stable API (no planned breaking changes)
- ✅ Performance benchmarks documented
- ✅ Security review completed
- ✅ Migration guide from 0.x (if applicable)

**Status**: **Ready for 1.0.0 release** - All criteria met ✅

#### Post-1.0.0 Releases

**PATCH releases (1.0.x)**
- Bug fixes only
- Security patches
- Documentation updates
- No new features
- Released as needed

**MINOR releases (1.x.0)**
- New backward-compatible features
- Deprecations with migration path
- Dependency updates (minor versions)
- Released quarterly or as needed

**MAJOR releases (x.0.0)**
- Breaking API changes
- Major architectural changes
- Dependency major version updates
- Released annually or as needed with 6-month deprecation notice

### Release Process

See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) for detailed release procedures.

### Changelog & Migration

- All changes documented in [CHANGELOG.md](CHANGELOG.md)
- Migration guides provided for breaking changes
- Deprecation warnings given at least one MINOR version before removal
- Security advisories published for vulnerabilities

### Supported Versions

| Version | Status | Support Period | Java | Spring Boot |
|---------|--------|----------------|------|-------------|
| 1.0.x   | Active | Current + 12 months | 17, 21 | 3.3+ |
| 0.x.x   | EOL    | Unsupported | - | - |

### Upgrade Policy

- **Java**: Minimum supported Java version may increase in MAJOR releases
- **Spring Boot**: Compatible with current and previous major version
- **Redis**: Supports Redis 5.0+ (may increase in MINOR releases)
- **Lettuce**: Follows Spring Data Redis compatibility

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and conventions
- Write tests for new features
- Update documentation for API changes
- Keep commits atomic and well-described
- Ensure all tests pass before submitting PR

## Support

For issues and questions:
- **GitHub Issues**: [Report bugs or request features](https://github.com/mehrdadfalahati/redis-utils/issues)
- **Documentation**: [Wiki and guides](https://github.com/mehrdadfalahati/redis-utils/wiki)
- **Discussions**: [Community Q&A](https://github.com/mehrdadfalahati/redis-utils/discussions)

## Acknowledgments

- Built with [Lettuce](https://lettuce.io/) - Advanced Redis client
- Powered by [Spring Boot](https://spring.io/projects/spring-boot)
- Testing with [Testcontainers](https://www.testcontainers.org/)

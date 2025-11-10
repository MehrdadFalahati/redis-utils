# Redis Utils - Implementation Strategy Guide

## Overview

This document provides a comprehensive implementation strategy for the Redis Utils library, detailing the architecture, design decisions, and Lettuce integration.

---

## 1. Package Layout

### Core Abstractions (`core/`)

**Purpose**: Domain model and value objects

- `RedisKey.java`: Immutable value object representing Redis keys with optional TTL
  - Factory methods for different time units
  - Type-safe with validation
  - Fluent API design

### Operations Layer (`operations/`)

**Purpose**: Interface definitions and default implementations

**Interfaces**:
- `RedisKeyOperations`: Key management (exists, delete, expire, TTL, persist)
- `RedisValueOperations`: Basic value operations (get, set, increment)
- `RedisStringOperations`: Extended string operations (append, range, etc.)

**Implementations**:
- `DefaultRedisKeyOperations`: Spring RedisTemplate-based implementation
- `DefaultRedisValueOperations`: Spring RedisTemplate-based implementation

**Design Choice**: Operations are segregated by interface to follow Interface Segregation Principle

### Client Layer (`client/`)

**Purpose**: Unified client facade and connection management

- `RedisClient`: Main interface hiding Lettuce complexity
  - Provides access to all operation types
  - Connection health checking
  - Escape hatch for custom commands

- `RedisConnectionManager`: Lifecycle management
  - Connection pooling
  - Graceful shutdown
  - Health monitoring
  - Implements Spring's `DisposableBean`

### Lettuce Integration (`lettuce/`)

**Purpose**: Lettuce-specific implementations

- `LettuceRedisClient`: Concrete implementation of `RedisClient`
- `LettuceStringOperations`: Extended string operations using Lettuce

**Why Lettuce?**
1. Thread-safe connection sharing
2. Reactive support (future expansion)
3. Advanced features (cluster, sentinel, pub/sub)
4. Better Spring Boot integration
5. Active development and maintenance

### Configuration (`config/`)

**Purpose**: Auto-configuration and property binding

- `RedisProperties`: Configurable properties with defaults
  - Timeout configuration
  - Pool settings
  - Retry configuration

- `RedisSerializationConfiguration`: JSON serialization setup
  - Java 8+ time support
  - Custom ObjectMapper configuration

- `RedisTemplateConfiguration`: RedisTemplate bean creation

- `RedisClientAutoConfiguration`: Spring Boot auto-configuration
  - Conditional bean creation
  - Integration with Spring Data Redis

### Exception Handling (`exception/`)

**Purpose**: Consistent error handling hierarchy

```
RedisException (RuntimeException)
├── RedisConnectionException    - Network/connection failures
├── RedisTimeoutException       - Operation timeouts (includes timeout value)
├── RedisSerializationException - JSON serialization errors (includes type info)
└── RedisOperationException     - Redis command errors (includes operation name)
```

**Design Decision**: All exceptions are **unchecked** (extend RuntimeException)
- Rationale: Redis operations are typically infrastructure concerns
- Clients can choose to handle or let propagate
- Consistent with Spring's DataAccessException hierarchy

### Utilities (`util/`)

**Purpose**: Cross-cutting concerns

- `RedisCommandExecutor`: Retry logic and timeout handling
  - Exponential backoff
  - Configurable retry attempts
  - Selective retry (connection errors only, not timeouts)

---

## 2. Connection Lifecycle Management

### Connection Management Strategy

```java
RedisConnectionManager
    ├── Uses Spring's RedisConnectionFactory
    ├── Creates Lettuce StatefulRedisConnection
    ├── Manages ClientResources (I/O threads)
    └── Implements graceful shutdown
```

### Lifecycle Phases

#### 1. Initialization
```java
@Bean
public RedisConnectionManager redisConnectionManager(
    RedisConnectionFactory connectionFactory,
    RedisProperties properties) {
    return new RedisConnectionManager(connectionFactory, properties);
}
```

#### 2. Connection Creation
- Lazy initialization on first use
- Thread-safe singleton pattern
- Extracts configuration from Spring's LettuceConnectionFactory
- Configures timeout, authentication, database selection

#### 3. Health Monitoring
```java
public boolean isHealthy() {
    return connection.sync().ping().equals("PONG");
}
```

#### 4. Graceful Shutdown
```java
@Override
public void destroy() {
    1. Close active connections
    2. Shutdown Redis client
    3. Release client resources (thread pools)
}
```

**Integration with Spring**: Automatic via `DisposableBean` interface

---

## 3. Error Handling & Timeout Strategy

### Exception Wrapping Strategy

**Lettuce Exception → Custom Exception Mapping**:

```java
try {
    // Execute Redis command
} catch (io.lettuce.core.RedisConnectionException e) {
    throw new RedisConnectionException("...", e);
} catch (io.lettuce.core.RedisCommandTimeoutException e) {
    throw new RedisTimeoutException("...", e, timeoutMillis);
} catch (Exception e) {
    throw new RedisException("...", e);
}
```

**Benefits**:
1. Hides Lettuce implementation details
2. Consistent exception handling across client implementations
3. Additional context (operation name, timeout value, target type)

### Retry Configuration

```yaml
redis:
  client:
    retry:
      enabled: true              # Enable/disable retry
      max-attempts: 3            # Total attempts (initial + retries)
      initial-backoff: 100ms     # First retry delay
      max-backoff: 2s            # Maximum retry delay
      backoff-multiplier: 2.0    # Exponential growth factor
      retry-on-timeout: false    # Don't retry timeouts by default
```

### Retry Logic Flow

```
Attempt 1 → Fail (Connection Error) → Wait 100ms
Attempt 2 → Fail (Connection Error) → Wait 200ms
Attempt 3 → Fail (Connection Error) → Throw RedisException

Note: Serialization and operation errors are NOT retried
```

### Timeout Configuration

**Multiple timeout layers**:

1. **Command Timeout** (configured in RedisProperties)
   - Default: 5 seconds
   - Applies to individual Redis commands

2. **Connection Timeout** (from Spring Data Redis)
   - Controls initial connection establishment

3. **Pool Wait Timeout** (for pooled connections)
   - Default: 3 seconds
   - Max time to wait for available connection

### Configuration Object Design

```java
@ConfigurationProperties(prefix = "redis.client")
public class RedisProperties {
    private Duration timeout;
    private boolean poolEnabled;
    private PoolConfig pool;
    private RetryConfig retry;
    private boolean circuitBreakerEnabled;
}
```

**Benefits**:
- Type-safe with Duration objects
- Auto-completion in IDEs
- Validation with Bean Validation
- Default values in code

---

## 4. Best Practices for Production

### Connection Pooling

**Configuration**:
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 8      # Max connections
          max-idle: 8        # Max idle connections
          min-idle: 2        # Min idle connections
          max-wait: 3s       # Max wait for connection
```

**Recommendations**:
- `max-active`: Based on expected concurrent operations
- `min-idle`: Keep connections warm for quick access
- `max-wait`: Should be < command timeout
- Monitor: Connection usage, wait time, pool exhaustion

### Timeout Tuning

**Default: 5 seconds** is reasonable for most use cases

**Adjust based on**:
- Network latency to Redis
- Command complexity (KEYS is slow, GET is fast)
- Acceptable response time for your application

**Monitoring**:
```java
@Autowired
private MeterRegistry meterRegistry;

public <T> T executeWithMetrics(String operation, Supplier<T> command) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
        return command.get();
    } finally {
        sample.stop(Timer.builder("redis.command")
            .tag("operation", operation)
            .register(meterRegistry));
    }
}
```

### Key Naming Conventions

**Recommended pattern**: `{namespace}:{entity}:{id}:{field}`

Examples:
- `user:profile:123` - User profile
- `cache:product:456` - Product cache
- `session:abc123` - Session data
- `lock:order:789` - Distributed lock

**Benefits**:
- Easy to identify data ownership
- Supports pattern-based operations (with caution)
- Clear in Redis monitoring tools

### TTL Strategy

**Always set TTL for cache data**:

```java
// Session - short TTL
RedisKey.ofMinutes("session:" + id, 30)

// User cache - medium TTL
RedisKey.ofHours("user:" + id, 1)

// Product catalog - long TTL
RedisKey.ofDays("product:" + id, 7)

// Persistent data - no TTL (use with caution)
RedisKey.of("config:app-settings")
```

**Benefits**:
- Automatic memory management
- Stale data eviction
- Cost optimization

### Serialization Considerations

**JSON (GenericJackson2JsonRedisSerializer)**:
- ✓ Human-readable in Redis
- ✓ Schema evolution support
- ✓ Cross-language compatibility
- ✗ Larger size than binary formats
- ✗ Slower than binary serialization

**Alternatives**:
- `JdkSerializationRedisSerializer`: Compact but Java-only
- `ProtobufRedisSerializer`: Efficient, cross-language
- Custom serializers for specific needs

### Error Handling Patterns

#### Pattern 1: Fail Fast
```java
try {
    return redisOps.get(key, User.class);
} catch (RedisException e) {
    log.error("Redis unavailable", e);
    throw new ServiceUnavailableException("Cache is down");
}
```

#### Pattern 2: Fallback
```java
try {
    return redisOps.get(key, User.class);
} catch (RedisException e) {
    log.warn("Redis cache miss, loading from DB", e);
    return loadFromDatabase(key);
}
```

#### Pattern 3: Circuit Breaker
```java
@CircuitBreaker(name = "redis", fallbackMethod = "fallbackCache")
public User getUser(String id) {
    return redisOps.get("user:" + id, User.class);
}

public User fallbackCache(String id, Exception e) {
    return loadFromDatabase(id);
}
```

### Distributed Locking

**Simple lock pattern**:
```java
public boolean acquireLock(String resource, Duration ttl) {
    String lockKey = "lock:" + resource;
    String owner = UUID.randomUUID().toString();

    return redisOps.setIfAbsent(
        RedisKey.of(lockKey, ttl),
        owner
    );
}

public void releaseLock(String resource) {
    keyOps.delete("lock:" + resource);
}
```

**Production considerations**:
- Use Redisson for advanced distributed locking
- Implement lock ownership verification
- Handle lock expiration edge cases
- Consider using Redis streams or pub/sub for coordination

### Monitoring & Observability

**Key metrics to track**:

1. **Operation Latency**
   - GET, SET, MGET command times
   - P50, P95, P99 percentiles

2. **Error Rates**
   - Connection failures
   - Timeouts
   - Serialization errors

3. **Connection Pool**
   - Active connections
   - Idle connections
   - Wait time

4. **Redis Server**
   - Memory usage
   - Eviction rate
   - Key count

**Implementation**:
```java
@Component
public class RedisMetrics {

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private RedisClient redisClient;

    @Scheduled(fixedRate = 10000)
    public void recordHealthMetric() {
        registry.gauge("redis.health",
            redisClient.isConnected() ? 1 : 0);
    }
}
```

---

## 5. Sample Implementation: RedisStringOperations

### Interface Definition

```java
public interface RedisStringOperations extends RedisValueOperations {
    long append(String key, String value);
    String getRange(String key, long start, long end);
    long setRange(String key, long offset, String value);
    long strlen(String key);
    void atomicMultiSet(Map<RedisKey, Object> keyValues);
    boolean multiSetIfAbsent(Map<RedisKey, Object> keyValues);
}
```

### Implementation

```java
@Component
public class LettuceStringOperations
    extends DefaultRedisValueOperations
    implements RedisStringOperations {

    public LettuceStringOperations(
        RedisTemplate<String, Object> redisTemplate,
        ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    public long append(String key, String value) {
        Long result = redisTemplate.opsForValue().append(key, value);
        return result != null ? result : 0L;
    }

    @Override
    public void atomicMultiSet(Map<RedisKey, Object> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return;
        }

        // Convert to string map for MSET
        Map<String, Object> stringMap = new HashMap<>();
        for (Map.Entry<RedisKey, Object> entry : keyValues.entrySet()) {
            stringMap.put(entry.getKey().key(), entry.getValue());
        }

        // MSET is atomic
        redisTemplate.opsForValue().multiSet(stringMap);

        // Set expirations (non-atomic)
        for (RedisKey key : keyValues.keySet()) {
            if (key.hasExpiration()) {
                redisTemplate.expire(key.key(), key.ttl());
            }
        }
    }

    // ... other methods
}
```

### Key Design Decisions

1. **Inheritance**: Extends `DefaultRedisValueOperations` to reuse basic operations
2. **Null Safety**: Returns 0L instead of null for numeric operations
3. **Atomicity Note**: MSET is atomic, but adding TTL is not (documented limitation)
4. **Validation**: Empty map early return for performance

---

## 6. Integration Points

### Spring Boot Auto-Configuration

```java
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({io.lettuce.core.RedisClient.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisClientAutoConfiguration {
    // Bean definitions
}
```

**Order matters**: Runs after Spring Data Redis auto-configuration

### Custom Configuration Override

Users can override any bean:

```java
@Configuration
public class CustomRedisConfig {

    @Bean
    public RedisClient customRedisClient(...) {
        // Custom implementation
    }

    @Bean
    public RedisSerializer<Object> customSerializer() {
        // Custom serialization
    }
}
```

### Testing Integration

```java
@SpringBootTest
@Testcontainers
public abstract class AbstractRedisTestContainer {

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
}
```

---

## 7. Migration from Jedis

If migrating from Jedis, key differences:

| Aspect | Jedis | Lettuce |
|--------|-------|---------|
| Threading | Not thread-safe | Thread-safe |
| Connection | One per thread | Shared connection |
| Performance | Good | Better (multiplexing) |
| Reactive | No | Yes |
| Cluster | Supported | Better support |

**Migration steps**:
1. Update Maven dependency (remove Jedis, add Lettuce)
2. Update `application.yml` (lettuce config instead of jedis)
3. No code changes needed (if using Spring Data Redis abstractions)
4. Test thoroughly, especially connection pooling behavior

---

## 8. Future Enhancements

### Planned Features

1. **Reactive Support**
   ```java
   public interface ReactiveRedisValueOperations {
       Mono<Void> set(RedisKey key, Object value);
       Mono<T> get(String key, Class<T> type);
   }
   ```

2. **Additional Data Structures**
   - Hash operations
   - List operations
   - Set operations
   - Sorted set operations
   - Stream operations

3. **Advanced Features**
   - Redis Cluster support
   - Redis Sentinel support
   - Pub/Sub operations
   - Lua script execution
   - Pipeline operations

4. **Observability**
   - Built-in metrics
   - Distributed tracing
   - Health indicators

---

## Summary

This Redis Utils library provides:

- ✅ Clean, type-safe API hiding Lettuce complexity
- ✅ Production-ready error handling and retry logic
- ✅ Automatic connection lifecycle management
- ✅ Spring Boot auto-configuration
- ✅ Comprehensive configuration options
- ✅ Easy testing with Testcontainers

**Next Steps**:
1. Review and test all implementations
2. Add unit tests for new components
3. Update documentation with examples
4. Publish to Maven Central

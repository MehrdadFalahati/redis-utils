# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned for v1.1.0
- Redis Transactions (MULTI/EXEC/WATCH)
- Pub/Sub messaging support
- Distributed lock implementation (Redlock)
- Enhanced monitoring and metrics

## [1.0.0] - 2025-01-XX

### Added

#### Core Operations
- **String/Value Operations** - Complete Redis string operations
  - Basic set/get with TTL support via `RedisKey` abstraction
  - Conditional operations: `setIfAbsent()` (SETNX), `setIfPresent()` (SETXX)
  - Atomic counters: `increment()`, `incrementBy()`, `decrement()`, `decrementBy()`
  - Batch operations: `multiGet()`, `multiSet()`, `atomicMultiSet()`, `multiSetIfAbsent()`
  - String manipulation: `append()`, `getRange()`, `setRange()`, `strlen()`
  - Atomic get-and-set operations: `getAndDelete()`, `getAndSet()`

- **Hash Operations** - Redis hash support for object storage
  - Single field operations: `get()`, `put()`, `putIfAbsent()`
  - Multi-field operations: `multiGet()`, `putAll()`
  - Bulk retrieval: `entries()`, `keys()`, `values()`
  - Field management: `hasKey()`, `delete()`, `size()`
  - Atomic increments: `increment()` (long and double)

- **List Operations** - Queue and stack implementations
  - Push operations: `leftPush()`, `rightPush()`, `leftPushIfPresent()`, `rightPushIfPresent()`
  - Pop operations: `leftPop()`, `rightPop()`
  - Range operations: `range()`, `index()`
  - List modification: `set()`, `trim()`, `remove()`
  - Size: `size()`

- **Set Operations** - Unique collections with set algebra
  - Member operations: `add()`, `remove()`, `isMember()`, `members()`
  - Random operations: `pop()`, `randomMember()`, `randomMembers()`, `distinctRandomMembers()`
  - Set algebra: `difference()`, `intersect()`, `union()`
  - Cardinality: `size()`

- **Sorted Set Operations** - Score-based rankings and leaderboards
  - Add with scores: `add()`
  - Score operations: `incrementScore()`, `score()`
  - Rank queries: `rank()`, `reverseRank()`
  - Range queries: `range()`, `reverseRange()`, `rangeByScore()`, `reverseRangeByScore()`
  - Count operations: `count()`, `size()`
  - Remove operations: `remove()`, `removeRange()`, `removeRangeByScore()`

- **Key Operations** - Key lifecycle management
  - Existence checks: `exists()`
  - Deletion: `delete()`
  - Expiration: `expire()`, `expireAt()`, `ttl()`
  - Persistence: `persist()`
  - Pattern matching: `keys()` (with production warnings)

#### Infrastructure
- **Multi-Module Architecture**
  - `redis-utils-core` - Core library without Spring Boot dependencies
  - `redis-utils-spring-boot-starter` - Spring Boot auto-configuration
  - `redis-utils-examples` - Example REST API application

- **Type Safety & Serialization**
  - Generic support with automatic type conversion
  - Jackson-based JSON serialization with type preservation
  - Custom ObjectMapper configuration support
  - Java 8+ date/time types support (LocalDateTime, Instant, etc.)
  - Configurable serialization strategies

- **Error Handling & Resilience**
  - Comprehensive exception hierarchy:
    - `RedisException` (base)
    - `RedisConnectionException` - Connection failures
    - `RedisTimeoutException` - Operation timeouts
    - `RedisSerializationException` - Serialization errors
    - `RedisOperationException` - Command failures
  - Automatic retry with exponential backoff
  - Configurable timeout handling
  - Optional circuit breaker support

- **Connection Management**
  - Lettuce-based connection pooling
  - Configurable pool settings (min/max idle, max total)
  - Health checks: `isConnected()` with PING command
  - Graceful shutdown with resource cleanup
  - Connection factory abstraction

- **Configuration**
  - Spring Boot auto-configuration with `@EnableAutoConfiguration`
  - Externalized configuration via `application.yml`/`application.properties`
  - Configuration prefix: `redis.client.*`
  - Conditional bean creation with `@ConditionalOnProperty`
  - Profile-based configuration support
  - Ability to disable auto-configuration

- **Unified Client Interface**
  - `RedisClient` - Single entry point for all operations
  - Fluent API: `opsForValue()`, `opsForHash()`, `opsForList()`, etc.
  - Direct access to operation interfaces
  - Custom command execution via `executeCommand()`

#### Testing
- **Comprehensive Test Suite** (269 tests total)
  - **Unit Tests** (117 tests) - Component isolation with Mockito:
    - RedisKey value object - 34 tests (equality, hashcode, TTL handling)
    - LettuceRedisClient connection management - 26 tests (lifecycle, health checks)
    - RedisCommandExecutor retry logic - 19 tests (exponential backoff, error handling)
    - Serialization framework - 38 tests:
      - JsonRedisSerializer - 8 tests
      - StringRedisSerializer - 12 tests
      - ByteArrayRedisSerializer - 8 tests
      - RedisSerializerRegistry - 10 tests
  - **Integration Tests** (152 tests) - End-to-end with real Redis via Testcontainers:
    - Redis operations - 121 tests:
      - RedisStringOperations - 26 tests (all string/value operations)
      - RedisHashOperations - 16 tests (hash CRUD, increments)
      - RedisListOperations - 17 tests (queue/stack patterns)
      - RedisSetOperations - 17 tests (set algebra operations)
      - RedisZSetOperations - 18 tests (scored sets, leaderboards)
      - RedisKeyOperations - 27 tests (key management, TTL, expiration)
    - SerializationIntegrationIT - 11 tests (end-to-end serialization)
    - RedisClientAutoConfigurationIT - 20 tests (Spring Boot auto-configuration, bean wiring)
  - Performance benchmarks and edge case coverage

- **Test Infrastructure**
  - Testcontainers integration for Redis 7
  - Shared test configuration with RedisTestConfiguration
  - Test utilities and helpers
  - Docker-based integration testing with automatic container management

#### Documentation
- Comprehensive README with:
  - Quick start guide
  - Complete API reference with examples
  - Configuration guide
  - Error handling patterns
  - Best practices
  - Architecture overview
- Javadoc for all public APIs
- Example application with REST endpoints
- Testing guide

### Changed
- Project restructured into multi-module Maven project
- Switched from single module to modular architecture
- Updated to Java 17 as minimum version
- Migrated to Spring Boot 3.3+

### Fixed
- Redis connection lifecycle management
- Proper TTL handling in bulk operations
- Serialization of complex nested objects
- Thread-safety in connection pool
- Memory leaks in failed operations

### Security
- Secure password handling in configuration
- No credential logging
- Safe serialization without code execution
- Dependency vulnerability scanning

## [0.1.0] - 2024-XX-XX (Internal Development)

### Added
- Initial project setup
- Basic Redis operations
- Lettuce client integration
- Spring Boot starter prototype

---

## Migration Guides

### Migrating to 1.0.0

#### From 0.x.x (if applicable)
If you were using a pre-release version:

**Breaking Changes:**
- Package structure changed to multi-module: update imports from `com.github.mehrdadfalahati.redisutils.*`
- Configuration prefix changed to `redis.client.*` (was `redis.*`)
- Some method signatures updated for consistency

**Migration Steps:**
1. Update Maven dependency to use `redis-utils-spring-boot-starter`
2. Update configuration properties prefix from `redis.*` to `redis.client.*`
3. Update imports if using classes from core module directly
4. Review TTL handling - now uses `RedisKey` wrapper consistently
5. Test thoroughly with updated API

**Example:**
```yaml
# Old (0.x.x)
redis:
  host: localhost
  port: 6379

# New (1.0.0)
spring:
  data:
    redis:
      host: localhost
      port: 6379

redis:
  client:
    timeout: 5s
    retry:
      enabled: true
```

---

## Version Support

| Version | Release Date | End of Support | Status |
|---------|--------------|----------------|--------|
| 1.0.x   | 2025-01-XX   | 2026-01-XX     | Active |
| 0.x.x   | -            | 2025-01-XX     | EOL    |

---

## Links

- [GitHub Releases](https://github.com/mehrdadfalahati/redis-utils/releases)
- [Issue Tracker](https://github.com/mehrdadfalahati/redis-utils/issues)
- [Maven Central](https://search.maven.org/artifact/com.github.mehrdadfalahati/redis-utils-spring-boot-starter)

[Unreleased]: https://github.com/mehrdadfalahati/redis-utils/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/mehrdadfalahati/redis-utils/releases/tag/v1.0.0
[0.1.0]: https://github.com/mehrdadfalahati/redis-utils/releases/tag/v0.1.0

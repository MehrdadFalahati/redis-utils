# Testing Guide

This document outlines the testing strategy and practices for the redis-utils project.

## Test Structure

All tests are located in `redis-utils-core/src/test/java` and follow these naming conventions:

- **Unit Tests**: `*Test.java` (e.g., `RedisKeyTest.java`, `LettuceRedisClientTest.java`)
- **Integration Tests**: `*IT.java` (e.g., `RedisStringOperationsIT.java`, `RedisHashOperationsIT.java`)

**Test Count**: 269 total tests
- 117 unit tests (fast, no external dependencies)
- 152 integration tests (with real Redis via Testcontainers)

## Test Categories

### Unit Tests

Unit tests verify individual components in isolation with mocked dependencies.

**Coverage areas:**
- Value objects (RedisKey - equality, hashcode, TTL handling) - 34 tests
- Connection management (LettuceRedisClient with mocked connections) - 26 tests
- Utility classes (RedisCommandExecutor - retry logic, error handling) - 19 tests
- Serialization (JsonRedisSerializer, StringRedisSerializer, ByteArrayRedisSerializer) - 28 tests
- Serializer registry management - 10 tests
- Exception classes (message construction, cause wrapping)
- Configuration property binding

**Run unit tests only:**
```bash
mvn test
```

### Integration Tests

Integration tests use Testcontainers to spin up a real Redis instance and test end-to-end functionality.

**Coverage areas:**
- RedisStringOperations - 26 tests (all string/value operations)
- RedisHashOperations - 16 tests (hash CRUD, increments)
- RedisListOperations - 17 tests (queue/stack patterns)
- RedisSetOperations - 17 tests (set algebra operations)
- RedisZSetOperations - 18 tests (scored sets, leaderboards)
- RedisKeyOperations - 27 tests (key management, TTL, expiration)
- SerializationIntegration - 11 tests (end-to-end serialization with real Redis)
- RedisClientAutoConfiguration - 20 tests (Spring Boot auto-configuration and bean wiring)
- TTL and expiration behavior
- Atomic operations and race conditions
- JSON serialization/deserialization with Jackson
- Connection pooling with Testcontainers

**Run integration tests only:**
```bash
mvn verify -DskipTests
```

**Run all tests (unit + integration):**
```bash
mvn verify
```

## Using Testcontainers

Integration tests use Testcontainers to automatically start a Redis container. Requirements:

1. Docker must be installed and running
2. Testcontainers dependencies are included in `pom.xml`

**Example integration test:**
```java
@SpringBootTest
@Testcontainers
class RedisStringOperationsIT {

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

    // Test methods...
}
```

### Alternative: Local Redis

If you prefer to use a local Redis instance instead of Testcontainers:

1. Remove `@Testcontainers` annotation and `@Container` field
2. Create `src/test/resources/application-test.yml`:
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   ```
3. Ensure Redis is running locally: `redis-server`

## Maven Plugin Configuration

### Surefire (Unit Tests)

Runs during the `test` phase:
- Includes: `**/*Test.java`
- Excludes: `**/*IT.java`

### Failsafe (Integration Tests)

Runs during the `verify` phase:
- Includes: `**/*IT.java`
- Runs after unit tests
- Separates integration test failures from unit test failures

## Continuous Integration

### GitHub Actions Workflows

Two CI workflows are provided:

#### 1. Full CI Pipeline (`ci.yml`)

Comprehensive pipeline with separate jobs:
- **Unit Tests**: Runs on JDK 17 & 21
- **Integration Tests**: Runs on JDK 17 & 21 (after unit tests pass)
- **Full Build**: Creates artifacts on JDK 21
- **Code Quality**: Optional quality checks

**Use when:**
- You want clear separation between test types
- You want parallel execution for faster feedback
- You need detailed artifact collection

#### 2. Simple CI (`ci-simple.yml`)

Streamlined single-job pipeline:
- Runs all tests in one go
- Tests on JDK 17 & 21

**Use when:**
- You want faster configuration
- Your project is small/medium sized
- You prefer simplicity over detailed reporting

### Activating a Workflow

Both workflows are provided. To use one:

1. **Use Full CI**: Keep `ci.yml`, delete `ci-simple.yml`
2. **Use Simple CI**: Keep `ci-simple.yml`, delete `ci.yml`

Or rename the one you don't want (e.g., `ci-simple.yml.disabled`)

## Writing Tests

### Best Practices

1. **Use descriptive test names**: `@DisplayName("Should set and get a simple string value")`
2. **Follow AAA pattern**: Arrange, Act, Assert
3. **Clean up after tests**: Use `@AfterEach` to remove test data
4. **Test edge cases**: null values, empty strings, large datasets
5. **Performance checks**: Assert operations complete within reasonable time
6. **Use test fixtures**: Create reusable test data in `@BeforeEach`

### Example Test Structure

```java
@Test
@DisplayName("Should increment numeric value")
void testIncrement() {
    // Arrange
    String key = "test:counter";

    // Act
    long value1 = stringOperations.increment(key);
    long value2 = stringOperations.increment(key);

    // Assert
    assertEquals(1, value1);
    assertEquals(2, value2);
}
```

## Test Coverage Goals

- **Unit Tests**: >80% coverage for business logic
- **Integration Tests**: 100% coverage for all Redis operations
- **Edge Cases**: Test boundary conditions, errors, timeouts

## Running Tests in IDE

### IntelliJ IDEA

- Right-click test class → Run
- Right-click `src/test/java` → Run All Tests
- Use test runner to filter by name pattern (`*IT` for integration tests)

### VS Code

- Install "Test Runner for Java" extension
- Click "Run Test" above test methods
- Use Test Explorer to run suites

## Troubleshooting

### Testcontainers Issues

**Error: "Could not find a valid Docker environment"**
- Solution: Start Docker Desktop or Docker daemon

**Error: "Port already in use"**
- Solution: Stop existing Redis containers: `docker ps` and `docker stop <container-id>`

**Tests slow to start**
- First run downloads Redis image (one-time)
- Subsequent runs reuse the image

### Test Failures

**Redis connection timeout**
- Check Docker is running
- Verify no firewall blocking container network
- Check container logs: `docker logs <container-id>`

**Serialization errors**
- Ensure test objects are serializable
- Check Jackson configuration in test context

## Performance Benchmarks

Integration tests include basic performance assertions:

- Large string operations: < 1 second
- Bulk set (100 keys): < 500ms
- Single key operations: < 50ms

Adjust thresholds based on your environment in test assertions.

## Future Enhancements

- [ ] Add JaCoCo for code coverage reporting
- [ ] Integrate with SonarQube for quality metrics
- [ ] Add mutation testing with PIT
- [ ] Performance benchmarking with JMH
- [ ] Contract testing for API compatibility

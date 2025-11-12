# Redis Utils Examples

Example Spring Boot application demonstrating the usage of `redis-utils-spring-boot-starter`.

## Prerequisites

- Java 17 or later
- Maven 3.6+
- Redis server running locally (or Docker)

## Quick Start

### 1. Start Redis

Using Docker:
```bash
docker run -d -p 6379:6379 redis:latest
```

Or use your local Redis installation.

### 2. Run the Application

```bash
cd redis-utils-examples
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Project Structure

```
redis-utils-examples/
├── src/main/java/
│   └── com/github/mehrdadfalahati/redisutils/example/
│       ├── RedisExampleApplication.java     # Main application
│       ├── controller/
│       │   └── CacheController.java         # REST endpoints
│       ├── service/
│       │   ├── UserService.java             # RedisValueOperations examples
│       │   └── ProductService.java          # RedisHashOperations examples
│       └── model/
│           └── User.java                    # Domain model
└── src/main/resources/
    └── application.yml                      # Configuration
```

## Configuration

### application.yml

```yaml
redis:
  client:
    enabled: true           # Enable auto-configuration
    timeout: 5s             # Command timeout
    pool-enabled: true      # Enable connection pooling

    pool:
      max-total: 8          # Maximum connections
      max-idle: 8           # Maximum idle connections
      min-idle: 0           # Minimum idle connections
      max-wait: 10s         # Max wait for connection

    retry:
      enabled: true         # Enable retry logic
      max-attempts: 3       # Maximum retry attempts
      initial-delay: 100ms  # Initial delay before retry
      max-delay: 2s         # Maximum delay between retries
      multiplier: 2.0       # Exponential backoff multiplier
```

## API Endpoints

### User Operations (RedisValueOperations)

**Cache a user:**
```bash
curl -X POST http://localhost:8080/api/cache/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": "user123",
    "username": "johndoe",
    "email": "john@example.com",
    "createdAt": "2024-01-01T10:00:00",
    "active": true
  }'
```

**Get cached user:**
```bash
curl http://localhost:8080/api/cache/users/user123
```

**Increment login count:**
```bash
curl -X POST http://localhost:8080/api/cache/users/user123/login
```

**Check if user exists:**
```bash
curl http://localhost:8080/api/cache/users/user123/exists
```

**Delete cached user:**
```bash
curl -X DELETE http://localhost:8080/api/cache/users/user123
```

### Product Operations (RedisHashOperations)

**Save a product:**
```bash
curl -X POST "http://localhost:8080/api/cache/products?productId=prod123" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "prod123",
    "name": "Laptop",
    "price": 999.99,
    "category": "Electronics"
  }'
```

**Get product:**
```bash
curl http://localhost:8080/api/cache/products/prod123
```

**Update product field:**
```bash
curl -X PUT http://localhost:8080/api/cache/products/prod123/fields/price \
  -H "Content-Type: application/json" \
  -d '899.99'
```

**Add inventory:**
```bash
curl -X POST "http://localhost:8080/api/cache/products/prod123/inventory?quantity=50"
```

**Get inventory:**
```bash
curl http://localhost:8080/api/cache/products/prod123/inventory
```

**Get all inventory:**
```bash
curl http://localhost:8080/api/cache/inventory
```

**Delete product:**
```bash
curl -X DELETE http://localhost:8080/api/cache/products/prod123
```

## Code Examples

### UserService - RedisValueOperations

Demonstrates:
- Basic get/set operations with automatic serialization
- TTL-based key expiration using `RedisKey.of()` fluent API
- Conditional operations (`setIfAbsent`)
- Increment operations

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final RedisValueOperations redisValueOperations;

    public void cacheUser(User user) {
        String key = "user:" + user.getId();
        redisValueOperations.set(RedisKey.of(key, Duration.ofHours(1)), user);
    }

    public Optional<User> getCachedUser(String userId) {
        String key = "user:" + userId;
        return Optional.ofNullable(
            redisValueOperations.get(key, User.class)
        );
    }
}
```

### ProductService - RedisHashOperations

Demonstrates:
- Hash field operations (`hSet`, `hGet`, `hGetAll`)
- Batch hash operations
- Hash field existence checks
- Inventory management

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final RedisHashOperations redisHashOperations;

    public void saveProduct(String productId, Map<String, Object> productData) {
        String key = "product:" + productId;
        redisHashOperations.putAll(key, productData);
    }

    public Map<String, Object> getProduct(String productId) {
        String key = "product:" + productId;
        return redisHashOperations.entries(key);
    }
}
```

## Running Tests

The example module includes integration tests that use Testcontainers:

```bash
mvn test
```

## Disabling Auto-Configuration

To disable Redis Utils auto-configuration:

```yaml
redis:
  client:
    enabled: false
```

Or exclude it from auto-configuration:

```java
@SpringBootApplication(exclude = {RedisClientAutoConfiguration.class})
public class RedisExampleApplication {
    // ...
}
```

## Learn More

- [Main README](../README.md) - Complete library documentation
- [Contributing Guide](../CONTRIBUTING.md) - Contributing guidelines and best practices
- [Testing Guide](../TESTING.md) - Testing strategies
- [Release Checklist](../RELEASE_CHECKLIST.md) - Release process documentation

## Troubleshooting

### Connection Refused

If you get "Connection refused" errors, ensure Redis is running:

```bash
# Check if Redis is running
redis-cli ping
# Should return: PONG

# Or check the process
ps aux | grep redis
```

### Serialization Errors

Ensure your domain models are serializable:
- Implement `Serializable` interface
- Have a no-args constructor (for Jackson deserialization)
- Use appropriate Jackson annotations if needed

### Timeout Issues

Increase timeouts in `application.yml`:

```yaml
redis:
  client:
    timeout: 10s
spring:
  data:
    redis:
      timeout: 10s
```

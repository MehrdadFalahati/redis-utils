# Contributing to Redis Utils

Thank you for your interest in contributing to Redis Utils! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)
- [Release Process](#release-process)
- [Community](#community)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of:
- Experience level
- Gender identity and expression
- Sexual orientation
- Disability
- Personal appearance
- Body size
- Race
- Ethnicity
- Age
- Religion
- Nationality

### Expected Behavior

- Be respectful and considerate
- Use welcoming and inclusive language
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards others

### Unacceptable Behavior

- Harassment, trolling, or discriminatory comments
- Personal or political attacks
- Publishing others' private information
- Other conduct that could reasonably be considered inappropriate

### Reporting

If you experience or witness unacceptable behavior, please report it by opening an issue or contacting the project maintainer.

## Getting Started

### Prerequisites

Before contributing, ensure you have:
- **Java 17 or later** (JDK 17 or 21 recommended)
- **Maven 3.6+** for building the project
- **Git** for version control
- **Docker** for running integration tests with Testcontainers
- **IDE** (IntelliJ IDEA or VS Code recommended)

### Finding Issues to Work On

1. **Good First Issues**: Look for issues labeled [`good first issue`](https://github.com/mehrdadfalahati/redis-utils/labels/good%20first%20issue)
2. **Help Wanted**: Check [`help wanted`](https://github.com/mehrdadfalahati/redis-utils/labels/help%20wanted) label
3. **Documentation**: Look for [`documentation`](https://github.com/mehrdadfalahati/redis-utils/labels/documentation) issues
4. **Bugs**: Help fix bugs labeled [`bug`](https://github.com/mehrdadfalahati/redis-utils/labels/bug)

Before starting work on an issue:
1. Comment on the issue to let others know you're working on it
2. Wait for maintainer confirmation if it's a significant change
3. Ask questions if anything is unclear

## How to Contribute

### Types of Contributions

We welcome various types of contributions:

#### 1. Bug Reports

**Before submitting:**
- Search existing issues to avoid duplicates
- Verify the bug exists in the latest version
- Collect necessary information

**When reporting:**
- Use the bug report template
- Provide a clear, descriptive title
- Include steps to reproduce
- Describe expected vs. actual behavior
- Include version information (Java, Spring Boot, Redis)
- Add relevant logs or stack traces
- Attach minimal reproducible example if possible

**Example:**
```markdown
**Description**: RedisValueOperations.get() throws NullPointerException when key doesn't exist

**Steps to Reproduce:**
1. Call redisValueOperations.get("non-existent-key", String.class)
2. Observe NullPointerException

**Expected Behavior:** Should return null
**Actual Behavior:** Throws NullPointerException

**Environment:**
- redis-utils version: 1.0.0
- Java version: 17
- Spring Boot version: 3.3.0
- Redis version: 7.0

**Stack Trace:**
[paste stack trace here]
```

#### 2. Feature Requests

**Before requesting:**
- Check if feature exists in unreleased versions
- Search for similar requests
- Consider if it fits the project scope

**When requesting:**
- Use the feature request template
- Clearly describe the problem it solves
- Provide use cases and examples
- Suggest a possible implementation (optional)
- Consider backward compatibility

**Example:**
```markdown
**Feature:** Add Redis Pub/Sub support

**Problem:** Currently no way to publish/subscribe to Redis channels

**Use Case:**
- Real-time notifications
- Event-driven microservices communication

**Proposed API:**
```java
redisClient.subscribe("channel", message -> {
    // handle message
});

redisClient.publish("channel", "Hello!");
```

**Alternatives Considered:**
- Using Spring's RedisMessageListenerContainer directly (more complex)
```

#### 3. Documentation Improvements

- Fix typos, grammar, or formatting
- Improve code examples
- Add missing documentation
- Clarify confusing sections
- Add tutorials or guides

#### 4. Code Contributions

- Bug fixes
- New features
- Performance improvements
- Code refactoring
- Test improvements

## Development Setup

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/redis-utils.git
cd redis-utils

# Add upstream remote
git remote add upstream https://github.com/mehrdadfalahati/redis-utils.git
```

### 2. Build the Project

```bash
# Build without tests (faster)
mvn clean install -DskipTests

# Full build with all tests
mvn clean verify
```

### 3. Import into IDE

**IntelliJ IDEA:**
1. File → Open → Select `pom.xml`
2. Import as Maven project
3. Wait for dependencies to download

**VS Code:**
1. Install "Extension Pack for Java"
2. Open project folder
3. VS Code will detect Maven project

### 4. Start Redis for Development

```bash
# Using Docker (recommended)
docker run -d -p 6379:6379 --name redis-dev redis:7-alpine

# Or use docker-compose
docker-compose up -d redis
```

### 5. Run Tests

```bash
# Unit tests only (fast)
mvn test

# Integration tests only
mvn verify -DskipTests

# All tests
mvn verify
```

## Coding Guidelines

### Code Style

We follow standard Java conventions:

#### Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: K&R style (opening brace on same line)
- **Imports**: Group and order (java.*, javax.*, org.*, com.*)

#### Naming Conventions
- **Classes**: `PascalCase` (e.g., `RedisValueOperations`)
- **Methods**: `camelCase` (e.g., `getValue()`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.github.mehrdadfalahati.redisutils`)

#### Code Organization
```java
// 1. Package declaration
package com.github.mehrdadfalahati.redisutils.operations;

// 2. Imports (grouped and sorted)
import java.time.Duration;
import java.util.Map;

import com.github.mehrdadfalahati.redisutils.core.RedisKey;

// 3. Class documentation
/**
 * Redis value operations interface.
 * Provides type-safe operations for Redis string/value data type.
 *
 * @author Mehrdad Falahati
 * @since 1.0.0
 */
public interface RedisValueOperations {
    // 4. Methods with Javadoc
    /**
     * Gets the value for the given key.
     *
     * @param key the Redis key
     * @param valueType the expected value type
     * @return the value, or null if key doesn't exist
     * @param <T> the value type
     */
    <T> T get(String key, Class<T> valueType);
}
```

### Documentation

#### Javadoc Requirements
- **All public classes** must have class-level Javadoc
- **All public methods** must have method-level Javadoc
- Use `@param`, `@return`, `@throws` appropriately
- Include `@since` tag for new APIs
- Add code examples for complex methods

**Example:**
```java
/**
 * Increments the numeric value at the given key by delta.
 * If the key doesn't exist, it's initialized to 0 before incrementing.
 *
 * <p>Example:
 * <pre>{@code
 * long count = redisOps.incrementBy("counter", 5);
 * System.out.println("New count: " + count);
 * }</pre>
 *
 * @param key the Redis key
 * @param delta the amount to increment by
 * @return the new value after incrementing
 * @throws RedisOperationException if the operation fails
 * @since 1.0.0
 */
long incrementBy(String key, long delta);
```

### Best Practices

#### 1. Null Safety
- Use `Optional<T>` for methods that may return null
- Validate parameters with `Objects.requireNonNull()`
- Document null behavior in Javadoc

```java
public <T> Optional<T> get(String key, Class<T> valueType) {
    Objects.requireNonNull(key, "Key must not be null");
    Objects.requireNonNull(valueType, "Value type must not be null");
    // ...
}
```

#### 2. Exception Handling
- Use custom exception hierarchy (`RedisException` and subclasses)
- Include meaningful error messages
- Preserve stack traces with exception chaining
- Don't catch generic `Exception` unless necessary

```java
try {
    return redisTemplate.opsForValue().get(key);
} catch (Exception e) {
    throw new RedisOperationException(
        "Failed to get value for key: " + key, e
    );
}
```

#### 3. Resource Management
- Use try-with-resources for closeable resources
- Clean up resources in finally blocks if needed
- Be careful with connection leaks

#### 4. Thread Safety
- Ensure operations are thread-safe
- Document thread-safety guarantees
- Use appropriate synchronization if needed

#### 5. Performance
- Avoid unnecessary object creation
- Use batch operations when appropriate
- Consider connection pooling
- Profile before optimizing

### Testing Guidelines

#### Test Structure
```java
@Test
@DisplayName("Should increment value by delta")
void testIncrementBy() {
    // Arrange (Given)
    String key = "test:counter";
    long delta = 5;
    redisOps.set(key, 10L);

    // Act (When)
    long result = redisOps.incrementBy(key, delta);

    // Assert (Then)
    assertEquals(15L, result);
    assertEquals(15L, redisOps.get(key, Long.class));
}
```

#### Test Coverage Requirements
- **Unit tests**: Test individual components in isolation
- **Integration tests**: Test end-to-end with real Redis
- **Edge cases**: Test null values, empty strings, large datasets
- **Error cases**: Test exception scenarios
- **Minimum coverage**: 80% for new code

#### Test Naming
- Use descriptive test names: `should<ExpectedBehavior>When<Condition>()`
- Use `@DisplayName` for readable descriptions
- Group related tests with nested classes

```java
@Nested
@DisplayName("Increment operations")
class IncrementOperations {

    @Test
    @DisplayName("Should increment non-existent key to 1")
    void shouldInitializeToOneWhenKeyDoesNotExist() {
        // test code
    }

    @Test
    @DisplayName("Should throw exception when key contains non-numeric value")
    void shouldThrowExceptionWhenValueIsNotNumeric() {
        // test code
    }
}
```

## Pull Request Process

### 1. Create a Feature Branch

```bash
# Update your fork
git fetch upstream
git checkout main
git merge upstream/main

# Create feature branch
git checkout -b feature/add-pub-sub-support
# or
git checkout -b fix/null-pointer-in-get-operation
```

**Branch naming conventions:**
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation changes
- `refactor/` - Code refactoring
- `test/` - Test improvements
- `chore/` - Maintenance tasks

### 2. Make Your Changes

- Write clean, well-documented code
- Follow coding guidelines
- Add/update tests
- Update documentation if needed
- Keep commits atomic and focused

### 3. Commit Your Changes

Write clear, descriptive commit messages:

```bash
# Good commit messages
git commit -m "feat: add Redis Pub/Sub support for channel messaging"
git commit -m "fix: handle null return value in RedisValueOperations.get()"
git commit -m "docs: update README with Pub/Sub examples"
git commit -m "test: add integration tests for Pub/Sub operations"

# Follow conventional commits format
# <type>: <description>
#
# Types: feat, fix, docs, style, refactor, test, chore
```

**Commit message format:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Example:**
```
feat(operations): add Redis Pub/Sub support

Implement RedisMessagingOperations interface with:
- Channel subscription/unsubscription
- Message publishing
- Pattern-based subscriptions
- Message listener registration

Closes #123
```

### 4. Run Tests

```bash
# Ensure all tests pass
mvn clean verify

# Check code style (if configured)
mvn checkstyle:check
```

### 5. Push and Create Pull Request

```bash
# Push to your fork
git push origin feature/add-pub-sub-support

# Then create PR on GitHub
```

### 6. Pull Request Template

Your PR description should include:

```markdown
## Description
Brief description of changes

## Related Issues
Closes #123
Related to #456

## Type of Change
- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Breaking change (fix or feature causing existing functionality to change)
- [ ] Documentation update
- [ ] Code refactoring
- [ ] Test improvement

## Testing
Describe how you tested your changes:
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed
- [ ] All tests pass locally

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Code is well-commented
- [ ] Documentation updated
- [ ] No new warnings introduced
- [ ] Tests added for new functionality
- [ ] All tests pass
- [ ] CHANGELOG.md updated (for notable changes)

## Screenshots (if applicable)
Add screenshots for UI changes

## Additional Notes
Any additional information for reviewers
```

### 7. Code Review Process

**What to expect:**
1. Maintainers will review your PR within 1-7 days
2. You may receive feedback or change requests
3. Address feedback by pushing new commits
4. PR will be merged once approved

**During review:**
- Be responsive to feedback
- Explain your design decisions
- Be open to suggestions
- Keep discussions professional and constructive

**After approval:**
- Maintainer will merge your PR
- Your changes will be included in the next release
- You'll be credited in CHANGELOG.md

## Release Process

Contributors don't typically perform releases, but understanding the process helps:

1. **Version Bump**: Maintainers update version in `pom.xml`
2. **CHANGELOG Update**: Notable changes are documented
3. **Testing**: Full test suite runs on CI
4. **Tag Creation**: Git tag created (e.g., `v1.1.0`)
5. **Artifact Publishing**: Published to Maven Central
6. **GitHub Release**: Release notes published

See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) for detailed process.

## Community

### Communication Channels

- **GitHub Issues**: Bug reports, feature requests
- **GitHub Discussions**: Q&A, ideas, general discussion
- **Pull Requests**: Code contributions, reviews

### Getting Help

If you need help:
1. Check existing documentation (README, wiki)
2. Search GitHub Issues and Discussions
3. Ask in GitHub Discussions
4. Tag maintainers if urgent

### Recognition

Contributors are recognized in:
- CHANGELOG.md for notable contributions
- GitHub contributors page
- Release notes

## Project Structure

Understanding the project structure helps with contributions:

```
redis-utils/
├── redis-utils-core/              # Core library (no Spring Boot)
│   ├── client/                    # Client interfaces
│   ├── operations/                # Operation interfaces
│   │   └── impl/                  # Default implementations
│   ├── lettuce/                   # Lettuce-specific code
│   ├── serialization/             # Serialization support
│   ├── exception/                 # Exception hierarchy
│   └── util/                      # Utility classes
│
├── redis-utils-spring-boot-starter/  # Spring Boot auto-config
│   ├── config/                    # Auto-configuration classes
│   └── META-INF/                  # Spring Boot metadata
│
├── redis-utils-examples/          # Example application
│   ├── controller/                # REST endpoints
│   ├── service/                   # Business logic
│   └── model/                     # Domain models
│
├── .github/workflows/             # CI/CD workflows
├── CHANGELOG.md                   # Version history
├── CONTRIBUTING.md                # This file
├── LICENSE                        # Apache 2.0 license
├── README.md                      # Main documentation
├── RELEASE_CHECKLIST.md          # Release process
└── TESTING.md                     # Testing guide
```

## Additional Resources

- [README.md](README.md) - Project overview and usage
- [TESTING.md](TESTING.md) - Testing guidelines
- [CHANGELOG.md](CHANGELOG.md) - Version history
- [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) - Release process
- [Semantic Versioning](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Keep a Changelog](https://keepachangelog.com/)

## Questions?

If you have questions about contributing:
1. Check this guide first
2. Search GitHub Discussions
3. Open a new discussion
4. Tag maintainers if needed

Thank you for contributing to Redis Utils! 🚀

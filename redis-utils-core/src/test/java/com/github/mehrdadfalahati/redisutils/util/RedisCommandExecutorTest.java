package com.github.mehrdadfalahati.redisutils.util;

import com.github.mehrdadfalahati.redisutils.config.RedisProperties;
import com.github.mehrdadfalahati.redisutils.exception.RedisConnectionException;
import com.github.mehrdadfalahati.redisutils.exception.RedisException;
import com.github.mehrdadfalahati.redisutils.exception.RedisTimeoutException;
import io.lettuce.core.RedisCommandTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RedisCommandExecutor.
 * Tests retry logic, timeout handling, and error scenarios with mocked operations.
 */
class RedisCommandExecutorTest {

    private RedisProperties properties;
    private RedisCommandExecutor executor;

    @BeforeEach
    void setUp() {
        properties = new RedisProperties();
        executor = new RedisCommandExecutor(properties);
    }

    // ========== Successful Execution Tests ==========

    @Test
    @DisplayName("Should execute command successfully without retry")
    void testSuccessfulExecution() {
        Supplier<String> command = () -> "SUCCESS";

        String result = executor.execute("test-operation", command);

        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("Should return null when command returns null")
    void testNullReturn() {
        Supplier<String> command = () -> null;

        String result = executor.execute("test-operation", command);

        assertNull(result);
    }

    // ========== Retry Disabled Tests ==========

    @Test
    @DisplayName("Should not retry when retry is disabled")
    void testNoRetryWhenDisabled() {
        properties.getRetry().setEnabled(false);
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        assertThrows(RedisConnectionException.class,
                () -> executor.execute("test-operation", command));

        assertEquals(1, attempts.get(), "Should only attempt once when retry is disabled");
    }

    // ========== Connection Exception Retry Tests ==========

    @Test
    @DisplayName("Should retry on connection exception")
    void testRetryOnConnectionException() {
        properties.getRetry().setMaxAttempts(3);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(10)); // Fast for testing
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new io.lettuce.core.RedisConnectionException("Connection failed");
            }
            return "SUCCESS";
        };

        String result = executor.execute("test-operation", command);

        assertEquals("SUCCESS", result);
        assertEquals(3, attempts.get(), "Should attempt 3 times before success");
    }

    @Test
    @DisplayName("Should fail after max retry attempts on connection exception")
    void testFailAfterMaxRetriesOnConnectionException() {
        properties.getRetry().setMaxAttempts(3);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        RedisException exception = assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        assertEquals(3, attempts.get(), "Should attempt exactly max attempts");
        assertTrue(exception.getMessage().contains("failed after 3 attempts"));
        assertInstanceOf(RedisConnectionException.class, exception.getCause());
    }

    // ========== Timeout Exception Tests ==========

    @Test
    @DisplayName("Should not retry on timeout by default")
    void testNoRetryOnTimeoutByDefault() {
        properties.getRetry().setRetryOnTimeout(false);
        properties.getRetry().setMaxAttempts(3);
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new RedisCommandTimeoutException();
        };

        assertThrows(RedisTimeoutException.class,
                () -> executor.execute("test-operation", command));

        assertEquals(1, attempts.get(), "Should not retry on timeout by default");
    }

    @Test
    @DisplayName("Should retry on timeout when enabled")
    void testRetryOnTimeoutWhenEnabled() {
        properties.getRetry().setRetryOnTimeout(true);
        properties.getRetry().setMaxAttempts(3);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new RedisCommandTimeoutException();
            }
            return "SUCCESS";
        };

        String result = executor.execute("test-operation", command);

        assertEquals("SUCCESS", result);
        assertEquals(2, attempts.get(), "Should retry on timeout when enabled");
    }

    // ========== Other Exception Tests ==========

    @Test
    @DisplayName("Should not retry on RedisException (non-transient error)")
    void testNoRetryOnRedisException() {
        properties.getRetry().setMaxAttempts(3);
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new RedisException("Operation error");
        };

        assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        assertEquals(1, attempts.get(), "Should not retry on RedisException");
    }

    @Test
    @DisplayName("Should wrap generic exceptions in RedisException")
    void testWrapGenericException() {
        Supplier<String> command = () -> {
            throw new RuntimeException("Generic error");
        };

        RedisException exception = assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        assertTrue(exception.getMessage().contains("test-operation"));
        assertTrue(exception.getMessage().contains("failed"));
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    // ========== Backoff Tests ==========

    @Test
    @DisplayName("Should apply exponential backoff between retries")
    void testExponentialBackoff() {
        properties.getRetry().setMaxAttempts(4);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(50));
        properties.getRetry().setBackoffMultiplier(2.0);
        properties.getRetry().setMaxBackoff(Duration.ofSeconds(1));

        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        long duration = System.currentTimeMillis() - startTime;

        assertEquals(4, attempts.get());
        // Should have delays: 50ms, 100ms, 200ms = 350ms minimum
        assertTrue(duration >= 300, "Should apply backoff delays between retries");
    }

    @Test
    @DisplayName("Should cap backoff at max backoff duration")
    void testBackoffCappedAtMax() {
        properties.getRetry().setMaxAttempts(5);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(100));
        properties.getRetry().setBackoffMultiplier(10.0); // Very high multiplier
        properties.getRetry().setMaxBackoff(Duration.ofMillis(200)); // But capped at 200ms

        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        long duration = System.currentTimeMillis() - startTime;

        assertEquals(5, attempts.get());
        // Even with high multiplier, should not exceed: 100 + 200 + 200 + 200 = 700ms
        assertTrue(duration < 1000, "Backoff should be capped at max backoff");
    }

    // ========== Interrupt Handling Tests ==========

    @Test
    @DisplayName("Should handle thread interruption during retry")
    void testInterruptDuringRetry() {
        properties.getRetry().setMaxAttempts(3);
        properties.getRetry().setInitialBackoff(Duration.ofSeconds(10)); // Long delay

        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        // Interrupt the thread during retry
        Thread testThread = new Thread(() -> {
            assertThrows(RedisException.class,
                    () -> executor.execute("test-operation", command));
        });

        testThread.start();
        try {
            Thread.sleep(100); // Let first attempt fail
            testThread.interrupt(); // Interrupt during backoff sleep
            testThread.join(1000);
        } catch (InterruptedException e) {
            fail("Test thread interrupted");
        }

        // Should have attempted at least once, may attempt twice
        assertTrue(attempts.get() >= 1);
        assertTrue(testThread.isInterrupted() || !testThread.isAlive());
    }

    // ========== Configuration Tests ==========

    @Test
    @DisplayName("Should use configured max attempts")
    void testConfiguredMaxAttempts() {
        properties.getRetry().setMaxAttempts(5);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        assertEquals(5, attempts.get(), "Should use configured max attempts");
    }

    @Test
    @DisplayName("Should handle zero initial backoff")
    void testZeroInitialBackoff() {
        properties.getRetry().setMaxAttempts(2);
        properties.getRetry().setInitialBackoff(Duration.ZERO);
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            attempts.incrementAndGet();
            throw new io.lettuce.core.RedisConnectionException("Connection failed");
        };

        assertThrows(RedisException.class,
                () -> executor.execute("test-operation", command));

        assertEquals(2, attempts.get());
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Should handle command that throws Error")
    void testCommandThrowsError() {
        Supplier<String> command = () -> {
            throw new OutOfMemoryError("Critical error");
        };

        // Should propagate Error, not catch it
        assertThrows(OutOfMemoryError.class,
                () -> executor.execute("test-operation", command));
    }

    @Test
    @DisplayName("Should succeed on last retry attempt")
    void testSuccessOnLastAttempt() {
        properties.getRetry().setMaxAttempts(3);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new io.lettuce.core.RedisConnectionException("Connection failed");
            }
            return "SUCCESS";
        };

        String result = executor.execute("test-operation", command);

        assertEquals("SUCCESS", result);
        assertEquals(3, attempts.get(), "Should succeed on last attempt");
    }

    @Test
    @DisplayName("Should handle mix of different transient exceptions")
    void testMixOfTransientExceptions() {
        properties.getRetry().setMaxAttempts(4);
        properties.getRetry().setRetryOnTimeout(true);
        properties.getRetry().setInitialBackoff(Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> command = () -> {
            int attempt = attempts.incrementAndGet();
            return switch (attempt) {
                case 1 -> throw new io.lettuce.core.RedisConnectionException("Connection failed");
                case 2 -> throw new RedisCommandTimeoutException();
                case 3 -> throw new io.lettuce.core.RedisConnectionException("Connection failed again");
                default -> "SUCCESS";
            };
        };

        String result = executor.execute("test-operation", command);

        assertEquals("SUCCESS", result);
        assertEquals(4, attempts.get(), "Should retry through different transient exceptions");
    }

    // ========== Lettuce Exception Wrapping Tests ==========

    @Test
    @DisplayName("Should wrap Lettuce connection exception in RedisConnectionException")
    void testWrapLettuceConnectionException() {
        Supplier<String> command = () -> {
            throw new io.lettuce.core.RedisConnectionException("Lettuce connection error");
        };

        properties.getRetry().setEnabled(false);

        RedisConnectionException exception = assertThrows(RedisConnectionException.class,
                () -> executor.execute("test-operation", command));

        assertTrue(exception.getMessage().contains("test-operation"));
        assertInstanceOf(io.lettuce.core.RedisConnectionException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should wrap Lettuce timeout exception in RedisTimeoutException")
    void testWrapLettuceTimeoutException() {
        Supplier<String> command = () -> {
            throw new RedisCommandTimeoutException();
        };

        properties.getRetry().setEnabled(false);
        properties.setTimeout(Duration.ofSeconds(5));

        RedisTimeoutException exception = assertThrows(RedisTimeoutException.class,
                () -> executor.execute("test-operation", command));

        assertTrue(exception.getMessage().contains("test-operation"));
        assertTrue(exception.getMessage().contains("timed out"));
        assertEquals(5000, exception.getTimeoutMillis());
    }
}

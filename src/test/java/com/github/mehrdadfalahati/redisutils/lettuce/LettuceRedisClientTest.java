package com.github.mehrdadfalahati.redisutils.lettuce;

import com.github.mehrdadfalahati.redisutils.client.RedisClient;
import com.github.mehrdadfalahati.redisutils.exception.RedisConnectionException;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LettuceRedisClient.
 * Tests connection management, state checking, and command execution with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class LettuceRedisClientTest {

    @Mock
    private RedisKeyOperations keyOperations;

    @Mock
    private RedisValueOperations valueOperations;

    @Mock
    private StatefulRedisConnection<String, Object> connection;

    @Mock
    private RedisCommands<String, Object> syncCommands;

    private LettuceRedisClient client;

    @BeforeEach
    void setUp() {
        client = new LettuceRedisClient(keyOperations, valueOperations, connection);
    }

    // ========== Operations Access Tests ==========

    @Test
    @DisplayName("Should return key operations when client is open")
    void testKeyOps() {
        RedisKeyOperations ops = client.keyOps();
        assertSame(keyOperations, ops);
    }

    @Test
    @DisplayName("Should return value operations when client is open")
    void testValueOps() {
        RedisValueOperations ops = client.valueOps();
        assertSame(valueOperations, ops);
    }

    @Test
    @DisplayName("Should throw exception when accessing key ops after close")
    void testKeyOpsAfterClose() {
        client.close();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.keyOps());

        assertTrue(exception.getMessage().contains("closed"));
    }

    @Test
    @DisplayName("Should throw exception when accessing value ops after close")
    void testValueOpsAfterClose() {
        client.close();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.valueOps());

        assertTrue(exception.getMessage().contains("closed"));
    }

    // ========== Connection State Tests ==========

    @Test
    @DisplayName("Should return true when connected and ping succeeds")
    void testIsConnectedSuccess() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.ping()).thenReturn("PONG");

        assertTrue(client.isConnected());

        verify(connection).sync();
        verify(syncCommands).ping();
    }

    @Test
    @DisplayName("Should return false when connection is null")
    void testIsConnectedNullConnection() {
        LettuceRedisClient clientWithNullConnection =
                new LettuceRedisClient(keyOperations, valueOperations, null);

        assertFalse(clientWithNullConnection.isConnected());
    }

    @Test
    @DisplayName("Should return false when client is closed")
    void testIsConnectedAfterClose() {
        client.close();

        assertFalse(client.isConnected());

        // Should not even attempt to ping
        verify(connection, never()).sync();
    }

    @Test
    @DisplayName("Should return false when ping throws exception")
    void testIsConnectedPingException() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.ping()).thenThrow(new RuntimeException("Connection error"));

        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Should return false when ping returns unexpected response")
    void testIsConnectedUnexpectedPingResponse() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.ping()).thenReturn("UNEXPECTED");

        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Should handle ping response case insensitively")
    void testIsConnectedCaseInsensitive() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.ping()).thenReturn("pong");

        assertTrue(client.isConnected());
    }

    // ========== Close Tests ==========

    @Test
    @DisplayName("Should close connection successfully")
    void testCloseSuccess() {
        client.close();

        verify(connection).close();
        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Should handle exception during close gracefully")
    void testCloseWithException() {
        doThrow(new RuntimeException("Close error")).when(connection).close();

        // Should not throw exception
        assertDoesNotThrow(() -> client.close());

        verify(connection).close();
    }

    @Test
    @DisplayName("Should be idempotent - multiple close calls are safe")
    void testCloseIdempotent() {
        client.close();
        client.close();
        client.close();

        // Connection should only be closed once
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Should not attempt to close null connection")
    void testCloseNullConnection() {
        LettuceRedisClient clientWithNullConnection =
                new LettuceRedisClient(keyOperations, valueOperations, null);

        assertDoesNotThrow(() -> clientWithNullConnection.close());
    }

    // ========== Execute Command Tests ==========

    @Test
    @DisplayName("Should execute command successfully")
    void testExecuteCommandSuccess() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.get("test-key")).thenReturn("test-value");

        String result = client.executeCommand(commands -> {
            @SuppressWarnings("unchecked")
            RedisCommands<String, Object> cmd = (RedisCommands<String, Object>) commands;
            return (String) cmd.get("test-key");
        });

        assertEquals("test-value", result);
        verify(connection).sync();
    }

    @Test
    @DisplayName("Should throw exception when executing command after close")
    void testExecuteCommandAfterClose() {
        client.close();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.executeCommand(commands -> "test"));

        assertTrue(exception.getMessage().contains("closed"));
        verify(connection, never()).sync();
    }

    @Test
    @DisplayName("Should throw exception when connection is null")
    void testExecuteCommandNullConnection() {
        LettuceRedisClient clientWithNullConnection =
                new LettuceRedisClient(keyOperations, valueOperations, null);

        RedisConnectionException exception = assertThrows(RedisConnectionException.class,
                () -> clientWithNullConnection.executeCommand(commands -> "test"));

        assertTrue(exception.getMessage().contains("No Redis connection available"));
    }

    @Test
    @DisplayName("Should wrap callback exceptions in RedisConnectionException")
    void testExecuteCommandCallbackException() {
        when(connection.sync()).thenReturn(syncCommands);

        RedisClient.RedisCommandCallback<String> callback = commands -> {
            throw new RuntimeException("Command failed");
        };

        RedisConnectionException exception = assertThrows(RedisConnectionException.class,
                () -> client.executeCommand(callback));

        assertTrue(exception.getMessage().contains("Failed to execute custom Redis command"));
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should handle command that returns null")
    void testExecuteCommandReturnsNull() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.get("non-existent")).thenReturn(null);

        String result = client.executeCommand(commands -> {
            @SuppressWarnings("unchecked")
            RedisCommands<String, Object> cmd = (RedisCommands<String, Object>) commands;
            return (String) cmd.get("non-existent");
        });

        assertNull(result);
    }

    @Test
    @DisplayName("Should execute multiple commands in sequence")
    void testExecuteMultipleCommands() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.set("key1", "value1")).thenReturn("OK");
        when(syncCommands.set("key2", "value2")).thenReturn("OK");

        String result1 = client.executeCommand(commands -> {
            @SuppressWarnings("unchecked")
            RedisCommands<String, Object> cmd = (RedisCommands<String, Object>) commands;
            return cmd.set("key1", "value1");
        });
        String result2 = client.executeCommand(commands -> {
            @SuppressWarnings("unchecked")
            RedisCommands<String, Object> cmd = (RedisCommands<String, Object>) commands;
            return cmd.set("key2", "value2");
        });

        assertEquals("OK", result1);
        assertEquals("OK", result2);
        verify(connection, times(2)).sync();
    }

    // ========== Complex Scenarios ==========

    @Test
    @DisplayName("Should handle operations lifecycle correctly")
    void testOperationsLifecycle() {
        // Initial state - should be operational
        assertNotNull(client.keyOps());
        assertNotNull(client.valueOps());

        // After close - should not be operational
        client.close();

        assertThrows(IllegalStateException.class, () -> client.keyOps());
        assertThrows(IllegalStateException.class, () -> client.valueOps());
        assertThrows(IllegalStateException.class,
                () -> client.executeCommand(commands -> "test"));
    }

    @Test
    @DisplayName("Should maintain connection state correctly")
    void testConnectionStateManagement() {
        // Setup connected state
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.ping()).thenReturn("PONG");

        // Initially connected
        assertTrue(client.isConnected());

        // After close, not connected
        client.close();
        assertFalse(client.isConnected());

        // Multiple close calls don't change state
        client.close();
        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Should execute complex command callback")
    void testComplexCommandCallback() {
        when(connection.sync()).thenReturn(syncCommands);
        when(syncCommands.set("key", "value")).thenReturn("OK");
        when(syncCommands.get("key")).thenReturn("value");
        when(syncCommands.del("key")).thenReturn(1L);

        Long result = client.executeCommand(commands -> {
            @SuppressWarnings("unchecked")
            RedisCommands<String, Object> cmd = (RedisCommands<String, Object>) commands;
            cmd.set("key", "value");
            String value = (String) cmd.get("key");
            assertEquals("value", value);
            return cmd.del("key");
        });

        assertEquals(1L, result);
        verify(syncCommands).set("key", "value");
        verify(syncCommands).get("key");
        verify(syncCommands).del("key");
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Should handle rapid open/close cycles")
    void testRapidOpenClose() {
        for (int i = 0; i < 10; i++) {
            client.close();
        }

        verify(connection, times(1)).close();
        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Should handle concurrent close attempts")
    void testConcurrentClose() throws InterruptedException {
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> client.close());
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Connection should only be closed once despite concurrent calls
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Should handle sync() returning null")
    void testSyncReturnsNull() {
        when(connection.sync()).thenReturn(null);

        // The callback will receive null and throw NullPointerException when trying to use it
        RedisConnectionException exception = assertThrows(RedisConnectionException.class,
                () -> client.executeCommand(commands -> {
                    @SuppressWarnings("unchecked")
                    RedisCommands<String, Object> cmd = (RedisCommands<String, Object>) commands;
                    return cmd.get("test"); // This will throw NPE since commands is null
                }));

        assertTrue(exception.getMessage().contains("Failed to execute custom Redis command"));
    }
}

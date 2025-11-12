package com.github.mehrdadfalahati.redisutils.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example Spring Boot application demonstrating Redis Utils usage.
 *
 * <p>This application showcases how to use the redis-utils-spring-boot-starter
 * with automatic configuration and dependency injection.
 *
 * <p>To run this application:
 * <ol>
 *   <li>Ensure Redis is running locally on port 6379, or</li>
 *   <li>Configure connection in application.yml</li>
 * </ol>
 *
 * @see UserService for examples of RedisValueOperations usage
 * @see ProductService for examples of RedisHashOperations usage
 * @see CacheController for REST API examples
 */
@SpringBootApplication
public class RedisExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisExampleApplication.class, args);
    }
}

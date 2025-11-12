package com.github.mehrdadfalahati.redisutils.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Minimal Spring Boot application for testing auto-configuration.
 * Unlike RedisTestConfiguration, this does NOT exclude RedisClientAutoConfiguration,
 * allowing us to test that the auto-configuration works properly.
 */
@SpringBootApplication
@Import({RedisTemplateConfiguration.class, RedisSerializationConfiguration.class})
public class AutoConfigTestApp {
    // No beans needed - we're testing that auto-configuration creates them all
}

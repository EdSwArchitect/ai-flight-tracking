package com.militarytracker.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void shouldLoadDefaultConfig() {
        AppConfig config = AppConfig.load();
        assertNotNull(config);
        assertTrue(config.hasPath("kafka.bootstrap-servers"));
        assertEquals("localhost:9092", config.getString("kafka.bootstrap-servers"));
    }

    @Test
    void shouldLoadKafkaDefaults() {
        AppConfig config = AppConfig.load();
        assertEquals("military_flights", config.getString("kafka.topic"));
        assertEquals("all", config.getString("kafka.acks"));
    }

    @Test
    void shouldLoadDatabaseDefaults() {
        AppConfig config = AppConfig.load();
        assertEquals("jdbc:postgresql://localhost:5432/militarytracker", config.getString("database.url"));
        assertEquals(10, config.getInt("database.pool.max-size"));
    }

    @Test
    void shouldLoadMetricsPort() {
        AppConfig config = AppConfig.load();
        assertEquals(9090, config.getInt("metrics.port"));
    }

    @Test
    void shouldLoadHealthPort() {
        AppConfig config = AppConfig.load();
        assertEquals(8081, config.getInt("health.port"));
    }

    @Test
    void shouldSupportHasPath() {
        AppConfig config = AppConfig.load();
        assertTrue(config.hasPath("kafka.bootstrap-servers"));
        assertFalse(config.hasPath("nonexistent.path"));
    }
}

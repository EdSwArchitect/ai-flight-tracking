package com.militarytracker.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonMapperTest {

    @Test
    void shouldReturnSingletonInstance() {
        ObjectMapper mapper1 = JsonMapper.get();
        ObjectMapper mapper2 = JsonMapper.get();
        assertSame(mapper1, mapper2);
    }

    @Test
    void shouldIgnoreUnknownProperties() throws Exception {
        String json = "{\"known\":\"value\",\"unknown\":\"ignored\"}";
        Map<?, ?> result = JsonMapper.get().readValue(json, Map.class);
        assertEquals("value", result.get("known"));
        assertEquals("ignored", result.get("unknown"));
    }

    @Test
    void shouldHandleJavaTimeTypes() throws Exception {
        Instant now = Instant.parse("2026-02-27T12:00:00Z");
        String json = JsonMapper.get().writeValueAsString(Map.of("time", now));
        assertTrue(json.contains("2026-02-27"));
    }
}

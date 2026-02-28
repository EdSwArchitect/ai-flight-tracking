package com.militarytracker.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoBoxRequestTest {

    @Test
    void validBoundingBox() {
        GeoBoxRequest box = new GeoBoxRequest(40.0, 38.0, -74.0, -78.0);
        assertTrue(box.isValid());
    }

    @Test
    void invalidWhenNorthLessThanSouth() {
        GeoBoxRequest box = new GeoBoxRequest(38.0, 40.0, -74.0, -78.0);
        assertFalse(box.isValid());
    }

    @Test
    void invalidWhenLatOutOfRange() {
        GeoBoxRequest box = new GeoBoxRequest(91.0, 38.0, -74.0, -78.0);
        assertFalse(box.isValid());
    }

    @Test
    void invalidWhenLonOutOfRange() {
        GeoBoxRequest box = new GeoBoxRequest(40.0, 38.0, 181.0, -78.0);
        assertFalse(box.isValid());
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String json = "{\"north\":40.0,\"south\":38.0,\"east\":-74.0,\"west\":-78.0}";
        GeoBoxRequest box = mapper.readValue(json, GeoBoxRequest.class);

        assertEquals(40.0, box.getNorth());
        assertEquals(38.0, box.getSouth());
        assertEquals(-74.0, box.getEast());
        assertEquals(-78.0, box.getWest());
        assertTrue(box.isValid());
    }
}

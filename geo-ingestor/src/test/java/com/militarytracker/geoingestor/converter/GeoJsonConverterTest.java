package com.militarytracker.geoingestor.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militarytracker.model.api.AcItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoJsonConverterTest {

    private GeoJsonConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        converter = new GeoJsonConverter(objectMapper);
    }

    @Test
    void convertShouldProduceValidGeoJson() throws Exception {
        AcItem item = new AcItem();
        item.setHex("ae1234");
        item.setLat(38.8977);
        item.setLon(-77.0365);
        item.setAltBaro(35000);
        item.setGroundSpeed(450.5);
        item.setTrack(180.0);
        item.setFlight("VALOR01 ");
        item.setAircraftType("C17");

        String geoJson = converter.convert(item);
        assertNotNull(geoJson);

        var tree = objectMapper.readTree(geoJson);
        assertEquals("Feature", tree.get("type").asText());
        assertEquals("Point", tree.get("geometry").get("type").asText());

        var coords = tree.get("geometry").get("coordinates");
        assertEquals(-77.0365, coords.get(0).asDouble(), 0.001);
        assertEquals(38.8977, coords.get(1).asDouble(), 0.001);
        assertEquals(35000.0, coords.get(2).asDouble(), 0.1);

        var props = tree.get("properties");
        assertEquals("ae1234", props.get("hex").asText());
        assertEquals("VALOR01", props.get("flight").asText());
    }

    @Test
    void convertShouldHandleOnGround() throws Exception {
        AcItem item = new AcItem();
        item.setHex("ae5678");
        item.setLat(40.0);
        item.setLon(-74.0);
        item.setAltBaro("ground");

        String geoJson = converter.convert(item);
        assertNotNull(geoJson);

        var tree = objectMapper.readTree(geoJson);
        var coords = tree.get("geometry").get("coordinates");
        assertEquals(0.0, coords.get(2).asDouble(), 0.1);
    }

    @Test
    void convertShouldHandleNullAltitude() throws Exception {
        AcItem item = new AcItem();
        item.setHex("ae9999");
        item.setLat(35.0);
        item.setLon(-80.0);

        String geoJson = converter.convert(item);
        assertNotNull(geoJson);

        var tree = objectMapper.readTree(geoJson);
        assertEquals(0.0, tree.get("geometry").get("coordinates").get(2).asDouble(), 0.1);
    }
}

package com.militarytracker.model.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class V2ResponseTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void shouldDeserializeV2Response() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/sample-v2-response.json")) {
            V2Response response = mapper.readValue(is, V2Response.class);

            assertEquals(1709052000L, response.getNow());
            assertEquals(2, response.getTotal());
            assertNotNull(response.getAc());
            assertEquals(2, response.getAc().size());
        }
    }

    @Test
    void shouldDeserializeAcItemWithNumericAltBaro() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/sample-v2-response.json")) {
            V2Response response = mapper.readValue(is, V2Response.class);
            AcItem item = response.getAc().get(0);

            assertEquals("AE1234", item.getHex());
            assertEquals("RCH405", item.getFlight());
            assertEquals("05-5139", item.getRegistration());
            assertEquals("C17", item.getAircraftType());
            assertEquals("Boeing C-17A Globemaster III", item.getDescription());
            assertEquals("United States Air Force", item.getOperator());
            assertEquals(38.8951, item.getLat());
            assertEquals(-77.0364, item.getLon());
            assertEquals(35000, item.getAltBaroFeet());
            assertFalse(item.isOnGround());
            assertEquals(35150, item.getAltGeom());
            assertEquals(450.2, item.getGroundSpeed());
            assertEquals(270.5, item.getTrack());
            assertEquals(-128, item.getVerticalRate());
            assertEquals("1234", item.getSquawk());
            assertEquals("A5", item.getCategory());
        }
    }

    @Test
    void shouldHandleAltBaroGroundValue() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/sample-v2-response.json")) {
            V2Response response = mapper.readValue(is, V2Response.class);
            AcItem groundItem = response.getAc().get(1);

            assertEquals("AE5678", groundItem.getHex());
            assertTrue(groundItem.isOnGround());
            assertNull(groundItem.getAltBaroFeet());
            assertEquals("ground", groundItem.getAltBaro());
        }
    }

    @Test
    void shouldTrimFlightCallsign() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/sample-v2-response.json")) {
            V2Response response = mapper.readValue(is, V2Response.class);
            AcItem item = response.getAc().get(0);

            assertEquals("RCH405", item.getFlight());
        }
    }

    @Test
    void shouldHandleMissingFields() throws Exception {
        String json = "{\"hex\":\"AB1234\",\"lat\":40.0,\"lon\":-74.0}";
        AcItem item = mapper.readValue(json, AcItem.class);

        assertEquals("AB1234", item.getHex());
        assertNull(item.getFlight());
        assertNull(item.getRegistration());
        assertNull(item.getAircraftType());
        assertNull(item.getAltBaro());
        assertNull(item.getAltBaroFeet());
        assertFalse(item.isOnGround());
    }
}

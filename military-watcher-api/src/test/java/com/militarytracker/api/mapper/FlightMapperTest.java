package com.militarytracker.api.mapper;

import com.militarytracker.model.dto.FlightSummaryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightMapperTest {

    @Mock
    private ResultSet rs;

    @Test
    void mapRow_allFieldsPresent_populatesDto() throws Exception {
        Instant now = Instant.parse("2026-01-15T12:00:00Z");

        when(rs.getLong("id")).thenReturn(42L);
        when(rs.getString("hex_icao")).thenReturn("AE1234");
        when(rs.getString("aircraft_type")).thenReturn("C17");
        when(rs.getString("flight")).thenReturn("RCH501");

        when(rs.getInt("alt_baro")).thenReturn(35000);
        when(rs.wasNull()).thenReturn(false)   // after alt_baro
                          .thenReturn(false)   // after ground_speed
                          .thenReturn(false)   // after track
                          .thenReturn(false)   // after lat
                          .thenReturn(false)   // after lon
                          .thenReturn(false);  // after alt_geom

        when(rs.getDouble("ground_speed")).thenReturn(450.5);
        when(rs.getDouble("track")).thenReturn(270.0);
        when(rs.getDouble("lat")).thenReturn(38.8977);
        when(rs.getDouble("lon")).thenReturn(-77.0365);
        when(rs.getDouble("alt_geom")).thenReturn(35200.0);
        when(rs.getBoolean("on_ground")).thenReturn(false);
        when(rs.getTimestamp("seen_at")).thenReturn(Timestamp.from(now));

        FlightSummaryDto dto = FlightMapper.mapRow(rs);

        assertNotNull(dto);
        assertEquals(42L, dto.getId());
        assertEquals("AE1234", dto.getHexIcao());
        assertEquals("C17", dto.getAircraftType());
        assertEquals("RCH501", dto.getFlight());
        assertEquals(35000, dto.getAltBaro());
        assertEquals(450.5, dto.getGroundSpeed());
        assertEquals(270.0, dto.getTrack());
        assertEquals(38.8977, dto.getLat());
        assertEquals(-77.0365, dto.getLon());
        assertEquals(35200, dto.getAltGeom());
        assertFalse(dto.isOnGround());
        assertEquals("2026-01-15T12:00:00Z", dto.getSeenAt());
    }

    @Test
    void mapRow_nullableFieldsNull_setsNulls() throws Exception {
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("hex_icao")).thenReturn("AB0001");
        when(rs.getString("aircraft_type")).thenReturn(null);
        when(rs.getString("flight")).thenReturn(null);

        when(rs.getInt("alt_baro")).thenReturn(0);
        when(rs.wasNull()).thenReturn(true)    // after alt_baro
                          .thenReturn(true)    // after ground_speed
                          .thenReturn(true)    // after track
                          .thenReturn(true)    // after lat
                          .thenReturn(true)    // after lon
                          .thenReturn(true);   // after alt_geom

        when(rs.getDouble("ground_speed")).thenReturn(0.0);
        when(rs.getDouble("track")).thenReturn(0.0);
        when(rs.getDouble("lat")).thenReturn(0.0);
        when(rs.getDouble("lon")).thenReturn(0.0);
        when(rs.getDouble("alt_geom")).thenReturn(0.0);
        when(rs.getBoolean("on_ground")).thenReturn(true);
        when(rs.getTimestamp("seen_at")).thenReturn(null);

        FlightSummaryDto dto = FlightMapper.mapRow(rs);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("AB0001", dto.getHexIcao());
        assertNull(dto.getAircraftType());
        assertNull(dto.getFlight());
        assertNull(dto.getAltBaro());
        assertNull(dto.getGroundSpeed());
        assertNull(dto.getTrack());
        assertNull(dto.getLat());
        assertNull(dto.getLon());
        assertNull(dto.getAltGeom());
        assertNull(dto.getSeenAt());
    }
}

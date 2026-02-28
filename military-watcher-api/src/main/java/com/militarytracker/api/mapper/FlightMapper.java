package com.militarytracker.api.mapper;

import com.militarytracker.model.dto.FlightDetailDto;
import com.militarytracker.model.dto.FlightSummaryDto;
import com.militarytracker.model.dto.TrackPointDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class FlightMapper {

    private FlightMapper() {
    }

    public static FlightSummaryDto mapRow(ResultSet rs) throws SQLException {
        FlightSummaryDto dto = new FlightSummaryDto();
        dto.setId(rs.getLong("id"));
        dto.setHexIcao(rs.getString("hex_icao"));
        dto.setAircraftType(rs.getString("aircraft_type"));
        dto.setFlight(rs.getString("flight"));

        int altBaro = rs.getInt("alt_baro");
        dto.setAltBaro(rs.wasNull() ? null : altBaro);

        double groundSpeed = rs.getDouble("ground_speed");
        dto.setGroundSpeed(rs.wasNull() ? null : groundSpeed);

        double track = rs.getDouble("track");
        dto.setTrack(rs.wasNull() ? null : track);

        double lat = rs.getDouble("lat");
        dto.setLat(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("lon");
        dto.setLon(rs.wasNull() ? null : lon);

        double altGeom = rs.getDouble("alt_geom");
        dto.setAltGeom(rs.wasNull() ? null : (int) altGeom);

        dto.setOnGround(rs.getBoolean("on_ground"));

        Timestamp seenAt = rs.getTimestamp("seen_at");
        dto.setSeenAt(seenAt != null ? seenAt.toInstant().toString() : null);

        return dto;
    }

    public static FlightDetailDto mapDetailRow(ResultSet rs) throws SQLException {
        FlightDetailDto dto = new FlightDetailDto();
        dto.setId(rs.getLong("id"));
        dto.setHexIcao(rs.getString("hex_icao"));
        dto.setRegistration(rs.getString("registration"));
        dto.setAircraftType(rs.getString("aircraft_type"));
        dto.setDescription(rs.getString("description"));
        dto.setOperator(rs.getString("operator"));
        dto.setCountry(rs.getString("country"));
        dto.setFlight(rs.getString("flight"));

        int altBaro = rs.getInt("alt_baro");
        dto.setAltBaro(rs.wasNull() ? null : altBaro);

        double altGeom = rs.getDouble("alt_geom");
        dto.setAltGeom(rs.wasNull() ? null : (int) altGeom);

        double groundSpeed = rs.getDouble("ground_speed");
        dto.setGroundSpeed(rs.wasNull() ? null : groundSpeed);

        double track = rs.getDouble("track");
        dto.setTrack(rs.wasNull() ? null : track);

        int verticalRate = rs.getInt("vertical_rate");
        dto.setVerticalRate(rs.wasNull() ? null : verticalRate);

        dto.setSquawk(rs.getString("squawk"));
        dto.setCategory(rs.getString("category"));

        double lat = rs.getDouble("lat");
        dto.setLat(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("lon");
        dto.setLon(rs.wasNull() ? null : lon);

        dto.setOnGround(rs.getBoolean("on_ground"));

        Timestamp seenAt = rs.getTimestamp("seen_at");
        dto.setSeenAt(seenAt != null ? seenAt.toInstant().toString() : null);

        return dto;
    }

    public static TrackPointDto mapTrackPoint(ResultSet rs) throws SQLException {
        TrackPointDto dto = new TrackPointDto();
        dto.setId(rs.getLong("id"));
        dto.setLat(rs.getDouble("lat"));
        dto.setLon(rs.getDouble("lon"));

        int altBaro = rs.getInt("alt_baro");
        dto.setAltBaro(rs.wasNull() ? null : altBaro);

        double groundSpeed = rs.getDouble("ground_speed");
        dto.setGroundSpeed(rs.wasNull() ? null : groundSpeed);

        double track = rs.getDouble("track");
        dto.setTrack(rs.wasNull() ? null : track);

        Timestamp seenAt = rs.getTimestamp("seen_at");
        dto.setSeenAt(seenAt != null ? seenAt.toInstant().toString() : null);

        return dto;
    }
}

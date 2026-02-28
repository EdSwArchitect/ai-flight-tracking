package com.militarytracker.api.repository;

import com.militarytracker.api.mapper.FlightMapper;
import com.militarytracker.model.dto.FlightSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FlightReadRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FlightReadRepository.class);

    private static final String LIST_FLIGHTS_SQL =
            "SELECT a.hex_icao, a.aircraft_type, fp.flight, fp.alt_baro, fp.ground_speed, " +
            "fp.track, ST_Y(fp.position) AS lat, ST_X(fp.position) AS lon, " +
            "ST_Z(fp.position) AS alt_geom, fp.on_ground, fp.seen_at, fp.id " +
            "FROM flight_positions fp " +
            "JOIN aircraft a ON fp.hex_icao = a.hex_icao " +
            "ORDER BY fp.seen_at DESC " +
            "LIMIT ? OFFSET ?";

    private static final String GET_FLIGHT_BY_ID_SQL =
            "SELECT a.hex_icao, a.aircraft_type, fp.flight, fp.alt_baro, fp.ground_speed, " +
            "fp.track, ST_Y(fp.position) AS lat, ST_X(fp.position) AS lon, " +
            "ST_Z(fp.position) AS alt_geom, fp.on_ground, fp.seen_at, fp.id " +
            "FROM flight_positions fp " +
            "JOIN aircraft a ON fp.hex_icao = a.hex_icao " +
            "WHERE fp.id = ?";

    private static final String FIND_WITHIN_BOUNDING_BOX_SQL =
            "SELECT a.hex_icao, a.aircraft_type, fp.flight, fp.alt_baro, fp.ground_speed, " +
            "fp.track, ST_Y(fp.position) AS lat, ST_X(fp.position) AS lon, " +
            "ST_Z(fp.position) AS alt_geom, fp.on_ground, fp.seen_at, fp.id " +
            "FROM flight_positions fp " +
            "JOIN aircraft a ON fp.hex_icao = a.hex_icao " +
            "WHERE fp.position && ST_MakeEnvelope(?, ?, ?, ?, 4326) " +
            "AND fp.seen_at > NOW() - INTERVAL '1 hour'";

    private final DataSource dataSource;

    public FlightReadRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FlightSummaryDto> listFlights(int limit, int offset) throws SQLException {
        List<FlightSummaryDto> flights = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(LIST_FLIGHTS_SQL)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flights.add(FlightMapper.mapRow(rs));
                }
            }
        }
        LOG.debug("Listed {} flights (limit={}, offset={})", flights.size(), limit, offset);
        return flights;
    }

    public FlightSummaryDto getFlightById(long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_FLIGHT_BY_ID_SQL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return FlightMapper.mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<FlightSummaryDto> findWithinBoundingBox(double north, double south,
                                                         double east, double west) throws SQLException {
        List<FlightSummaryDto> flights = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_WITHIN_BOUNDING_BOX_SQL)) {
            ps.setDouble(1, west);
            ps.setDouble(2, south);
            ps.setDouble(3, east);
            ps.setDouble(4, north);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flights.add(FlightMapper.mapRow(rs));
                }
            }
        }
        LOG.debug("Found {} flights within bounding box [N={}, S={}, E={}, W={}]",
                flights.size(), north, south, east, west);
        return flights;
    }
}

package com.militarytracker.dbingestor.repository;

import com.militarytracker.model.api.AcItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class FlightPositionRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FlightPositionRepository.class);

    private static final String INSERT_SQL = """
            INSERT INTO flight_positions
                (aircraft_id, flight, position, alt_baro, alt_geom, ground_speed, track,
                 vertical_rate, squawk, category, on_ground, seen_at)
            VALUES
                (?, ?, ST_SetSRID(ST_MakePoint(?, ?, ?), 4326), ?, ?, ?, ?,
                 ?, ?, ?, ?, NOW())
            """;

    public void insertPosition(long aircraftId, AcItem item, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setLong(1, aircraftId);
            ps.setString(2, item.getFlight());

            // PostGIS: ST_MakePoint(lon, lat, alt)
            ps.setDouble(3, item.getLon());
            ps.setDouble(4, item.getLat());
            ps.setInt(5, item.getAltGeom() != null ? item.getAltGeom() : 0);

            if (item.getAltBaroFeet() != null) {
                ps.setInt(6, item.getAltBaroFeet());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            if (item.getAltGeom() != null) {
                ps.setInt(7, item.getAltGeom());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            if (item.getGroundSpeed() != null) {
                ps.setDouble(8, item.getGroundSpeed());
            } else {
                ps.setNull(8, Types.REAL);
            }

            if (item.getTrack() != null) {
                ps.setDouble(9, item.getTrack());
            } else {
                ps.setNull(9, Types.REAL);
            }

            if (item.getVerticalRate() != null) {
                ps.setInt(10, item.getVerticalRate());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            ps.setString(11, item.getSquawk());
            ps.setString(12, item.getCategory());
            ps.setBoolean(13, item.isOnGround());

            ps.executeUpdate();
        }
    }
}

package com.militarytracker.dbingestor.repository;

import com.militarytracker.model.api.AcItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FlightTrackRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FlightTrackRepository.class);

    private static final String FIND_ACTIVE_TRACK_SQL = """
            SELECT id FROM flight_tracks
            WHERE aircraft_id = ? AND flight = ? AND end_time IS NULL
            ORDER BY start_time DESC LIMIT 1
            """;

    private static final String CREATE_TRACK_SQL = """
            INSERT INTO flight_tracks (aircraft_id, flight, track_line, start_time, point_count)
            VALUES (?, ?, ST_SetSRID(ST_MakeLine(ARRAY[ST_MakePoint(?, ?, ?)]), 4326), NOW(), 1)
            """;

    private static final String APPEND_TO_TRACK_SQL = """
            UPDATE flight_tracks
            SET track_line = ST_AddPoint(track_line, ST_MakePoint(?, ?, ?)),
                point_count = point_count + 1,
                end_time = NOW(),
                updated_at = NOW()
            WHERE id = ?
            """;

    public void updateOrCreateTrack(long aircraftId, AcItem item, Connection conn) throws Exception {
        String flight = item.getFlight();
        if (flight == null) {
            return;
        }

        Long trackId = findActiveTrack(aircraftId, flight, conn);

        if (trackId != null) {
            appendToTrack(trackId, item, conn);
        } else {
            createTrack(aircraftId, item, conn);
        }
    }

    private Long findActiveTrack(long aircraftId, String flight, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(FIND_ACTIVE_TRACK_SQL)) {
            ps.setLong(1, aircraftId);
            ps.setString(2, flight);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        }
    }

    private void createTrack(long aircraftId, AcItem item, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(CREATE_TRACK_SQL)) {
            ps.setLong(1, aircraftId);
            ps.setString(2, item.getFlight());
            ps.setDouble(3, item.getLon());
            ps.setDouble(4, item.getLat());
            ps.setInt(5, item.getAltGeom() != null ? item.getAltGeom() : 0);
            ps.executeUpdate();
        }
    }

    private void appendToTrack(long trackId, AcItem item, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(APPEND_TO_TRACK_SQL)) {
            ps.setDouble(1, item.getLon());
            ps.setDouble(2, item.getLat());
            ps.setInt(3, item.getAltGeom() != null ? item.getAltGeom() : 0);
            ps.setLong(4, trackId);
            ps.executeUpdate();
        }
    }
}

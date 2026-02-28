package com.militarytracker.dbingestor.repository;

import com.militarytracker.model.api.AcItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AircraftRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AircraftRepository.class);

    private static final String UPSERT_SQL = """
            INSERT INTO aircraft (hex_icao, registration, aircraft_type, description, operator)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (hex_icao) DO UPDATE SET
                registration = COALESCE(EXCLUDED.registration, aircraft.registration),
                aircraft_type = COALESCE(EXCLUDED.aircraft_type, aircraft.aircraft_type),
                description = COALESCE(EXCLUDED.description, aircraft.description),
                operator = COALESCE(EXCLUDED.operator, aircraft.operator),
                updated_at = NOW()
            RETURNING id
            """;

    private final DataSource dataSource;

    public AircraftRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long upsertAircraft(AcItem item, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {
            ps.setString(1, item.getHex());
            ps.setString(2, item.getRegistration());
            ps.setString(3, item.getAircraftType());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getOperator());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new IllegalStateException("UPSERT did not return an id for hex=" + item.getHex());
            }
        }
    }
}

package com.militarytracker.dbingestor.service;

import com.militarytracker.dbingestor.metrics.IngestorMetrics;
import com.militarytracker.dbingestor.repository.AircraftRepository;
import com.militarytracker.dbingestor.repository.FlightPositionRepository;
import com.militarytracker.dbingestor.repository.FlightTrackRepository;
import com.militarytracker.model.api.AcItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

public class IngestionService {

    private static final Logger LOG = LoggerFactory.getLogger(IngestionService.class);

    private final DataSource dataSource;
    private final AircraftRepository aircraftRepo;
    private final FlightPositionRepository positionRepo;
    private final FlightTrackRepository trackRepo;

    public IngestionService(DataSource dataSource,
                            AircraftRepository aircraftRepo,
                            FlightPositionRepository positionRepo,
                            FlightTrackRepository trackRepo) {
        this.dataSource = dataSource;
        this.aircraftRepo = aircraftRepo;
        this.positionRepo = positionRepo;
        this.trackRepo = trackRepo;
    }

    public void ingest(AcItem item) {
        if (item.getLat() == null || item.getLon() == null) {
            LOG.debug("Skipping item hex={} with no position data", item.getHex());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long aircraftId = aircraftRepo.upsertAircraft(item, conn);
                positionRepo.insertPosition(aircraftId, item, conn);
                trackRepo.updateOrCreateTrack(aircraftId, item, conn);
                conn.commit();

                IngestorMetrics.RECORDS_INGESTED.inc();
                IngestorMetrics.TOTAL_RECORDS.inc();
                IngestorMetrics.RATE_OF_RECORDS.inc();

                LOG.trace("Ingested record for hex={}", item.getHex());
            } catch (Exception e) {
                conn.rollback();
                IngestorMetrics.RECORDS_FAILED.inc();
                LOG.error("Failed to ingest record for hex={}: {}", item.getHex(), e.getMessage());
            }
        } catch (Exception e) {
            IngestorMetrics.RECORDS_FAILED.inc();
            LOG.error("Database connection error for hex={}: {}", item.getHex(), e.getMessage());
        }
    }
}

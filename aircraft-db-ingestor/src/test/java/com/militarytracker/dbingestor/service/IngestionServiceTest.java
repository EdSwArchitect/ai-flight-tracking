package com.militarytracker.dbingestor.service;

import com.militarytracker.dbingestor.repository.AircraftRepository;
import com.militarytracker.dbingestor.repository.FlightPositionRepository;
import com.militarytracker.dbingestor.repository.FlightTrackRepository;
import com.militarytracker.model.api.AcItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private AircraftRepository aircraftRepo;
    @Mock
    private FlightPositionRepository positionRepo;
    @Mock
    private FlightTrackRepository trackRepo;

    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new IngestionService(dataSource, aircraftRepo, positionRepo, trackRepo);
    }

    @Test
    void shouldIngestValidItem() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        AcItem item = createTestItem("AE1234", 38.8951, -77.0364);
        when(aircraftRepo.upsertAircraft(any(), any())).thenReturn(42L);

        ingestionService.ingest(item);

        verify(aircraftRepo).upsertAircraft(eq(item), eq(connection));
        verify(positionRepo).insertPosition(eq(42L), eq(item), eq(connection));
        verify(trackRepo).updateOrCreateTrack(eq(42L), eq(item), eq(connection));
        verify(connection).commit();
    }

    @Test
    void shouldSkipItemWithoutLatitude() throws Exception {
        AcItem item = new AcItem();
        item.setHex("AE1234");
        item.setLon(-77.0);

        ingestionService.ingest(item);

        verify(dataSource, never()).getConnection();
        verify(aircraftRepo, never()).upsertAircraft(any(), any());
    }

    @Test
    void shouldSkipItemWithoutLongitude() throws Exception {
        AcItem item = new AcItem();
        item.setHex("AE1234");
        item.setLat(38.0);

        ingestionService.ingest(item);

        verify(dataSource, never()).getConnection();
    }

    @Test
    void shouldRollbackOnError() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        AcItem item = createTestItem("AE1234", 38.0, -77.0);
        when(aircraftRepo.upsertAircraft(any(), any())).thenThrow(new RuntimeException("DB error"));

        ingestionService.ingest(item);

        verify(connection).rollback();
    }

    private AcItem createTestItem(String hex, double lat, double lon) {
        AcItem item = new AcItem();
        item.setHex(hex);
        item.setLat(lat);
        item.setLon(lon);
        item.setAltGeom(35000);
        item.setFlight("RCH405");
        return item;
    }
}

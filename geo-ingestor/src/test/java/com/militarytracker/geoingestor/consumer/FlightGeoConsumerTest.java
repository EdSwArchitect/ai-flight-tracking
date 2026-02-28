package com.militarytracker.geoingestor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militarytracker.geoingestor.converter.GeoJsonConverter;
import com.militarytracker.geoingestor.converter.WktConverter;
import com.militarytracker.geoingestor.indexer.OpenSearchIndexer;
import com.militarytracker.geoingestor.metrics.GeoMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightGeoConsumerTest {

    private ObjectMapper objectMapper;
    private GeoJsonConverter geoJsonConverter;

    @Mock
    private OpenSearchIndexer openSearchIndexer;

    private GeoMetrics metrics;
    private FlightGeoConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        geoJsonConverter = new GeoJsonConverter(objectMapper);
        WktConverter wktConverter = new WktConverter();
        metrics = new GeoMetrics(new SimpleMeterRegistry());
        consumer = new FlightGeoConsumer(objectMapper, geoJsonConverter, wktConverter, openSearchIndexer, metrics);
    }

    @Test
    void consumeShouldProcessValidMessage() {
        String message = """
                {"hex":"ae1234","lat":38.8977,"lon":-77.0365,"alt_baro":35000,"gs":450.5,"track":180.0,"flight":"VALOR01 ","t":"C17"}
                """;

        when(openSearchIndexer.index(eq("ae1234"), anyString())).thenReturn(true);

        consumer.consume(message);

        verify(openSearchIndexer).index(eq("ae1234"), anyString());
    }

    @Test
    void consumeShouldSkipMessageWithoutCoordinates() {
        String message = """
                {"hex":"ae1234","flight":"VALOR01 ","t":"C17"}
                """;

        consumer.consume(message);

        verify(openSearchIndexer, never()).index(anyString(), anyString());
    }

    @Test
    void consumeShouldHandleIndexingFailure() {
        String message = """
                {"hex":"ae5678","lat":40.0,"lon":-74.0,"alt_baro":25000}
                """;

        when(openSearchIndexer.index(eq("ae5678"), anyString())).thenReturn(false);

        consumer.consume(message);

        verify(openSearchIndexer).index(eq("ae5678"), anyString());
    }

    @Test
    void consumeShouldHandleInvalidJson() {
        consumer.consume("not valid json");

        verify(openSearchIndexer, never()).index(anyString(), anyString());
    }
}

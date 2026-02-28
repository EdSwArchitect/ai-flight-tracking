package com.militarytracker.geoingestor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militarytracker.geoingestor.converter.GeoJsonConverter;
import com.militarytracker.geoingestor.converter.WktConverter;
import com.militarytracker.geoingestor.indexer.OpenSearchIndexer;
import com.militarytracker.geoingestor.metrics.GeoMetrics;
import com.militarytracker.model.api.AcItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FlightGeoConsumer {

    private static final Logger log = LoggerFactory.getLogger(FlightGeoConsumer.class);

    private final ObjectMapper objectMapper;
    private final GeoJsonConverter geoJsonConverter;
    private final WktConverter wktConverter;
    private final OpenSearchIndexer openSearchIndexer;
    private final GeoMetrics metrics;

    public FlightGeoConsumer(ObjectMapper objectMapper,
                             GeoJsonConverter geoJsonConverter,
                             WktConverter wktConverter,
                             OpenSearchIndexer openSearchIndexer,
                             GeoMetrics metrics) {
        this.objectMapper = objectMapper;
        this.geoJsonConverter = geoJsonConverter;
        this.wktConverter = wktConverter;
        this.openSearchIndexer = openSearchIndexer;
        this.metrics = metrics;
    }

    @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        metrics.incrementReadFromKafka();

        try {
            AcItem item = objectMapper.readValue(message, AcItem.class);

            if (item.getLat() == null || item.getLon() == null) {
                log.debug("Skipping item without coordinates: hex={}", item.getHex());
                return;
            }

            String geoJson = geoJsonConverter.convert(item);
            if (geoJson != null) {
                metrics.incrementGeoJsonCreated();
            }

            String wkt = wktConverter.convert(item);
            if (wkt != null) {
                metrics.incrementWktCreated();
            }

            if (geoJson != null) {
                boolean indexed = openSearchIndexer.index(item.getHex(), geoJson);
                if (indexed) {
                    metrics.incrementOpensearchIndexed();
                } else {
                    metrics.incrementOpensearchFailed();
                }
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
            metrics.incrementOpensearchFailed();
        }
    }
}

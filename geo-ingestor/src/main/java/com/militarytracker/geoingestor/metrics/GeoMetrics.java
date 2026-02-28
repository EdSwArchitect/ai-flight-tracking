package com.militarytracker.geoingestor.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Micrometer-based metrics for the Geo Ingestor pipeline.
 */
@Component
public class GeoMetrics {

    private final Counter recordsReadFromKafka;
    private final Counter geoJsonCreated;
    private final Counter wktCreated;
    private final Counter opensearchIndexed;
    private final Counter opensearchFailed;

    public GeoMetrics(MeterRegistry registry) {
        this.recordsReadFromKafka = Counter.builder("geo_records_read_from_kafka")
                .description("Number of records read from Kafka")
                .register(registry);

        this.geoJsonCreated = Counter.builder("geo_records_geojson_created")
                .description("Number of GeoJSON features created")
                .register(registry);

        this.wktCreated = Counter.builder("geo_records_wkt_created")
                .description("Number of WKT strings created")
                .register(registry);

        this.opensearchIndexed = Counter.builder("geo_records_opensearch_indexed")
                .description("Number of records successfully indexed in OpenSearch")
                .register(registry);

        this.opensearchFailed = Counter.builder("geo_records_opensearch_failed")
                .description("Number of records that failed to index in OpenSearch")
                .register(registry);
    }

    public void incrementReadFromKafka() {
        recordsReadFromKafka.increment();
    }

    public void incrementGeoJsonCreated() {
        geoJsonCreated.increment();
    }

    public void incrementWktCreated() {
        wktCreated.increment();
    }

    public void incrementOpensearchIndexed() {
        opensearchIndexed.increment();
    }

    public void incrementOpensearchFailed() {
        opensearchFailed.increment();
    }
}

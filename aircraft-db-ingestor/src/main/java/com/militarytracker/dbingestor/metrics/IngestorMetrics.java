package com.militarytracker.dbingestor.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public final class IngestorMetrics {

    public static final Counter RECORDS_INGESTED = Counter.build()
            .name("number_of_records_ingested")
            .help("Count of records successfully ingested into the database")
            .register();

    public static final Counter RECORDS_FAILED = Counter.build()
            .name("number_of_records_failed_to_ingest")
            .help("Count of records that failed to ingest into the database")
            .register();

    public static final Counter RECORDS_READ_FROM_KAFKA = Counter.build()
            .name("number_of_records_read_from_kafka")
            .help("Count of records read/consumed from Kafka")
            .register();

    public static final Gauge TOTAL_RECORDS = Gauge.build()
            .name("number_of_records")
            .help("Total records written to the database")
            .register();

    public static final Counter RATE_OF_RECORDS = Counter.build()
            .name("rate_of_records")
            .help("Rate of records being ingested over time")
            .register();

    private IngestorMetrics() {
    }
}

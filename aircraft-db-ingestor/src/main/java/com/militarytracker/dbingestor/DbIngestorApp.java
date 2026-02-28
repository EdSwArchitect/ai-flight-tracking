package com.militarytracker.dbingestor;

import com.militarytracker.common.config.AppConfig;
import com.militarytracker.common.jdbc.DataSourceFactory;
import com.militarytracker.common.kafka.KafkaConsumerFactory;
import com.militarytracker.common.lifecycle.HealthCheckServer;
import com.militarytracker.common.lifecycle.ShutdownHook;
import com.militarytracker.common.metrics.MetricsServer;
import com.militarytracker.dbingestor.consumer.FlightKafkaConsumer;
import com.militarytracker.dbingestor.repository.AircraftRepository;
import com.militarytracker.dbingestor.repository.FlightPositionRepository;
import com.militarytracker.dbingestor.repository.FlightTrackRepository;
import com.militarytracker.dbingestor.service.IngestionService;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbIngestorApp {

    private static final Logger LOG = LoggerFactory.getLogger(DbIngestorApp.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Aircraft DB Ingestor...");

        AppConfig config = AppConfig.load();

        MetricsServer metricsServer = new MetricsServer(config.getInt("metrics.port"));
        HealthCheckServer healthServer = new HealthCheckServer(config.getInt("health.port"));

        HikariDataSource dataSource = DataSourceFactory.create(config.getSubConfig("database"));

        AircraftRepository aircraftRepo = new AircraftRepository(dataSource);
        FlightPositionRepository positionRepo = new FlightPositionRepository();
        FlightTrackRepository trackRepo = new FlightTrackRepository();
        IngestionService ingestionService = new IngestionService(dataSource, aircraftRepo, positionRepo, trackRepo);

        KafkaConsumer<String, String> kafkaConsumer = KafkaConsumerFactory.create(config.getSubConfig("kafka"));
        String topic = config.getString("kafka.topic");
        FlightKafkaConsumer flightConsumer = new FlightKafkaConsumer(kafkaConsumer, ingestionService, topic);

        Thread consumerThread = Thread.startVirtualThread(flightConsumer);

        healthServer.setReady(true);
        LOG.info("Aircraft DB Ingestor started. Consuming from topic: {}", topic);

        new ShutdownHook()
                .register(flightConsumer::shutdown)
                .register(dataSource::close)
                .register(metricsServer::stop)
                .register(healthServer::stop)
                .install();
    }
}

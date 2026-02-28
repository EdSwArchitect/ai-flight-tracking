package com.militarytracker.svc;

import com.militarytracker.common.config.AppConfig;
import com.militarytracker.common.kafka.KafkaProducerFactory;
import com.militarytracker.common.lifecycle.HealthCheckServer;
import com.militarytracker.common.lifecycle.ShutdownHook;
import com.militarytracker.common.metrics.MetricsServer;
import com.militarytracker.svc.poller.AdsbApiClient;
import com.militarytracker.svc.poller.AdsbApiPoller;
import com.militarytracker.svc.publisher.FlightKafkaPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MilitaryAircraftSvcApp {

    private static final Logger LOG = LoggerFactory.getLogger(MilitaryAircraftSvcApp.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Military Aircraft SVC...");

        AppConfig config = AppConfig.load();

        MetricsServer metricsServer = new MetricsServer(config.getInt("metrics.port"));
        HealthCheckServer healthServer = new HealthCheckServer(config.getInt("health.port"));

        KafkaProducer<String, String> producer = KafkaProducerFactory.create(config.getSubConfig("kafka"));
        String topic = config.getString("kafka.topic");
        FlightKafkaPublisher publisher = new FlightKafkaPublisher(producer, topic);

        String apiUrl = config.getString("adsb.api-url");
        AdsbApiClient apiClient = new AdsbApiClient(apiUrl);
        AdsbApiPoller poller = new AdsbApiPoller(apiClient, publisher);

        int pollInterval = config.getInt("adsb.poll-interval-seconds");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "adsb-poller");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(poller, 0, pollInterval, TimeUnit.SECONDS);

        healthServer.setReady(true);
        LOG.info("Military Aircraft SVC started. Polling every {}s from {}", pollInterval, apiUrl);

        new ShutdownHook()
                .register(() -> scheduler.shutdown())
                .register(publisher::close)
                .register(metricsServer::stop)
                .register(healthServer::stop)
                .install();
    }
}

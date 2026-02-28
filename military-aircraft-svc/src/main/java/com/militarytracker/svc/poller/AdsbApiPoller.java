package com.militarytracker.svc.poller;

import com.militarytracker.model.api.V2Response;
import com.militarytracker.svc.metrics.SvcMetrics;
import com.militarytracker.svc.publisher.FlightKafkaPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsbApiPoller implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(AdsbApiPoller.class);

    private final AdsbApiClient apiClient;
    private final FlightKafkaPublisher publisher;

    public AdsbApiPoller(AdsbApiClient apiClient, FlightKafkaPublisher publisher) {
        this.apiClient = apiClient;
        this.publisher = publisher;
    }

    @Override
    public void run() {
        try {
            V2Response response = apiClient.fetchMilitaryAircraft();

            int total = response.getTotal();
            SvcMetrics.METRICS_RETRIEVED.set(total);
            SvcMetrics.METRICS_RETRIEVED_TOTAL.inc(total);
            SvcMetrics.CALLS_SUCCESSFUL.inc();

            LOG.info("Fetched {} aircraft records", total);

            if (response.getAc() != null && !response.getAc().isEmpty()) {
                int published = publisher.publish(response.getAc());
                LOG.info("Published {} records to Kafka", published);
            }

        } catch (Exception e) {
            SvcMetrics.CALLS_UNSUCCESSFUL.inc();
            LOG.error("Error polling ADS-B API: {}", e.getMessage(), e);
        }
    }
}

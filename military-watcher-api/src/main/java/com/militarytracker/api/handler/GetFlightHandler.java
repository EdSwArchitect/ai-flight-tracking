package com.militarytracker.api.handler;

import com.militarytracker.api.metrics.ApiMetrics;
import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.FlightSummaryDto;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GetFlightHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GetFlightHandler.class);

    private final FlightReadRepository repository;

    public GetFlightHandler(FlightReadRepository repository) {
        this.repository = repository;
    }

    public void handle(Context ctx) {
        String idParam = ctx.pathParam("id");

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            ApiMetrics.REQUESTS_TOTAL.labels("get_flight", "400").inc();
            ctx.status(400).json(Map.of("error", "Invalid flight id: " + idParam));
            return;
        }

        LOG.debug("Getting flight by id={}", id);

        try {
            FlightSummaryDto flight = repository.getFlightById(id);
            if (flight == null) {
                ApiMetrics.REQUESTS_TOTAL.labels("get_flight", "404").inc();
                ctx.status(404).json(Map.of("error", "Flight not found: " + id));
            } else {
                ApiMetrics.REQUESTS_TOTAL.labels("get_flight", "200").inc();
                ctx.json(flight);
            }
        } catch (Exception e) {
            LOG.error("Error getting flight id={}", id, e);
            ApiMetrics.REQUESTS_TOTAL.labels("get_flight", "500").inc();
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }
}

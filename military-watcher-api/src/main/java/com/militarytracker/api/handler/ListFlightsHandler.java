package com.militarytracker.api.handler;

import com.militarytracker.api.metrics.ApiMetrics;
import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.FlightSummaryDto;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListFlightsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ListFlightsHandler.class);
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_OFFSET = 0;

    private final FlightReadRepository repository;

    public ListFlightsHandler(FlightReadRepository repository) {
        this.repository = repository;
    }

    public void handle(Context ctx) {
        int limit = parseIntOrDefault(ctx.queryParam("limit"), DEFAULT_LIMIT);
        int offset = parseIntOrDefault(ctx.queryParam("offset"), DEFAULT_OFFSET);

        LOG.debug("Listing flights with limit={}, offset={}", limit, offset);

        try {
            List<FlightSummaryDto> flights = repository.listFlights(limit, offset);
            ApiMetrics.REQUESTS_TOTAL.labels("list_flights", "200").inc();
            ctx.json(flights);
        } catch (Exception e) {
            LOG.error("Error listing flights", e);
            ApiMetrics.REQUESTS_TOTAL.labels("list_flights", "500").inc();
            ctx.status(500).json(java.util.Map.of("error", "Internal server error"));
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

package com.militarytracker.api.handler;

import com.militarytracker.api.metrics.ApiMetrics;
import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.FlightSummaryDto;
import com.militarytracker.model.dto.GeoBoxRequest;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GeoBoxFlightHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GeoBoxFlightHandler.class);

    private final FlightReadRepository repository;

    public GeoBoxFlightHandler(FlightReadRepository repository) {
        this.repository = repository;
    }

    public void handle(Context ctx) {
        GeoBoxRequest request;
        try {
            request = ctx.bodyAsClass(GeoBoxRequest.class);
        } catch (Exception e) {
            ApiMetrics.REQUESTS_TOTAL.labels("geobox_flights", "400").inc();
            ctx.status(400).json(Map.of("error", "Invalid request body"));
            return;
        }

        if (!request.isValid()) {
            ApiMetrics.REQUESTS_TOTAL.labels("geobox_flights", "400").inc();
            ctx.status(400).json(Map.of("error", "Invalid bounding box coordinates"));
            return;
        }

        LOG.debug("GeoBox query: north={}, south={}, east={}, west={}",
                request.getNorth(), request.getSouth(), request.getEast(), request.getWest());

        try {
            List<FlightSummaryDto> flights = repository.findWithinBoundingBox(
                    request.getNorth(), request.getSouth(),
                    request.getEast(), request.getWest());
            ApiMetrics.REQUESTS_TOTAL.labels("geobox_flights", "200").inc();
            ctx.json(flights);
        } catch (Exception e) {
            LOG.error("Error querying geobox flights", e);
            ApiMetrics.REQUESTS_TOTAL.labels("geobox_flights", "500").inc();
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }
}

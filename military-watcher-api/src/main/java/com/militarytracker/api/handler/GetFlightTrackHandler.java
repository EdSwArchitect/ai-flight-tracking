package com.militarytracker.api.handler;

import com.militarytracker.api.metrics.ApiMetrics;
import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.TrackPointDto;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GetFlightTrackHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GetFlightTrackHandler.class);

    private final FlightReadRepository repository;

    public GetFlightTrackHandler(FlightReadRepository repository) {
        this.repository = repository;
    }

    public void handle(Context ctx) {
        String idParam = ctx.pathParam("id");

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            ApiMetrics.REQUESTS_TOTAL.labels("get_flight_track", "400").inc();
            ctx.status(400).json(Map.of("error", "Invalid flight id: " + idParam));
            return;
        }

        LOG.debug("Getting flight track for position id={}", id);

        try {
            List<TrackPointDto> track = repository.getFlightTrack(id);
            if (track.isEmpty()) {
                ApiMetrics.REQUESTS_TOTAL.labels("get_flight_track", "404").inc();
                ctx.status(404).json(Map.of("error", "No track found for flight: " + id));
            } else {
                ApiMetrics.REQUESTS_TOTAL.labels("get_flight_track", "200").inc();
                ctx.json(track);
            }
        } catch (Exception e) {
            LOG.error("Error getting flight track for id={}", id, e);
            ApiMetrics.REQUESTS_TOTAL.labels("get_flight_track", "500").inc();
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }
}

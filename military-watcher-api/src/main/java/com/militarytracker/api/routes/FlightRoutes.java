package com.militarytracker.api.routes;

import com.militarytracker.api.handler.GeoBoxFlightHandler;
import com.militarytracker.api.handler.GetFlightHandler;
import com.militarytracker.api.handler.GetFlightTrackHandler;
import com.militarytracker.api.handler.ListFlightsHandler;
import io.javalin.Javalin;

public final class FlightRoutes {

    private FlightRoutes() {
    }

    public static void register(Javalin app,
                                ListFlightsHandler listFlightsHandler,
                                GetFlightHandler getFlightHandler,
                                GeoBoxFlightHandler geoBoxFlightHandler,
                                GetFlightTrackHandler getFlightTrackHandler) {
        app.get("/list-flights", listFlightsHandler::handle);
        app.get("/list-flight/{id}", getFlightHandler::handle);
        app.get("/flight-track/{id}", getFlightTrackHandler::handle);
        app.post("/geobox-list-flight", geoBoxFlightHandler::handle);
    }
}

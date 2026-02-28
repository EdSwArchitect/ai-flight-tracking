package com.militarytracker.api.routes;

import io.javalin.Javalin;

import java.util.Map;

public final class HealthRoutes {

    private HealthRoutes() {
    }

    public static void register(Javalin app) {
        app.get("/health", ctx -> ctx.json(Map.of("status", "UP")));
        app.get("/ready", ctx -> ctx.json(Map.of("status", "READY")));
    }
}

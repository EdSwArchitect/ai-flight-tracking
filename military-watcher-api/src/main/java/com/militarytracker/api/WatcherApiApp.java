package com.militarytracker.api;

import com.militarytracker.api.handler.GeoBoxFlightHandler;
import com.militarytracker.api.handler.GetFlightHandler;
import com.militarytracker.api.handler.GetFlightTrackHandler;
import com.militarytracker.api.handler.ListFlightsHandler;
import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.api.routes.FlightRoutes;
import com.militarytracker.api.routes.HealthRoutes;
import com.militarytracker.common.config.AppConfig;
import com.militarytracker.common.json.JsonMapper;
import com.militarytracker.common.jdbc.DataSourceFactory;
import com.militarytracker.common.lifecycle.ShutdownHook;
import com.militarytracker.common.metrics.MetricsServer;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatcherApiApp {

    private static final Logger LOG = LoggerFactory.getLogger(WatcherApiApp.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Military Watcher API...");

        AppConfig config = AppConfig.load();

        MetricsServer metricsServer = new MetricsServer(config.getInt("metrics.port"));

        HikariDataSource dataSource = DataSourceFactory.create(config.getSubConfig("database"));

        FlightReadRepository flightRepo = new FlightReadRepository(dataSource);

        ListFlightsHandler listFlightsHandler = new ListFlightsHandler(flightRepo);
        GetFlightHandler getFlightHandler = new GetFlightHandler(flightRepo);
        GeoBoxFlightHandler geoBoxFlightHandler = new GeoBoxFlightHandler(flightRepo);
        GetFlightTrackHandler getFlightTrackHandler = new GetFlightTrackHandler(flightRepo);

        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.jsonMapper(new JavalinJackson(JsonMapper.get(), false));
            javalinConfig.http.defaultContentType = "application/json";
            javalinConfig.bundledPlugins.enableCors(cors ->
                    cors.addRule(rule -> rule.anyHost())
            );
        });

        FlightRoutes.register(app, listFlightsHandler, getFlightHandler, geoBoxFlightHandler, getFlightTrackHandler);
        HealthRoutes.register(app);

        int port = config.getInt("server.port");
        app.start(port);
        LOG.info("Military Watcher API started on port {}", port);

        new ShutdownHook()
                .register(app::stop)
                .register(dataSource::close)
                .register(metricsServer::stop)
                .install();
    }
}

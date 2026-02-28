package com.militarytracker.common.lifecycle;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HealthCheckServer {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServer.class);

    private final HttpServer server;
    private final AtomicBoolean ready = new AtomicBoolean(false);

    public HealthCheckServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", exchange -> {
            byte[] response = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.createContext("/ready", exchange -> {
            if (ready.get()) {
                byte[] response = "{\"status\":\"READY\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } else {
                byte[] response = "{\"status\":\"NOT_READY\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(503, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });

        server.setExecutor(null);
        server.start();
        LOG.info("Health check server started on port {}", port);
    }

    public void setReady(boolean isReady) {
        ready.set(isReady);
    }

    public void stop() {
        server.stop(0);
        LOG.info("Health check server stopped");
    }
}

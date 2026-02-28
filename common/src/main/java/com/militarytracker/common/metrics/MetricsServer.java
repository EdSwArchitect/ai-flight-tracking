package com.militarytracker.common.metrics;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class MetricsServer {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsServer.class);

    private final HTTPServer server;

    public MetricsServer(int port) throws IOException {
        DefaultExports.initialize();
        this.server = new HTTPServer(port);
        LOG.info("Prometheus metrics server started on port {}", port);
    }

    public void stop() {
        server.close();
        LOG.info("Prometheus metrics server stopped");
    }
}

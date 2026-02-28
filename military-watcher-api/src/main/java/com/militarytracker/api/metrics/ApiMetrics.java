package com.militarytracker.api.metrics;

import io.prometheus.client.Counter;

public final class ApiMetrics {

    public static final Counter REQUESTS_TOTAL = Counter.build()
            .name("api_requests_total")
            .help("Total number of API requests")
            .labelNames("endpoint", "status")
            .register();

    public static final Counter REQUESTS_ERRORS = Counter.build()
            .name("api_requests_errors_total")
            .help("Total number of API request errors")
            .labelNames("endpoint")
            .register();

    private ApiMetrics() {
    }
}

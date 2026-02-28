package com.militarytracker.svc.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public final class SvcMetrics {

    public static final Counter CALLS_SUCCESSFUL = Counter.build()
            .name("number_of_calls_successful")
            .help("Count of successful API calls")
            .register();

    public static final Counter CALLS_UNSUCCESSFUL = Counter.build()
            .name("number_of_calls_unsuccessful")
            .help("Count of failed API calls")
            .register();

    public static final Gauge METRICS_RETRIEVED = Gauge.build()
            .name("number_of_metrics_retrieved")
            .help("Total aircraft records retrieved in last poll")
            .register();

    public static final Counter METRICS_RETRIEVED_TOTAL = Counter.build()
            .name("metrics_retrieved_rate")
            .help("Rate of aircraft records retrieved over time")
            .register();

    private SvcMetrics() {
    }
}

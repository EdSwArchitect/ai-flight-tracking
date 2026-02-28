package com.militarytracker.common.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class ShutdownHook {

    private static final Logger LOG = LoggerFactory.getLogger(ShutdownHook.class);

    private final List<AutoCloseable> resources = new ArrayList<>();

    public ShutdownHook register(AutoCloseable resource) {
        resources.add(resource);
        return this;
    }

    public void install() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook triggered, closing {} resources...", resources.size());
            for (int i = resources.size() - 1; i >= 0; i--) {
                try {
                    resources.get(i).close();
                } catch (Exception e) {
                    LOG.warn("Error closing resource: {}", e.getMessage(), e);
                }
            }
            LOG.info("Shutdown complete");
        }, "shutdown-hook"));
    }
}

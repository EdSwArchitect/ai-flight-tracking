package com.militarytracker.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public final class AppConfig {

    private final Config config;

    private AppConfig(Config config) {
        this.config = config;
    }

    public static AppConfig load() {
        Config config = ConfigFactory.load();
        return new AppConfig(config);
    }

    public static AppConfig load(String resourceBasename) {
        Config config = ConfigFactory.load(resourceBasename);
        return new AppConfig(config);
    }

    public Config getConfig() {
        return config;
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean hasPath(String path) {
        return config.hasPath(path);
    }

    public Config getSubConfig(String path) {
        return config.getConfig(path);
    }
}

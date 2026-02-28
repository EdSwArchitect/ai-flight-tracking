package com.militarytracker.common.jdbc;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static HikariDataSource create(Config dbConfig) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(dbConfig.getString("url"));
        hikari.setUsername(dbConfig.getString("username"));
        hikari.setPassword(dbConfig.getString("password"));
        hikari.setMaximumPoolSize(dbConfig.getInt("pool.max-size"));
        hikari.setMinimumIdle(dbConfig.getInt("pool.min-idle"));
        hikari.setConnectionTimeout(30000);
        hikari.setIdleTimeout(600000);
        hikari.setMaxLifetime(1800000);

        if (dbConfig.hasPath("ssl.enabled") && dbConfig.getBoolean("ssl.enabled")) {
            hikari.addDataSourceProperty("ssl", "true");
            hikari.addDataSourceProperty("sslmode", dbConfig.getString("ssl.mode"));
            if (dbConfig.hasPath("ssl.root-cert")) {
                hikari.addDataSourceProperty("sslrootcert", dbConfig.getString("ssl.root-cert"));
            }
        }

        return new HikariDataSource(hikari);
    }
}

# Common

Shared infrastructure utilities and cross-cutting concerns for all Plain Java backend services.

## Contents

| Class | Description |
|---|---|
| `AppConfig` | HOCON configuration loader (Typesafe Config) |
| `KafkaProducerFactory` | Creates Kafka producers with optional SSL/TLS |
| `KafkaConsumerFactory` | Creates Kafka consumers with optional SSL/TLS |
| `DataSourceFactory` | Creates HikariCP datasources with optional SSL |
| `MetricsServer` | Prometheus metrics HTTP server (port 9090) |
| `HealthCheckServer` | Health/readiness HTTP server (port 8081) |
| `ShutdownHook` | Graceful shutdown orchestration |
| `JsonMapper` | Jackson ObjectMapper singleton |
| `TlsContextFactory` | SSL/TLS context creation for Kafka and databases |

## Configuration

Default configuration is provided in `reference.conf` (HOCON format) with environment variable overrides:

| Env Variable | Description |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses |
| `KAFKA_SSL_ENABLED` | Enable Kafka SSL (`true`/`false`) |
| `DATABASE_URL` | JDBC connection URL |
| `DATABASE_USERNAME` | DB username |
| `DATABASE_PASSWORD` | DB password |
| `DATABASE_SSL_ENABLED` | Enable DB SSL (`true`/`false`) |

## Usage

```xml
<dependency>
    <groupId>com.militarytracker</groupId>
    <artifactId>military-tracker-common</artifactId>
</dependency>
```

## Build

```bash
mvn compile -pl common -am
```

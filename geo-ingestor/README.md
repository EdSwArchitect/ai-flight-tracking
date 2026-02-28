# Geo Ingestor

Spring Boot Kafka consumer that converts aircraft data to GeoJSON and indexes it in OpenSearch.

## How It Works

1. Consumes messages from the `military_flights` Kafka topic (consumer group: `geo-ingestor-group`)
2. Filters out aircraft without valid position data
3. Converts each record to GeoJSON Point features via GeoTools
4. Indexes GeoJSON documents into the `military_flights_geo` OpenSearch index

## Ports

| Port | Purpose |
|---|---|
| 8080 | Spring Boot application |
| 8081 | Spring Boot Actuator (health, metrics, requires `spring-boot-starter-web`) |

## Metrics

| Metric | Description |
|---|---|
| `geo_records_read_from_kafka` | Records consumed from Kafka |
| `geo_records_geojson_created` | GeoJSON features created |
| `geo_records_wkt_created` | WKT geometries created |
| `geo_records_opensearch_indexed` | Documents indexed into OpenSearch |
| `geo_records_opensearch_failed` | Documents that failed to index |

## Configuration

Via environment variables or `application.yml`:

| Variable | Default | Description |
|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka-cluster-kafka-bootstrap:9092` | Kafka brokers |
| `OPENSEARCH_HOST` | `opensearch-cluster` | OpenSearch host |
| `OPENSEARCH_PORT` | `9200` | OpenSearch port |
| `OPENSEARCH_SCHEME` | `http` | `http` or `https` |
| `OPENSEARCH_INDEX` | `military_flights_geo` | Index name |

## Build & Run

```bash
# Build
mvn package -pl geo-ingestor -am -DskipTests

# Run locally
java -jar geo-ingestor/target/geo-ingestor-*.jar

# Docker
docker build -t geo-ingestor:latest -f docker/geo-ingestor/Dockerfile .
```

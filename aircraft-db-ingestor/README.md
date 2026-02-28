# Aircraft DB Ingestor

Kafka consumer that ingests aircraft position data and persists it to PostgreSQL with PostGIS.

## How It Works

1. Consumes messages from the `military_flights` Kafka topic (consumer group: `db-ingestor-group`)
2. Upserts aircraft metadata into the `aircraft` table
3. Inserts position records with PostGIS geometry into `flight_positions`
4. Creates/updates flight track linestrings in `flight_tracks`

## Ports

| Port | Purpose |
|---|---|
| 8081 | Health checks (`/health`, `/ready`) |
| 9090 | Prometheus metrics |

## Metrics

| Metric | Description |
|---|---|
| `number_of_records_read_from_kafka` | Records consumed from Kafka |
| `number_of_records_ingested` | Records successfully written to DB |
| `number_of_records_failed_to_ingest` | Records that failed to write |
| `rate_of_records` | Ingestion rate over time |

## Configuration

Via environment variables or `application.conf`:

| Variable | Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka-cluster-kafka-bootstrap:9092` | Kafka brokers |
| `KAFKA_TOPIC` | `military_flights` | Kafka topic |
| `KAFKA_CONSUMER_GROUP` | `db-ingestor-group` | Consumer group ID |
| `DATABASE_URL` | `jdbc:postgresql://postgres-cluster-rw:5432/militarytracker` | JDBC URL |
| `DATABASE_USERNAME` | `postgres` | DB username |
| `DATABASE_PASSWORD` | `postgres` | DB password |

## Build & Run

```bash
# Build
mvn package -pl aircraft-db-ingestor -am -DskipTests

# Run locally
java -jar aircraft-db-ingestor/target/aircraft-db-ingestor-*.jar

# Docker
docker build -t aircraft-db-ingestor:latest -f docker/aircraft-db-ingestor/Dockerfile .
```

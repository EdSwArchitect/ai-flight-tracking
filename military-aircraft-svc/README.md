# Military Aircraft SVC

Data ingestion service that polls the ADS-B military API and publishes aircraft records to Kafka.

## How It Works

1. Polls `https://api.adsb.lol/v2/mil` every 15 seconds
2. Deserializes the JSON response into `AcItem` objects
3. Publishes each aircraft record to the `military_flights` Kafka topic

## Ports

| Port | Purpose |
|---|---|
| 8081 | Health checks (`/health`, `/ready`) |
| 9090 | Prometheus metrics |

## Metrics

| Metric | Description |
|---|---|
| `number_of_metrics_retrieved` | Total aircraft records retrieved |
| `number_of_calls_successful` | Successful API calls |
| `number_of_calls_unsuccessful` | Failed API calls |
| `metrics_retrieved_rate` | Retrieval rate over time |

## Configuration

Via environment variables or `application.conf`:

| Variable | Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka-cluster-kafka-bootstrap:9092` | Kafka brokers |
| `ADSB_API_URL` | `https://api.adsb.lol/v2/mil` | ADS-B API endpoint |
| `POLL_INTERVAL_SECONDS` | `15` | Polling interval |

## Build & Run

```bash
# Build
mvn package -pl military-aircraft-svc -am -DskipTests

# Run locally
java -jar military-aircraft-svc/target/military-aircraft-svc-*.jar

# Docker
docker build -t military-aircraft-svc:latest -f docker/military-aircraft-svc/Dockerfile .
```

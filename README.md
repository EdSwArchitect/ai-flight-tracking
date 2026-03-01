# Military Aircraft Tracker

An event-driven system that ingests real-time military aircraft ADS-B data, streams it through Kafka, persists it to PostgreSQL (with PostGIS) and OpenSearch, and exposes it via a REST API with a React frontend.

## Architecture

```
ADS-B API (api.adsb.lol/v2/mil)
       |
       v
Military Aircraft SVC  -->  Kafka (military_flights topic)
                                |
                     +----------+----------+
                     |                     |
                     v                     v
          Aircraft DB Ingestor      Geo Ingestor
                     |                     |
                     v                     v
           PostgreSQL + PostGIS       OpenSearch
          (write/read replicas)    (geo_shape index)
                     |                     |
                     v                     v
       Military Watcher REST API   OpenSearch Dashboards
                     |
                     v
            React Frontend (Leaflet maps)

        Prometheus + Grafana (observability)
```

## Services

| Service | Description | Port |
|---------|-------------|------|
| **military-aircraft-svc** | Polls ADS-B API, publishes to Kafka | 9090 (metrics) |
| **aircraft-db-ingestor** | Kafka consumer, writes to PostgreSQL | 9090 (metrics) |
| **geo-ingestor** | Kafka consumer, indexes GeoJSON to OpenSearch | 8080, 8081 (metrics) |
| **military-watcher-api** | REST API serving flight data | 7070 |
| **frontend** | React + Leaflet UI | 80 |

## Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 22+ (frontend)
- Docker & Docker Compose
- Kind + kubectl (for Kubernetes deployment)

## Quick Start (Docker Compose)

### Unencrypted

```bash
# Build all services
mvn clean package -DskipTests

# Start infrastructure (Kafka, PostgreSQL, OpenSearch, Prometheus, Grafana)
bash scripts/start-infra.sh

# Stop infrastructure
bash scripts/stop-infra.sh

# Stop and remove all data volumes
bash scripts/stop-infra.sh --clean
```

### TLS

```bash
# Generates certificates automatically if not present
bash scripts/start-infra-tls.sh

# Stop TLS infrastructure
bash scripts/stop-infra.sh --tls
```

### Access Points (Docker)

| Service | Unencrypted | TLS |
|---------|-------------|-----|
| Kafka | localhost:9092 | localhost:9093 (SSL) |
| PostgreSQL | localhost:5432 | localhost:5432 (ssl=on) |
| OpenSearch | http://localhost:9200 | https://localhost:9200 |
| OpenSearch Dashboards | http://localhost:5601 | http://localhost:5601 |
| Prometheus | http://localhost:9090 | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) | http://localhost:3000 (admin/admin) |

## Kubernetes Deployment (Kind)

```bash
# Create Kind cluster and deploy everything
bash k8s/setup-kind.sh

# With TLS enabled
bash k8s/setup-kind.sh --tls

# Tear down (with confirmation prompt)
bash k8s/teardown-kind.sh

# Tear down without confirmation
bash k8s/teardown-kind.sh --force
```

### Access Points (Kind)

| Service | URL |
|---------|-----|
| Frontend | http://localhost:80 |
| OpenSearch Dashboards | http://localhost:5601 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |
| Kafka Bridge | http://localhost:8880 |

### Useful kubectl Commands

```bash
kubectl get pods -n military-tracker
kubectl logs -f deployment/military-aircraft-svc -n military-tracker
kubectl logs -f deployment/geo-ingestor -n military-tracker
kubectl exec -it statefulset/opensearch -n military-tracker -- curl localhost:9200/_cat/indices
```

## Building

```bash
# Build all Java modules
mvn clean package

# Build with tests
mvn clean verify

# Build Docker images
docker build -t military-aircraft-svc:latest -f docker/military-aircraft-svc/Dockerfile .
docker build -t aircraft-db-ingestor:latest -f docker/aircraft-db-ingestor/Dockerfile .
docker build -t geo-ingestor:latest -f docker/geo-ingestor/Dockerfile .
docker build -t military-watcher-api:latest -f docker/military-watcher-api/Dockerfile .
docker build -t military-tracker-frontend:latest -f docker/frontend/Dockerfile .
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/list-flights?limit=50&offset=0` | Paginated list of tracked military flights |
| `GET` | `/list-flight/{id}` | Get details of a specific flight |
| `GET` | `/flight-track/{id}` | Get historical track points for a flight's aircraft |
| `POST` | `/geobox-list-flight` | List flights within a geographic bounding box |

## TLS Certificate Generation

```bash
# Generate self-signed CA + server/client certificates
bash scripts/generate-certs.sh

# Force regeneration
bash scripts/generate-certs.sh --force
```

Certificates are written to `certs/` with subdirectories for `ca/`, `kafka/`, `postgres/`, `opensearch/`, and `client/`. Includes PEM, PKCS12, and JKS formats.

## Project Structure

```
.
├── common/                    # Shared library (Kafka, JDBC, TLS utilities)
├── model/                     # Data models
├── military-aircraft-svc/     # ADS-B poller + Kafka producer
├── aircraft-db-ingestor/      # Kafka consumer + PostgreSQL writer
├── geo-ingestor/              # Kafka consumer + OpenSearch indexer (Spring Boot)
├── military-watcher-api/      # REST API
├── frontend/                  # React + Leaflet frontend
├── docker/                    # Dockerfiles and Compose configs
│   ├── docker-compose.yml     # Unencrypted infrastructure
│   └── docker-compose-tls.yml # TLS infrastructure
├── k8s/                       # Kubernetes manifests
│   ├── setup-kind.sh          # Kind cluster setup script
│   └── tls/                   # TLS-specific K8s manifests
├── scripts/                   # Infrastructure management scripts
├── grafana/                   # Grafana dashboard definitions
└── certs/                     # Generated TLS certificates
```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Backend | Java 21, Plain Java + Spring Boot |
| Frontend | React 19, TypeScript, Vite, Leaflet |
| Message Broker | Apache Kafka 4.0 (Strimzi on K8s) |
| Database | PostgreSQL 17 + PostGIS 3.5 |
| Search | OpenSearch 2.18 |
| Monitoring | Prometheus, Grafana |
| Container | Docker, Kind (Kubernetes) |

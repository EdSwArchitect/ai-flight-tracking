# Military Watcher API

REST API serving flight data to the frontend using Javalin 6.x and PostgreSQL/PostGIS.

Javalin 6.x requires Jetty 11. The `jetty-bom:11.0.24` is imported in the module's `dependencyManagement` to override Spring Boot's Jetty 12 management.

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/list-flights?limit=50&offset=0` | Paginated list of all tracked flights |
| `GET` | `/list-flight/{id}` | Flight detail by ID |
| `GET` | `/flight-track/{id}` | Historical track points for a flight's aircraft |
| `POST` | `/geobox-list-flight` | Flights within a geographic bounding box |

### GeoBox Request Body

```json
{
  "north": 42.0,
  "south": 38.0,
  "east": -74.0,
  "west": -80.0
}
```

## Ports

| Port | Purpose |
|---|---|
| 7070 | REST API |
| 8081 | Health checks (`/health`, `/ready`) |
| 9090 | Prometheus metrics |

## Configuration

Via environment variables or `application.conf`:

| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://postgres-cluster-ro:5432/militarytracker` | JDBC URL (read replica) |
| `DATABASE_USERNAME` | `postgres` | DB username |
| `DATABASE_PASSWORD` | `postgres` | DB password |
| `SERVER_PORT` | `7070` | API server port |

## Build & Run

```bash
# Build
mvn package -pl military-watcher-api -am -DskipTests

# Run locally
java -jar military-watcher-api/target/military-watcher-api-*.jar

# Docker
docker build -t military-watcher-api:latest -f docker/military-watcher-api/Dockerfile .
```

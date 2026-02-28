#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="${PROJECT_DIR}/docker/docker-compose.yml"

echo "==> Starting Military Tracker infrastructure (unencrypted)..."
docker compose -f "$COMPOSE_FILE" up -d

echo "==> Waiting for services to be healthy..."

echo -n "    Kafka: "
until docker compose -f "$COMPOSE_FILE" exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ready"

echo -n "    PostgreSQL: "
until docker compose -f "$COMPOSE_FILE" exec -T postgres pg_isready -U postgres >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ready"

echo -n "    OpenSearch: "
until curl -s http://localhost:9200/_cluster/health >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ready"

echo -n "    OpenSearch Dashboards: "
attempts=0
until curl -s http://localhost:5601/api/status >/dev/null 2>&1; do
    echo -n "."
    sleep 3
    attempts=$((attempts + 1))
    if [ $attempts -ge 40 ]; then
        echo " timeout (may still be starting)"
        break
    fi
done
[ $attempts -lt 40 ] && echo " ready"

echo -n "    Prometheus: "
until curl -s http://localhost:9090/-/ready >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ready"

echo -n "    Grafana: "
until curl -s http://localhost:3000/api/health >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ready"

# Create Kafka topic if it doesn't exist
echo "==> Creating Kafka topic 'military_flights'..."
docker compose -f "$COMPOSE_FILE" exec -T kafka \
    /opt/kafka/bin/kafka-topics.sh \
    --create \
    --if-not-exists \
    --bootstrap-server localhost:9092 \
    --topic military_flights \
    --partitions 6 \
    --replication-factor 1 \
    --config retention.ms=86400000 \
    --config cleanup.policy=delete \
    2>/dev/null || true

echo ""
echo "==> Infrastructure is running!"
echo ""
echo "Access points:"
echo "  Kafka:                  localhost:9092"
echo "  PostgreSQL:             localhost:5432  (postgres/postgres/militarytracker)"
echo "  OpenSearch:             http://localhost:9200"
echo "  OpenSearch Dashboards:  http://localhost:5601"
echo "  Prometheus:             http://localhost:9090"
echo "  Grafana:                http://localhost:3000  (admin/admin)"
echo ""
echo "Useful commands:"
echo "  docker compose -f docker/docker-compose.yml logs -f kafka"
echo "  docker compose -f docker/docker-compose.yml exec postgres psql -U postgres -d militarytracker"
echo "  bash scripts/stop-infra.sh"

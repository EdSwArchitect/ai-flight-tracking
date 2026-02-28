#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="${PROJECT_DIR}/docker/docker-compose-tls.yml"
CERTS_DIR="${PROJECT_DIR}/certs"

echo "==> Starting Military Tracker infrastructure (TLS)..."

# Generate certificates if they don't exist
if [ ! -d "$CERTS_DIR" ]; then
    echo "==> Certificates not found. Generating..."
    bash "${SCRIPT_DIR}/generate-certs.sh"
    echo ""
fi

docker compose -f "$COMPOSE_FILE" up -d

echo "==> Waiting for services to be healthy..."

echo -n "    Kafka (TLS): "
attempts=0
until docker compose -f "$COMPOSE_FILE" exec -T kafka bash -c 'echo | openssl s_client -connect localhost:9093 2>/dev/null | grep -q "CONNECTED"' 2>/dev/null; do
    echo -n "."
    sleep 3
    attempts=$((attempts + 1))
    if [ $attempts -ge 40 ]; then
        echo " timeout (may still be starting)"
        break
    fi
done
[ $attempts -lt 40 ] && echo " ready"

echo -n "    PostgreSQL (TLS): "
until docker compose -f "$COMPOSE_FILE" exec -T postgres pg_isready -U postgres >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ready"

echo -n "    OpenSearch (HTTPS): "
attempts=0
until curl -sk https://localhost:9200/_cluster/health >/dev/null 2>&1; do
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

# Create Kafka topic via SSL
echo "==> Creating Kafka topic 'military_flights' (TLS)..."
docker compose -f "$COMPOSE_FILE" exec -T kafka bash -c '
cat > /tmp/client-ssl.properties <<PROPS
security.protocol=SSL
ssl.truststore.location=/etc/kafka/certs/ca-truststore.p12
ssl.truststore.password=changeit
ssl.truststore.type=PKCS12
PROPS
/opt/kafka/bin/kafka-topics.sh \
    --create \
    --if-not-exists \
    --bootstrap-server localhost:9093 \
    --command-config /tmp/client-ssl.properties \
    --topic military_flights \
    --partitions 6 \
    --replication-factor 1 \
    --config retention.ms=86400000 \
    --config cleanup.policy=delete
' 2>/dev/null || true

echo ""
echo "==> TLS Infrastructure is running!"
echo ""
echo "Access points:"
echo "  Kafka (SSL):    localhost:9093"
echo "  PostgreSQL:     localhost:5432  (ssl=on, postgres/postgres/militarytracker)"
echo "  OpenSearch:     https://localhost:9200"
echo "  Prometheus:     http://localhost:9090"
echo "  Grafana:        http://localhost:3000  (admin/admin)"
echo ""
echo "TLS connection examples:"
echo "  Kafka:      kafka-topics.sh --bootstrap-server localhost:9093 --command-config client-ssl.properties --list"
echo "  PostgreSQL: psql \"sslmode=require host=localhost user=postgres dbname=militarytracker\""
echo "  OpenSearch: curl --cacert certs/ca/ca.crt https://localhost:9200/_cluster/health"
echo ""
echo "Useful commands:"
echo "  docker compose -f docker/docker-compose-tls.yml logs -f kafka"
echo "  bash scripts/stop-infra.sh --tls"

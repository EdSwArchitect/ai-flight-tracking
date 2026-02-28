#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CLUSTER_NAME="military-tracker"

# ---------------------------------------------------------------------------
# Parse arguments
# ---------------------------------------------------------------------------
TLS=false
for arg in "$@"; do
    case "$arg" in
        --tls) TLS=true ;;
        --help|-h)
            echo "Usage: $0 [--tls]"
            echo ""
            echo "Sets up a Kind cluster with the Military Tracker infrastructure."
            echo ""
            echo "Options:"
            echo "  --tls    Enable TLS for Kafka, PostgreSQL, and OpenSearch"
            exit 0
            ;;
        *) echo "Unknown argument: $arg"; exit 1 ;;
    esac
done

if [ "$TLS" = "true" ]; then
    echo "==> TLS mode enabled"
fi

# ---------------------------------------------------------------------------
# Create Kind cluster
# ---------------------------------------------------------------------------
echo "==> Creating Kind cluster..."
kind create cluster --config "$SCRIPT_DIR/kind-config.yaml"

echo "==> Installing Strimzi Kafka Operator..."
kubectl create namespace kafka || true
kubectl apply -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
echo "Waiting for Strimzi operator to be ready..."
kubectl wait --for=condition=Ready pod -l name=strimzi-cluster-operator -n kafka --timeout=300s

echo "==> Creating application namespace..."
kubectl apply -f "$SCRIPT_DIR/namespace.yaml"

# ---------------------------------------------------------------------------
# TLS: Generate certificates and create K8s secrets
# ---------------------------------------------------------------------------
if [ "$TLS" = "true" ]; then
    echo "==> Generating TLS certificates and creating K8s secrets..."
    bash "$SCRIPT_DIR/tls/generate-k8s-certs.sh"
fi

# ---------------------------------------------------------------------------
# Deploy Kafka
# ---------------------------------------------------------------------------
echo "==> Deploying Kafka metrics ConfigMap..."
kubectl apply -f "$SCRIPT_DIR/kafka/kafka-metrics-configmap.yaml"

echo "==> Deploying Kafka cluster (KRaft mode)..."
kubectl apply -f "$SCRIPT_DIR/kafka/kafka-cluster.yaml"
echo "Waiting for Kafka cluster to be ready..."
kubectl wait kafka/military-tracker-kafka --for=condition=Ready -n military-tracker --timeout=300s

echo "==> Creating Kafka topic..."
kubectl apply -f "$SCRIPT_DIR/kafka/kafka-topic.yaml"

echo "==> Deploying Kafka Bridge..."
kubectl apply -f "$SCRIPT_DIR/kafka/kafka-bridge.yaml"

# ---------------------------------------------------------------------------
# Deploy PostgreSQL
# ---------------------------------------------------------------------------
if [ "$TLS" = "true" ]; then
    echo "==> Deploying PostgreSQL (TLS)..."
    kubectl apply -f "$SCRIPT_DIR/postgres/postgres-secret.yaml"
    kubectl apply -f "$SCRIPT_DIR/postgres/postgres-configmap.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/postgres-tls-statefulset.yaml"
    # Deploy replica without TLS (reads don't require TLS in dev)
    kubectl apply -f "$SCRIPT_DIR/postgres/postgres-replica-statefulset.yaml"
else
    echo "==> Deploying PostgreSQL..."
    kubectl apply -f "$SCRIPT_DIR/postgres/"
fi

# ---------------------------------------------------------------------------
# Deploy OpenSearch
# ---------------------------------------------------------------------------
if [ "$TLS" = "true" ]; then
    echo "==> Deploying OpenSearch (TLS)..."
    kubectl apply -f "$SCRIPT_DIR/tls/opensearch-tls-statefulset.yaml"
else
    echo "==> Deploying OpenSearch..."
    kubectl apply -f "$SCRIPT_DIR/opensearch/"
fi

echo "==> Waiting for infrastructure to be ready..."
kubectl wait --for=condition=Ready pod -l app=postgres-primary -n military-tracker --timeout=180s || true
kubectl wait --for=condition=Ready pod -l app=opensearch -n military-tracker --timeout=180s || true

# ---------------------------------------------------------------------------
# Build and load Docker images
# ---------------------------------------------------------------------------
echo "==> Building Docker images..."
cd "$PROJECT_DIR"
docker build -t military-aircraft-svc:latest -f docker/military-aircraft-svc/Dockerfile .
docker build -t aircraft-db-ingestor:latest -f docker/aircraft-db-ingestor/Dockerfile .
docker build -t geo-ingestor:latest -f docker/geo-ingestor/Dockerfile .
docker build -t military-watcher-api:latest -f docker/military-watcher-api/Dockerfile .
docker build -t military-tracker-frontend:latest -f docker/frontend/Dockerfile .

echo "==> Loading images into Kind..."
kind load docker-image military-aircraft-svc:latest --name "$CLUSTER_NAME"
kind load docker-image aircraft-db-ingestor:latest --name "$CLUSTER_NAME"
kind load docker-image geo-ingestor:latest --name "$CLUSTER_NAME"
kind load docker-image military-watcher-api:latest --name "$CLUSTER_NAME"
kind load docker-image military-tracker-frontend:latest --name "$CLUSTER_NAME"

# ---------------------------------------------------------------------------
# Deploy application services
# ---------------------------------------------------------------------------
if [ "$TLS" = "true" ]; then
    echo "==> Deploying TLS ConfigMaps..."
    kubectl apply -f "$SCRIPT_DIR/tls/military-aircraft-svc-tls-config.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/aircraft-db-ingestor-tls-config.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/geo-ingestor-tls-config.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/military-watcher-api-tls-config.yaml"

    echo "==> Deploying application services (TLS)..."
    kubectl apply -f "$SCRIPT_DIR/tls/military-aircraft-svc-tls-deployment.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/aircraft-db-ingestor-tls-deployment.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/geo-ingestor-tls-deployment.yaml"
    kubectl apply -f "$SCRIPT_DIR/tls/military-watcher-api-tls-deployment.yaml"
else
    echo "==> Deploying application services..."
    kubectl apply -f "$SCRIPT_DIR/military-aircraft-svc/"
    kubectl apply -f "$SCRIPT_DIR/aircraft-db-ingestor/"
    kubectl apply -f "$SCRIPT_DIR/geo-ingestor/"
    kubectl apply -f "$SCRIPT_DIR/military-watcher-api/"
fi

# Frontend does not need TLS changes (static files served by Nginx)
kubectl apply -f "$SCRIPT_DIR/frontend/"

echo "==> Deploying monitoring stack..."
kubectl apply -f "$SCRIPT_DIR/monitoring/"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
echo "==> Deployment complete!"
if [ "$TLS" = "true" ]; then
    echo "    Mode: TLS (Kafka:9093, PostgreSQL:SSL, OpenSearch:HTTPS)"
else
    echo "    Mode: Unencrypted (Kafka:9092, PostgreSQL:plain, OpenSearch:HTTP)"
fi
echo ""
echo "Access points:"
echo "  Frontend:       http://localhost:80"
echo "  Prometheus:     http://localhost:9090"
echo "  Grafana:        http://localhost:3000  (admin/admin)"
echo "  Kafka Bridge:   http://localhost:8880"
echo ""
echo "Useful commands:"
echo "  kubectl get pods -n military-tracker"
echo "  kubectl logs -f deployment/military-aircraft-svc -n military-tracker"
echo "  kind delete cluster --name $CLUSTER_NAME"

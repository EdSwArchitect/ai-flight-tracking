#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# ---------------------------------------------------------------------------
usage() {
    echo "Usage: $0 [--tls] [--clean]"
    echo ""
    echo "Stops the Military Tracker Docker infrastructure."
    echo ""
    echo "Options:"
    echo "  --tls    Stop the TLS infrastructure (default: unencrypted)"
    echo "  --clean  Remove volumes and all data (default: preserve data)"
}

TLS=false
CLEAN=false

for arg in "$@"; do
    case "$arg" in
        --tls) TLS=true ;;
        --clean) CLEAN=true ;;
        --help|-h) usage; exit 0 ;;
        *) echo "Unknown argument: $arg"; usage; exit 1 ;;
    esac
done

if [ "$TLS" = "true" ]; then
    COMPOSE_FILE="${PROJECT_DIR}/docker/docker-compose-tls.yml"
    MODE="TLS"
else
    COMPOSE_FILE="${PROJECT_DIR}/docker/docker-compose.yml"
    MODE="unencrypted"
fi

echo "==> Stopping Military Tracker infrastructure (${MODE})..."

if [ "$CLEAN" = "true" ]; then
    echo "    Removing containers and volumes..."
    docker compose -f "$COMPOSE_FILE" down -v
    echo ""
    echo "==> Infrastructure stopped. All data volumes removed."
else
    docker compose -f "$COMPOSE_FILE" down
    echo ""
    echo "==> Infrastructure stopped. Data volumes preserved."
    echo "    Use --clean to also remove volumes."
fi

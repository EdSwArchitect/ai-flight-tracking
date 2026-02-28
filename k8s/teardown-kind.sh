#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME="military-tracker"

# ---------------------------------------------------------------------------
usage() {
    echo "Usage: $0 [--force]"
    echo ""
    echo "Tears down the Military Tracker Kind cluster."
    echo ""
    echo "Options:"
    echo "  --force  Skip confirmation prompt"
}

FORCE=false

for arg in "$@"; do
    case "$arg" in
        --force) FORCE=true ;;
        --help|-h) usage; exit 0 ;;
        *) echo "Unknown argument: $arg"; usage; exit 1 ;;
    esac
done

# Check if cluster exists
if ! kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
    echo "Kind cluster '${CLUSTER_NAME}' does not exist."
    exit 0
fi

if [ "$FORCE" != "true" ]; then
    echo "This will delete the Kind cluster '${CLUSTER_NAME}' and all its data."
    read -r -p "Are you sure? [y/N] " response
    case "$response" in
        [yY][eE][sS]|[yY]) ;;
        *) echo "Aborted."; exit 0 ;;
    esac
fi

echo "==> Deleting Kind cluster '${CLUSTER_NAME}'..."
kind delete cluster --name "$CLUSTER_NAME"

echo ""
echo "==> Kind cluster deleted."
echo "    All Kubernetes resources and persistent volumes have been removed."

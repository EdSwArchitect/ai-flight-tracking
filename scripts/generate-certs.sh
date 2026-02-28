#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CERTS_DIR="${PROJECT_DIR}/certs"

# Configurable defaults
CA_VALIDITY="${CA_VALIDITY:-3650}"
CERT_VALIDITY="${CERT_VALIDITY:-365}"
STORE_PASSWORD="${STORE_PASSWORD:-changeit}"
KEY_SIZE="${KEY_SIZE:-2048}"

# SANs covering localhost, Docker service names, and K8s service names
KAFKA_SANS="DNS:localhost,DNS:kafka,DNS:military-tracker-kafka-kafka-bootstrap,DNS:military-tracker-kafka-kafka-brokers,IP:127.0.0.1"
POSTGRES_SANS="DNS:localhost,DNS:postgres,DNS:postgres-primary,DNS:postgres-replica,IP:127.0.0.1"
OPENSEARCH_SANS="DNS:localhost,DNS:opensearch-node,DNS:opensearch-cluster,IP:127.0.0.1"
CLIENT_SANS="DNS:localhost,DNS:military-aircraft-svc,DNS:aircraft-db-ingestor,DNS:geo-ingestor,DNS:military-watcher-api,IP:127.0.0.1"

# ---------------------------------------------------------------------------
usage() {
    echo "Usage: $0 [--force]"
    echo ""
    echo "Generates self-signed CA + server/client certificates for development."
    echo ""
    echo "Options:"
    echo "  --force    Regenerate certificates even if certs/ directory exists"
    echo ""
    echo "Environment variables:"
    echo "  CA_VALIDITY      CA certificate validity in days (default: 3650)"
    echo "  CERT_VALIDITY    Server/client certificate validity in days (default: 365)"
    echo "  STORE_PASSWORD   Keystore/truststore password (default: changeit)"
    echo "  KEY_SIZE         RSA key size in bits (default: 2048)"
}

FORCE=false
for arg in "$@"; do
    case "$arg" in
        --force) FORCE=true ;;
        --help|-h) usage; exit 0 ;;
        *) echo "Unknown argument: $arg"; usage; exit 1 ;;
    esac
done

if [ -d "$CERTS_DIR" ] && [ "$FORCE" != "true" ]; then
    echo "Certificate directory already exists: $CERTS_DIR"
    echo "Use --force to regenerate."
    exit 0
fi

# Clean and create directory structure
rm -rf "$CERTS_DIR"
mkdir -p "$CERTS_DIR"/{ca,kafka,postgres,opensearch,client}

echo "==> Generating certificates in $CERTS_DIR"
echo "    CA validity:     ${CA_VALIDITY} days"
echo "    Cert validity:   ${CERT_VALIDITY} days"
echo "    Key size:        ${KEY_SIZE} bits"
echo "    Store password:  ${STORE_PASSWORD}"
echo ""

# ---------------------------------------------------------------------------
# 1. Certificate Authority
# ---------------------------------------------------------------------------
echo "==> [1/5] Generating Certificate Authority..."

openssl req -x509 -new -nodes \
    -keyout "$CERTS_DIR/ca/ca.key" \
    -out "$CERTS_DIR/ca/ca.crt" \
    -days "$CA_VALIDITY" \
    -subj "/C=US/ST=Dev/L=Local/O=MilitaryTracker/OU=CA/CN=MilitaryTracker-CA" \
    -addext "basicConstraints=critical,CA:TRUE" \
    -addext "keyUsage=critical,keyCertSign,cRLSign" \
    2>/dev/null

# CA truststore — PKCS12
openssl pkcs12 -export \
    -in "$CERTS_DIR/ca/ca.crt" \
    -nokeys \
    -out "$CERTS_DIR/ca/ca-truststore.p12" \
    -password "pass:${STORE_PASSWORD}" \
    -name "ca" \
    2>/dev/null

# CA truststore — JKS
keytool -importcert -noprompt \
    -alias "ca" \
    -file "$CERTS_DIR/ca/ca.crt" \
    -keystore "$CERTS_DIR/ca/ca-truststore.jks" \
    -storepass "$STORE_PASSWORD" \
    -storetype JKS \
    2>/dev/null

echo "    Created: ca.key, ca.crt, ca-truststore.p12, ca-truststore.jks"

# ---------------------------------------------------------------------------
# Helper: generate a signed certificate
# ---------------------------------------------------------------------------
generate_cert() {
    local name="$1"
    local cn="$2"
    local sans="$3"
    local out_dir="$4"
    local key_file="$out_dir/${name}.key"
    local csr_file="$out_dir/${name}.csr"
    local crt_file="$out_dir/${name}.crt"
    local ext_file
    ext_file="$(mktemp)"

    # Generate private key
    openssl genrsa -out "$key_file" "$KEY_SIZE" 2>/dev/null

    # Generate CSR
    openssl req -new \
        -key "$key_file" \
        -out "$csr_file" \
        -subj "/C=US/ST=Dev/L=Local/O=MilitaryTracker/OU=${name}/CN=${cn}" \
        2>/dev/null

    # Extensions file
    cat > "$ext_file" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth,clientAuth
subjectAltName=${sans}
EOF

    # Sign with CA
    openssl x509 -req \
        -in "$csr_file" \
        -CA "$CERTS_DIR/ca/ca.crt" \
        -CAkey "$CERTS_DIR/ca/ca.key" \
        -CAcreateserial \
        -out "$crt_file" \
        -days "$CERT_VALIDITY" \
        -extfile "$ext_file" \
        2>/dev/null

    # Clean up CSR and temp file
    rm -f "$csr_file" "$ext_file"

    # PKCS12 keystore (key + cert + CA chain)
    openssl pkcs12 -export \
        -in "$crt_file" \
        -inkey "$key_file" \
        -chain -CAfile "$CERTS_DIR/ca/ca.crt" \
        -out "$out_dir/${name}-keystore.p12" \
        -password "pass:${STORE_PASSWORD}" \
        -name "$name" \
        2>/dev/null

    # JKS keystore (converted from PKCS12)
    keytool -importkeystore -noprompt \
        -srckeystore "$out_dir/${name}-keystore.p12" \
        -srcstoretype PKCS12 \
        -srcstorepass "$STORE_PASSWORD" \
        -destkeystore "$out_dir/${name}-keystore.jks" \
        -deststoretype JKS \
        -deststorepass "$STORE_PASSWORD" \
        2>/dev/null
}

# ---------------------------------------------------------------------------
# 2. Kafka Server Certificate
# ---------------------------------------------------------------------------
echo "==> [2/5] Generating Kafka server certificate..."

generate_cert "kafka" "kafka" "$KAFKA_SANS" "$CERTS_DIR/kafka"

echo "    Created: kafka.key, kafka.crt, kafka-keystore.p12, kafka-keystore.jks"

# ---------------------------------------------------------------------------
# 3. PostgreSQL Server Certificate
# ---------------------------------------------------------------------------
echo "==> [3/5] Generating PostgreSQL server certificate..."

generate_cert "server" "postgres" "$POSTGRES_SANS" "$CERTS_DIR/postgres"

# PostgreSQL also needs the CA cert as root.crt for client verification
cp "$CERTS_DIR/ca/ca.crt" "$CERTS_DIR/postgres/root.crt"

# PostgreSQL requires key file to be readable only by owner
chmod 600 "$CERTS_DIR/postgres/server.key"

echo "    Created: server.key, server.crt, root.crt, server-keystore.p12, server-keystore.jks"

# ---------------------------------------------------------------------------
# 4. OpenSearch Server Certificate + Admin Certificate
# ---------------------------------------------------------------------------
echo "==> [4/5] Generating OpenSearch certificates..."

generate_cert "opensearch" "opensearch-node" "$OPENSEARCH_SANS" "$CERTS_DIR/opensearch"

# Admin certificate for OpenSearch security plugin
generate_cert "admin" "admin" "DNS:localhost,IP:127.0.0.1" "$CERTS_DIR/opensearch"

echo "    Created: opensearch.key, opensearch.crt, opensearch-keystore.p12, opensearch-keystore.jks"
echo "    Created: admin.key, admin.crt, admin-keystore.p12, admin-keystore.jks"

# ---------------------------------------------------------------------------
# 5. Client Certificate (for Java services)
# ---------------------------------------------------------------------------
echo "==> [5/5] Generating client certificate..."

generate_cert "client" "military-tracker-client" "$CLIENT_SANS" "$CERTS_DIR/client"

# Also copy CA truststore into client dir for convenience
cp "$CERTS_DIR/ca/ca-truststore.p12" "$CERTS_DIR/client/"
cp "$CERTS_DIR/ca/ca-truststore.jks" "$CERTS_DIR/client/"

echo "    Created: client.key, client.crt, client-keystore.p12, client-keystore.jks"
echo "    Copied:  ca-truststore.p12, ca-truststore.jks"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
echo "==> Certificate generation complete!"
echo ""
echo "Directory structure:"
find "$CERTS_DIR" -type f | sort | sed "s|${PROJECT_DIR}/||"
echo ""
echo "Keystore/truststore password: ${STORE_PASSWORD}"
echo ""
echo "Verify CA:        openssl x509 -in certs/ca/ca.crt -text -noout"
echo "Verify Kafka:     openssl x509 -in certs/kafka/kafka.crt -text -noout"
echo "Verify JKS:       keytool -list -keystore certs/ca/ca-truststore.jks -storepass ${STORE_PASSWORD}"
echo "Verify PKCS12:    keytool -list -keystore certs/client/client-keystore.p12 -storepass ${STORE_PASSWORD}"

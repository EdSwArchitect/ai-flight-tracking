# Cloud Cost Comparison: Military Aircraft Tracker

Estimated monthly cloud costs for deploying the Military Aircraft Tracker application across AWS, Azure, and Google Cloud. Costs are based on US East regions with on-demand pricing unless noted. All prices in USD/month.

---

## Application Resource Requirements

| Component | Count | CPU | Memory | Storage | Notes |
|---|---|---|---|---|---|
| military-aircraft-svc | 1 | 0.5 vCPU | 512 MB | -- | ADS-B poller + Kafka producer |
| aircraft-db-ingestor | 1 | 0.5 vCPU | 512 MB | -- | Kafka consumer + PostgreSQL writer |
| geo-ingestor | 1 | 0.5 vCPU | 768 MB | -- | Spring Boot Kafka consumer + OpenSearch indexer |
| military-watcher-api | 1 | 0.5 vCPU | 512 MB | -- | Javalin REST API |
| frontend | 1 | 0.1 vCPU | 128 MB | -- | Nginx serving React SPA |
| Kafka | 3 brokers | 2 vCPU each | 8 GB each | 100 GB each | Message streaming |
| PostgreSQL + PostGIS | 1 primary + 1 replica | 2 vCPU each | 8 GB each | 100 GB | Flight positions + aircraft data |
| OpenSearch | 2 nodes | 2 vCPU each | 8 GB each | 100 GB each | Geo-spatial indexing |
| Prometheus | 1 | 0.5 vCPU | 1 GB | 50 GB | Metrics collection |
| Grafana | 1 | 0.25 vCPU | 512 MB | 1 GB | Dashboards |

**Total application services**: ~2.1 vCPU, ~2.4 GB RAM
**Total infrastructure**: ~14 vCPU, ~42 GB RAM, ~550 GB storage

---

## Scenario 1: AWS with Kubernetes (EKS)

Uses managed services for Kafka, PostgreSQL, and OpenSearch.

| Component | Service | Specification | Monthly Cost |
|---|---|---|---|
| EKS Control Plane | EKS | Standard tier | $73 |
| Worker Nodes (apps + monitoring) | EC2 | 2x m6i.large (2 vCPU, 8 GB) | $140 |
| PostgreSQL | RDS PostgreSQL | db.r6g.large (2 vCPU, 16 GB) + 100 GB gp3 | $176 |
| PostgreSQL Read Replica | RDS PostgreSQL | db.r6g.large + 100 GB gp3 | $176 |
| Kafka | MSK | 3x kafka.m5.large + 300 GB storage | $483 |
| OpenSearch | OpenSearch Service | 2x m6g.large.search + 200 GB gp3 | $203 |
| Load Balancer | ALB | Application Load Balancer | $25 |
| Data Transfer | -- | 100 GB egress (free tier) | $0 |
| **Total (On-Demand)** | | | **$1,276/mo** |
| **Total (1yr Reserved)** | | | **~$850/mo** |

### AWS EKS -- Cost Reduction Options

| Optimization | Savings | Revised Cost |
|---|---|---|
| Use db.t3.medium for PostgreSQL (dev/test) | -$222 | $1,054/mo |
| Spot instances for worker nodes (~60% off) | -$84 | $1,192/mo |
| MSK Serverless (low-volume) | -$300 | ~$976/mo |
| 1-year Reserved Instances (all components) | -35% compute | ~$850/mo |
| 3-year Reserved Instances | -60% compute | ~$620/mo |

---

## Scenario 2: AWS with EC2 (No Kubernetes)

All services run on EC2 instances using Docker Compose or systemd. Self-managed Kafka, PostgreSQL, and OpenSearch.

| Component | Specification | Monthly Cost |
|---|---|---|
| App Server | 1x m6i.xlarge (4 vCPU, 16 GB) -- all 5 microservices + monitoring | $140 |
| Database Server | 1x m6i.xlarge (4 vCPU, 16 GB) -- PostgreSQL + PostGIS | $140 |
| Data Server | 1x m6i.2xlarge (8 vCPU, 32 GB) -- Kafka (3 brokers) + OpenSearch | $280 |
| EBS Storage | 500 GB gp3 (across all instances) | $40 |
| Load Balancer | ALB | $25 |
| Data Transfer | 100 GB egress (free tier) | $0 |
| **Total (On-Demand)** | | **$625/mo** |
| **Total (1yr Reserved)** | | **~$415/mo** |
| **Total (3yr Reserved)** | | **~$275/mo** |

### EC2 Pros and Cons

| Pros | Cons |
|---|---|
| ~50% cheaper than managed K8s + services | Self-managed Kafka, PostgreSQL, OpenSearch |
| Simpler networking | No auto-scaling, no self-healing |
| Full control over configuration | Manual HA/failover setup |
| No managed service overhead costs | Patching and upgrades are your responsibility |
| | No built-in rolling deployments |

---

## Scenario 3: Azure with Kubernetes (AKS)

Uses managed services where available. Azure Event Hubs replaces Kafka (Kafka-protocol compatible).

| Component | Service | Specification | Monthly Cost |
|---|---|---|---|
| AKS Control Plane | AKS Standard | With SLA | $73 |
| Worker Nodes (apps + monitoring) | VMs | 2x Standard_D2s_v5 (2 vCPU, 8 GB) | $140 |
| PostgreSQL | Azure Database Flexible | D2ds_v5 (2 vCPU, 8 GB) + 100 GB | $94 |
| PostgreSQL Read Replica | Azure Database Flexible | D2ds_v5 + 100 GB | $94 |
| Kafka-compatible Streaming | Event Hubs Standard | 5 Throughput Units | $110 |
| Search | Azure AI Search | Standard S1 (1 search unit) | $245 |
| Load Balancer | Standard LB | | $20 |
| Data Transfer | -- | 100 GB egress (free tier) | $0 |
| **Total (On-Demand)** | | | **$776/mo** |
| **Total (1yr Reserved)** | | | **~$550/mo** |

### Azure -- Important Caveats

- **Event Hubs Standard** is not true Apache Kafka. It provides a Kafka-protocol endpoint on a shared service. The application's Kafka client code works with it, but operational behavior differs (no topic compaction, different retention model). For true Kafka, deploy Strimzi on AKS (+$300-400/mo in compute) or use Confluent Cloud (+$400-500/mo).
- **Azure AI Search** is not OpenSearch. It is a different product with AI enrichment capabilities. The geo-ingestor would need code changes to use it. Alternatively, self-host OpenSearch on AKS nodes (+$100-200/mo compute instead of $245).
- **AKS Free Tier** saves $73/mo but provides no control plane SLA.

### Azure with True Kafka + Self-Hosted OpenSearch

| Adjustment | Cost Change |
|---|---|
| Replace Event Hubs with Strimzi on AKS (3x D4s_v5 nodes) | +$310 |
| Replace AI Search with self-hosted OpenSearch (2 pods on existing nodes) | -$145 |
| Add 1x D4s_v5 node for OpenSearch | +$140 |
| **Revised Total** | **~$1,081/mo** |

---

## Scenario 4: Google Cloud with Kubernetes (GKE)

Uses managed services where available.

| Component | Service | Specification | Monthly Cost |
|---|---|---|---|
| GKE Control Plane | GKE Standard | (Free with $74.40 credit) | $0 |
| Worker Nodes (apps + monitoring) | Compute Engine | 2x e2-standard-2 (2 vCPU, 8 GB) | $98 |
| PostgreSQL | Cloud SQL | db-custom-2-8192 (2 vCPU, 8 GB) + 100 GB SSD | $118 |
| PostgreSQL Read Replica | Cloud SQL | db-custom-2-8192 + 100 GB SSD | $118 |
| Kafka | Managed Kafka | 3 brokers (comparable to MSK m5.large) | $450 |
| OpenSearch | Elastic Cloud on GCP | 2-node deployment | $175 |
| Load Balancer | Cloud Load Balancer | | $22 |
| Data Transfer | -- | 100 GB egress (free under 200 GB) | $0 |
| **Total (On-Demand)** | | | **$981/mo** |
| **Total (1yr CUD)** | | | **~$700/mo** |

### GCP -- Cost Reduction Options

| Optimization | Savings | Revised Cost |
|---|---|---|
| GKE Autopilot (pay-per-pod, no idle node waste) | -$20 to -$40 | ~$941-961/mo |
| Preemptible VMs for worker nodes (~80% off) | -$78 | ~$903/mo |
| Self-host OpenSearch on GKE | -$75 | ~$906/mo |
| 1-year Committed Use Discounts | -37% compute | ~$700/mo |
| 3-year Committed Use Discounts | -55% compute | ~$550/mo |

---

## Summary Comparison

### On-Demand Pricing

| Deployment Model | Monthly Cost | Annual Cost |
|---|---|---|
| **AWS EKS** (fully managed) | $1,276 | $15,312 |
| **AWS EC2** (self-managed) | $625 | $7,500 |
| **Azure AKS** (Event Hubs + AI Search) | $776 | $9,312 |
| **Azure AKS** (true Kafka + OpenSearch) | $1,081 | $12,972 |
| **GCP GKE** (fully managed) | $981 | $11,772 |

### With 1-Year Commitments

| Deployment Model | Monthly Cost | Annual Cost | vs On-Demand |
|---|---|---|---|
| **AWS EKS** | ~$850 | ~$10,200 | -33% |
| **AWS EC2** | ~$415 | ~$4,980 | -34% |
| **Azure AKS** (Event Hubs) | ~$550 | ~$6,600 | -29% |
| **Azure AKS** (true Kafka) | ~$750 | ~$9,000 | -31% |
| **GCP GKE** | ~$700 | ~$8,400 | -29% |

### With 3-Year Commitments

| Deployment Model | Monthly Cost | Annual Cost | vs On-Demand |
|---|---|---|---|
| **AWS EKS** | ~$620 | ~$7,440 | -51% |
| **AWS EC2** | ~$275 | ~$3,300 | -56% |
| **Azure AKS** (Event Hubs) | ~$400 | ~$4,800 | -48% |
| **GCP GKE** | ~$550 | ~$6,600 | -44% |

---

## Cost Breakdown by Category

| Category | AWS EKS | AWS EC2 | Azure AKS | GCP GKE |
|---|---|---|---|---|
| Kubernetes / Orchestration | $73 | $0 | $73 | $0* |
| Compute (worker nodes / VMs) | $140 | $560 | $140 | $98 |
| PostgreSQL | $352 | (included) | $188 | $236 |
| Kafka / Streaming | $483 | (included) | $110 | $450 |
| OpenSearch / Search | $203 | (included) | $245 | $175 |
| Load Balancer | $25 | $25 | $20 | $22 |
| **Total** | **$1,276** | **$625** | **$776** | **$981** |

*GKE first cluster free via monthly credit

---

## Development / Test Environment

For a minimal non-HA setup suitable for development:

| Component | AWS EKS | AWS EC2 | Azure AKS | GCP GKE |
|---|---|---|---|---|
| K8s Control Plane | $73 | -- | $0 (Free Tier) | $0 (credit) |
| Compute | $70 (1x m6i.large) | $140 (1x m6i.xlarge) | $61 (1x B2ms) | $49 (1x e2-standard-2) |
| PostgreSQL | $65 (db.t3.medium) | (included) | $94 (D2ds_v5) | $118 (db-custom-2-8192) |
| Kafka | $161 (MSK 1 broker) | (included) | $22 (Event Hubs 1 TU) | $150 (1 broker) |
| OpenSearch | $94 (1x m6g.large) | (included) | $75 (Basic tier) | $95 (Elastic Cloud basic) |
| Load Balancer | $25 | $25 | $20 | $22 |
| **Total** | **$488** | **$165** | **$272** | **$434** |

---

## Recommendations

### Best Overall Value: AWS EC2
At $625/mo on-demand ($275/mo with 3yr RI), running everything on 3 EC2 instances is the cheapest option by far. Trade-off: you manage all infrastructure yourself (patching, HA, backups).

### Best Managed Kubernetes: GCP GKE
GKE offers the cheapest compute ($98 vs $140 for AWS/Azure), a free control plane via credit, and GKE Autopilot for zero node management. Total: $981/mo on-demand.

### Best Budget Kubernetes: Azure AKS
If Azure Event Hubs (Kafka-compatible) and self-hosted OpenSearch are acceptable, Azure AKS is the cheapest managed K8s option at $776/mo, dropping to $550/mo with 1yr reserved.

### Best for Kafka Ecosystem: AWS EKS
AWS MSK is the most mature managed Kafka service with full feature parity. Combined with AWS OpenSearch Service, the entire stack uses battle-tested managed services. Premium cost of $1,276/mo reflects fully managed operations.

### Cost Sensitivity Summary

| Priority | Recommended |
|---|---|
| Lowest cost, willing to self-manage | AWS EC2 ($275-625/mo) |
| Lowest cost Kubernetes, some trade-offs | Azure AKS with Event Hubs ($550-776/mo) |
| Cheapest compute, true Kafka | GCP GKE ($700-981/mo) |
| Fully managed, no compromises | AWS EKS ($850-1,276/mo) |

---

*Pricing sourced from official cloud provider pricing pages (February 2026). Actual costs vary based on usage patterns, data transfer volumes, support plans, and regional pricing. Spot/preemptible instances can reduce compute costs by 60-80% for fault-tolerant workloads.*

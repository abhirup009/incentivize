# ProjectOverview.md
[CREATED 2025-06-27]

## Purpose
Provide a concise yet complete technical overview of the Incentivization Platform – its domain model, architecture, data flows, and how the design meets the stated functional & non-functional requirements.

---
## Table of Contents
1. Introduction & Problem Statement
2. Core Domain Entities
3. High-Level System Architecture
4. Component Responsibilities
5. Data Flow & Sequence Diagrams (textual)
6. Functional Requirements Mapping
7. Non-Functional Requirements & Design Decisions
8. Technology Stack & Deployment Topology
9. Open Questions / Next Steps

---
## 1. Introduction & Problem Statement
Modern consumer applications use incentives (cashback, points, badges) to drive user behaviour. The platform must ingest a high-volume event stream, evaluate tenant-defined rules, manage campaigns/cohorts, and push near-real-time (NRT) incentives back to clients with sub-500 ms latency while remaining highly available under burst traffic ("celebrity problem").

---
## 2. Core Domain Entities
| Entity    | Description |
|-----------|-------------|
| **Tenant** | An organisation using the platform. Owns limits, campaigns, cohorts, and users. |
| **User**   | End-consumer performing actions that may trigger incentives. Always scoped to a single tenant. |
| **Action** | A business-meaningful verb emitted as an `Event` (e.g., `PURCHASE`, `WATCH_VIDEO`). |
| **Event**  | The concrete, timestamped occurrence of an `Action` by a `User`. Contains metadata needed for rule evaluation. |
| **Campaign** | A marketing configuration created by a tenant. Contains rule set, cohort targeting, supported actions, validity window, and a `type` (`SIMPLE`, `QUEST`). |
| **Limit**  | Global and per-user throttles (daily cap, total cap, etc.) defined by tenant to control incentive spend. |
| **Incentive** | The reward generated when a campaign’s conditions are satisfied (coupon, points, etc.). Closely tied to user notification. |

> **Quest vs Simple Campaigns**  
> • *SIMPLE*: single action → incentive.  
> • *QUEST*: ordered set of actions must all occur within campaign window before incentive is issued.

---
## 3. High-Level System Architecture
```
+--------------+      events      +---------+
| Event Svcs   | ───────────────▶ | Broker  |
|(Payments etc)|                  +---+-----+
+--------------+                      │ consume
                                      ▼
                              +-------+-------+
                              |  Incentivize  |
                              |   Consumer    |
                              |     (IC)      |
                              +-------+-------+
                                      │ campaign query
                                      ▼
                              +-------+-------+
                              |     CMS       |
                              +---------------+
                                      │ write incentive
                                      ▼
                              +-------+-------+
                              |   IC DB (PG)  |
                              +-------+-------+
                                      │ CDC (WAL)
               ┌──────────────────────┼──────────────────────┐
               │                      │                      │
               ▼                      ▼                      ▼
      +---------------+   +----------------------+   +--------------+
      | Redis Limits  |   |  Analytics  Bay      |   |   DCM        |
      |  Aggregation  |   |   (OLAP)            |   +------+-------+
      +---------------+   +----------------------+          │ push
                                                             ▼
                                                       +--------------+
                                                       |   Client     |
                                                       +--------------+
```
Key supporting services:
• **Campaign Management System (CMS)** – CRUD for campaigns & cohorts.  
• **Cohort Service** – builds & stores large user segments (pre-computed or dynamic).  
• **Limit Service** – evaluates spend/user limits during rule execution.  
• **Metadata Store** – Postgres/Jooq for relational state; Redis for hot limits.  
• **Analytics Bay** – downstream data warehouse fed via CDC from IC DB.  
• **Redis Aggregation Pipeline** – ingests CDC to update real-time limit counters.  
• **Rule Engine** – in-memory DSL evaluated inside consumer workers.

---
## 4. Component Responsibilities
1. **Event Bus** (Kafka/Pulsar) – durable, ordered, partitioned stream of `Event`s.
2. **Incentivize Consumer (IC) Cluster**  
   • Subscribes to bus, partitions by `tenantId` & `userId` to keep ordering.  
   • For each event: fetch applicable `Campaign`s + `Cohort` membership → run rule engine.  
   • Emits `IncentiveGenerated` when all conditions & limits pass.
3. **Campaign Management System (CMS)**  
   • Exposes REST+OpenAPI endpoints to create, update, fetch campaigns.  
   • Persists to Postgres; publishes cache-invalidation events.
4. **Cohort Service**  
   • Maintains cohort definitions (SQL-based, upload list, or real-time).  
   • Provides membership look-ups with millisecond latency (Redis / Bloom filters).
5. **Distributed Connection Manager (DCM)**  
   • Maintains WebSocket sessions to clients; subscribes to `IncentiveGenerated` topic.  
   • Delivers push notifications or triggers mobile PN gateway.
6. **Limit Service** – centralised evaluation of per-tenant / per-user caps, leveraging Redis counters + Lua scripts for atomicity.
7. **Notification Service** – optional abstraction over APNs/Firebase.

---
## 5. Data Flow & Sequence (SIMPLE campaign)
1. **Event Service → Broker**: Payments/Onboarding service publishes an event.  
2. **Incentivize Consumer (IC)** reads the event and queries CMS for relevant campaign metadata/status.  
3. **Rule Engine** evaluates event context & cohort membership.  
4. **Limit Service** checks caps; if passed, record spend.  
5. **IC** stores the generated `Incentive` in DB and publishes `IncentiveGenerated`.  
6. **DCM** receives the message and pushes NRT notification to the client (<500 ms goal).  
7. **CDC → Analytics Bay**: Postgres WAL streams to Analytics Bay for offline analysis.  
8. **CDC → Redis Agg Pipeline**: WAL streams to Redis-based aggregation layer to refresh user and tenant limit counters.

### QUEST variant
Consumer keeps per-user campaign progress in Redis. On final required action, same steps 4-6 are executed.

---
## 6. Functional Requirements Mapping
| # | Requirement | Design Artefact |
|---|-------------|-----------------|
| F1 | Tenant onboarding & limit definition | Tenant API + Limit Service tables |
| F2 | Campaign CRUD | CMS, Postgres, OpenAPI spec |
| F3 | Cohort management | Cohort Service + Redis/Bloom storage |
| F4 | SIMPLE vs QUEST rules | Rule Engine state machine, progress store |
| F5 | NRT notification | DCM + IncentiveGenerated topic, <500 ms path |

---
## 7. Non-Functional Requirements & Design Decisions
| NFR | Approach |
|------|----------|
| Latency 300-500 ms | Local caches in consumer, async notifications, in-memory rule eval |
| Availability > Consistency | Eventual state sync; duplicate suppression via idempotent incentive IDs |
| Large cohorts | Pre-materialised sets, sharded Redis, bitmap indices |
| Impulsive shocks | Kafka back-pressure & consumer auto-scaling (K8s HPA), bounded thread pools |
| Celebrity problem | Hot-partition detection → temporary replica consumers; out-of-order aggregation |

---
## 8. Technology Stack & Deployment
| Layer | Tech |
|-------|------|
| Language | Kotlin + Spring Boot (synchronous) |
| Build | Gradle + OpenAPI + Jooq codegen |
| Stream | Apache Kafka (3× broker, 3× zookeeper) |
| DB | PostgreSQL (campaigns, incentives), Redis (limits, cohorts) |
| Infra | Kubernetes, Helm charts, Istio for traffic, Prometheus + Grafana |
| CI/CD | GitHub Actions → ArgoCD |

Deployment topology: dedicated namespaces per tenant tier; horizontal scaling on consumer & DCM; multi-AZ Postgres cluster.

---
## 9. Hot Campaign Optimisation with Count-Min Sketch
High-volume campaigns ("hot" campaigns) dominate event traffic. To avoid excessive memory usage in Redis for user aggregation data, we will introduce a **Count-Min Sketch (CMS)** per tenant/time-window to approximate campaign hit counts. Detailed per-user aggregation keys will be created **only** for campaigns whose estimated count exceeds `HOT_THRESHOLD`.

### Why CMS?
1. **Memory-Efficient** – fixed O(w·d) space; 10 kB per tenant window suffices for 1 % error.
2. **Speed** – constant-time update/query; pipeline friendly in Redis Lua.
3. **No False Negatives** – counts are over-estimates → we never miss a hot campaign.
4. **Simple Deployment** – implemented with existing Redis; no extra infra.

### Flow Summary
1. IC increments `cms:{tenantId}:{window}` with campaignId hash.
2. Estimate ≥ threshold? mark as hot (`SADD hot:set:{tenantId}`).
3. Only for hot campaigns store `agg:{campaignId}:{userId}` hash.
4. Cold campaigns aggregate in Postgres only.

### Configuration
* Window size: 10 min (bucketed).
* Threshold: 5 000 events per window (configurable).
* Error bounds: ε=0.01, δ=0.01 (w=272, d=5).

### Metrics
* `campaign_hot_ratio`, `cms_false_positive_rate`, memory delta pre/post.

---
## 10. Open Questions / Next Steps
- Rule DSL syntax & complexity (simple JSON vs Drools).  
- Cohort recomputation cadence & SLA.  
- Idempotency and deduplication strategy for at-least-once event delivery.  
- Incentive expiration & reversal flows.

---
*Author: Cascade AI – 2025-06-27*

# ProjectBreakDown.md
[CREATED 2025-06-27]

## End-to-End Build Plan for the Incentivization Platform
This document lists the concrete, ordered steps (milestones & tasks) required to deliver the Incentivization Platform as specified in ProjectOverview.md & ProjectLLD.md.

---
### Phase 0 – Foundations
1. **Repository bootstrap**  
   • Gradle Kotlin DSL, Spring Boot 3.5 skeleton  
   • Dependency management (Kotlin, Flyway, jOOQ, OpenAPI)
2. **Rule & Docs baseline**  
   • Memory-bank initialized  
   • HLD / LLD drafts committed

### Phase 1 – Data Layer
3. **Schema v1** (Flyway `V1__init.sql`)  
   • Enums `campaign_type`, `campaign_status`, `incentive_type`  
   • Tables `campaign`, `incentive`, `user_aggregation`  
   • Indexes & FKs
4. **jOOQ code generation**  
   • Configure generator task  
   • Verify generated DAOs & records compile

### Phase 2 – API Contract
5. **OpenAPI spec** (`api.yaml`)  
   • Health endpoint  
   • `POST /events` ingestion  
   • Future stubs (tenant, campaign, limits) placeholder only
6. **Regenerate interfaces** via Gradle task `openApiGenerate`

### Phase 3 – Controller Layer
7. **Implement generated interfaces**  
   • `EventsController` returns 202  
   • Unit tests with MockMvc

### Phase 4 – Service Layer
8. **EventIngestionService**  
   • Validate ActionEvent  
   • Publish to Kafka (`action-events` topic)
9. **CampaignEvaluationService** (IC)  
   • Consume `action-events`  
   • Load active campaigns via CMS REST  
   • Evaluate SIMPLE/QUEST rules (unordered)  
   • Persist `user_aggregation`, `incentive`
10. **LimitCheckService**  
    • Lua script in Redis  
    • Reject over-limit incentives

### Phase 5 – Messaging & DCM
11. **Kafka topics & serialization** (Avro/JSON-Schema)  
12. **DCM WebSocket gateway**  
    • Consume `incentive-generated`  
    • Push to clients

### Phase 6 – (reserved) External analytics integration – out of current scope

### Phase 7 – CMS & Cohort Service
15. **Campaign CRUD APIs** (CMS)  
16. **Cohort rule DSL parser & recomputation scheduler**

### Phase 8 – Observability & Ops
17. **Logging & tracing** (SLF4J + Correlation IDs)  
18. **Metrics** (Micrometer + Prometheus/Grafana)  
19. **Health & readiness probes**

### Phase 9 – CI/CD & Deployment
20. **GitHub Actions pipeline**  
    • Build, test, Flyway migrate, Docker image  
21. **Docker Compose / Kubernetes manifests** for infra (Postgres, Kafka, Redis, Debezium, Grafana)  
22. **Blue-green deployment strategy**

### Phase 10 – Hardening & UAT
23. **Load & chaos testing** (k6, ToxiProxy)  
24. **Security audit** (OWASP dependency-check, Secrets scan)  
25. **User Acceptance Testing** – sign-off

### Phase 11 – Launch & Post-Launch
26. **Production rollout**  
27. **Monitoring dashboards & alerting**  
28. **Post-launch KPI tracking & feedback loop**

---
*Author: Cascade AI – 2025-06-27*

# decisionLog.md

[2025-06-25 00:38:51] Upgraded OpenAPI Generator to 7.0.1 and generated Kotlin interfaces
[2025-06-25 01:36:53] Added spring-boot-starter-web, validation, jackson-module-kotlin dependencies to support generated controllers
[2025-06-25 01:44:54] Switched secondary storage from MongoDB to DynamoDB across project docs and techContext

[2025-06-24 23:59:56] - Moved ProjectOverview.md out of memory-bank to project root to serve as shared high-level documentation
[CREATED 2025-06-23]

Purpose: Record significant architectural and technical decisions.

---
[2025-06-25 03:24:40] - [Aligned all jOOQ artifacts and Gradle plugin to version 3.19.23 to resolve AbstractMethodError during code generation]
[2025-06-27 17:49:30] - [Refined Incentivization Platform HLD: IC publishes incentives to DCM which immediately pushes to client; IC DB streams CDC to Analytics Bay and Redis Limits aggregation; CMS remains query-only]
[2025-06-27 18:08:02] - [Aligned Gradle build with Flyway, jOOQ, OpenAPI, removed openapiGenerator() dependency call, fixed bracket syntax]
[2025-06-28 09:30:46] - [Removed CmsClient; services now call ICampaignRepository directly for campaign lookup, simplifying architecture and improving visibility]
[2025-06-28 11:25:00] - [Replaced free-form ruleJson with typed rules list and pluggable RuleEngine across domain and services; added ACTION_COUNT & COHORT handlers]
[2025-06-28 13:54:17] - [Adopted Count-Min Sketch in Redis for hot campaign detection and selective aggregation to save memory and increase throughput]
[2025-06-28 15:50:00] - [Reduced hotCampaign.threshold default to 50 for dev; standardised DB access via docker exec psql; ensured tenant-aligned event simulation; documented CMS monitoring workflow]

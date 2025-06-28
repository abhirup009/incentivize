# Lessons Learned – 2025-06-28

## Context
While integrating Count-Min Sketch (CMS) hot-campaign detection and validating metrics locally, we encountered several issues that obscured results. The following notes capture the problems, root-causes and permanent fixes.

---

### 1. Tenant ID Mismatch
* **Symptom** – `cms.hotCampaigns` gauge stayed at 0 despite high event volume.
* **Root Cause** – Helper endpoint published events for tenant `0000…0001`, but all campaigns belonged to tenant `1111…1111` – therefore repository lookup returned no campaigns and CMS counters never incremented.
* **Fix** – Always fetch the active tenant ID directly from Postgres (via `docker exec postgres psql …`) before publishing events.

### 2. Wrong DB Column Names in Rule Seeding
* **Symptom** – SQL script updated `rules` column which does not exist; campaigns remained rule-less.
* **Fix** – Corrected script to update `rule_json` and `campaign_id` columns.

### 3. Local `psql` Assumption
* **Symptom** – Automation scripts failed (`psql: command not found`).
* **Fix** – Standardised on `docker exec postgres psql …` for all DB interactions per user preference. Added to tech context and memory-bank.

### 4. Threshold Too High
* **Symptom** – Even with correct tenant, gauge stayed 0 – threshold default 5 000.
* **Fix** – Reduced `hotCampaign.threshold` default to 50 via `@Value` in `HotCampaignService`.

### 5. Campaign Over-counting
* **Observation** – Gauge jumped to 20, not 5, because `findActiveCampaigns()` does not filter by `actionCode` yet.
* **Next Step** – Add action-level filtering in repository layer to align CMS counts with rule semantics.

---

## Updated Best Practices
1. **Always verify tenant-campaign alignment** when simulating events.
2. Use **Docker-executed psql** for DB scripts & queries.
3. Keep **CMS hot threshold** low in dev to surface metrics quickly; externalise via `application.yaml`.
4. Ensure **rule seeding scripts** target correct columns.
5. Incorporate **action filtering** in repository queries.

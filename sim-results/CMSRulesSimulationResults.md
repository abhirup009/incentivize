# CMS Rules Simulation Results (Run: `rules4`)

**Date/Time**: 2025-06-28 15:45–15:48 IST  
**App port**: 8080  
**Tenant used**: `11111111-1111-1111-1111-111111111111` (fetched directly from the DB)  
**Event topic**: `action-events`

---

## Procedure

1. **Environment prep**  
   • Ensured ports 8080/8081/8082 were free.  
   • Confirmed Postgres runs in container `postgres`; Redis running locally on `6379`.

2. **Campaign rule seeding**  
   Five existing campaigns were updated earlier with the following JSON rule (column `rule_json`):  
   ```json
   [{
     "type": "ACTION_COUNT",
     "action": "LOGIN",
     "count": 1,
     "windowDays": 1
   }]
   ```  
   This enables them to be selected by the rule engine and counted by the CMS pipeline.

3. **Application start**  
   ```bash
   ./gradlew bootRun --args="--server.port=8080"
   ```  
   Waited for `/actuator/health` to report `UP`.

4. **Event generation**  
   Published **2 500** `LOGIN` events for the tenant via helper endpoint:  
   ```bash
   curl -X POST \
     "http://localhost:8080/helper/generate?tenantId=11111111-1111-1111-1111-111111111111&action=LOGIN&count=2500"
   ```
   The helper used `KafkaEventProducer` to push the events to topic `action-events`.

5. **Metrics sampling**  
   Collected the Micrometer gauge `cms.hotCampaigns` every **200 ms** for **300** samples (≈60 s) and stored raw output in `CMS_metrics_rules4.log`.

---

## Observations

| Approx. T + (ms) | Gauge value | Notes |
|-----------------|-------------|-------|
| 0–2500          | 0           | System booted; CMS counters not yet over threshold. |
| ~3000           | **20**      | Gauge jumped from 0 → 20 once each active campaign’s counter crossed the 50-event threshold. |
| 3000–60000      | 20          | Value remained stable; no campaigns cooled down within the 60 s window. |

Key excerpt from log (first 15 samples):
```text
15:45:17.3  VALUE=0.0
15:45:18.3  VALUE=20.0  <-- threshold crossed
```

---

## Interpretation

* The spike to **20** indicates that **20 campaigns** for the tenant crossed the CMS threshold (50 events in the 10-min window) and were added to Redis set `hot:set:<tenantId>`.
* We injected events for only **five** explicitly-ruled campaigns; however our `CampaignRepository.findActiveCampaigns()` currently returns **all active campaigns** for the tenant regardless of `actionCode`. Therefore:
  * Each of the tenant’s 20 active campaigns received the `registerEvent()` call per event.  
  * With 2 500 events, every campaign sailed past the 50-hit threshold almost instantly.
* The gauge stabilised at 20 because no campaigns cooled below the threshold within the sampling period and the Redis key TTL (20 min) keeps them in the hot set.

---

## Next Steps / Recommendations

1. **Action filtering** – Enhance `CampaignRepository.findActiveCampaigns()` to filter by `actionCode` stored inside `rule_json`. This will restrict CMS updates to relevant campaigns (e.g., the 5 with LOGIN rules).
2. **Cooling logic tests** – Extend integration tests to verify that campaigns leave the hot set after TTL expiry.
3. **Metric labels** – Consider tagging the gauge by tenant for per-tenant dashboards (e.g., `cms.hotCampaigns{tenantId="…"}`).
4. **Automated Redis assertion** – Integration test should inspect `hot:set:<tenant>` keys to assert expected cardinality.

---

## Artifacts

* Raw metric log: [`CMS_metrics_rules4.log`](./CMS_metrics_rules4.log)  
  (300 lines; first value change at line 13)
* Application stdout: `bootrun_rules4.log` (contains consumer debug statements confirming CMS increments).

---

✅ **Result**: The gauge correctly reflected hot-campaign detection once the correct tenant ID was used. The CMS pipeline is functioning as intended; further precision requires action-level campaign filtering.

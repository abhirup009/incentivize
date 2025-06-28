# Count-Min Sketch Hot-Campaign Simulation

This document describes the exact steps and scripts used to reproduce the **`cms.hotCampaigns`** metric growth locally.

---

## 1. Pre-requisites

* Docker running Postgres, Redis, Kafka – `docker-compose up -d`
* Application built & container services healthy
* **Ports** 8080/8081/8082 **free** (helper script kills any process on those ports).

---

## 2. Rule Seeding Script

`./scripts/add_rules_to_5_campaigns.sql`
```sql
-- Update first 5 campaigns to have a basic ACTION_COUNT rule
WITH to_update AS (
    SELECT campaign_id FROM campaign ORDER BY campaign_id LIMIT 5
)
UPDATE campaign
SET rule_json = '[{"type":"ACTION_COUNT","action":"LOGIN","count":1,"windowDays":1}]'::jsonb
WHERE campaign_id IN (SELECT campaign_id FROM to_update);
```

Apply via dockerised psql:
```bash
cat scripts/add_rules_to_5_campaigns.sql | \
  docker exec -i postgres psql -U incentivize -d incentivize
```

---

## 3. Simulation Bash Script

`simulate_cms.sh`
```bash
#!/usr/bin/env bash
set -euo pipefail
PORT=8080
LOG=CMS_metrics_$(date +%Y%m%d_%H%M%S).log
# kill port if occupied
pid=$(lsof -ti tcp:$PORT || true)
[ -n "$pid" ] && kill -9 $pid

# tenant id
TENANT=$(docker exec -i postgres psql -U incentivize -d incentivize -At -c "select tenant_id from campaign limit 1")

echo "Using tenant $TENANT"

# start app
./gradlew bootRun --args="--server.port=$PORT" &
APP_PID=$!
trap 'kill -9 $APP_PID' EXIT
# wait for health
until curl -s http://localhost:$PORT/actuator/health | grep -q UP; do sleep 1; done

# publish events (2500 LOGIN)
curl -s -X POST \
  "http://localhost:$PORT/helper/generate?tenantId=$TENANT&action=LOGIN&count=2500" >/dev/null

# sample metric 300× every 200ms
touch $LOG
echo "timestamp value" > $LOG
for i in {1..300}; do
  ts=$(date +%H:%M:%S.%3N)
  val=$(curl -s http://localhost:$PORT/actuator/metrics/cms.hotCampaigns | jq -r '.measurements[0].value')
  echo "$ts $val" >> $LOG
  sleep 0.2
done

echo "Log written to $LOG"
```

Make executable and run:
```bash
chmod +x simulate_cms.sh
./simulate_cms.sh
```

---

## 4. Expected Output

* Gauge remains `0` until each campaign exceeds the **50-event** threshold.
* Gauge jumps to `20` (all active campaigns) and stays stable for the sampling window.
* Raw log lines look like:
  ```text
  15:45:17.300 0.0
  15:45:18.300 20.0
  15:45:18.500 20.0
  ```

---

## 5. Cleanup

```bash
kill -9 $(lsof -ti tcp:8080) || true
```

---

## Troubleshooting Tips

| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| Gauge stays **0** | Mismatched tenant ID | Fetch tenant ID directly from DB and publish events with it |
| Gauge climbs slowly | `hotCampaign.threshold` too high | Lower in `application.yaml` or via env var |
| Gauge too high | `findActiveCampaigns()` not filtering by `actionCode` | Implement action-level filter |

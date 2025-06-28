# Local Load Test Results (2025-06-28)

Test Parameters
---------------
* Kafka broker: `localhost:9092`
* Events published: **1 500** `ActionEvent`s via `/helper/generate`
* Tenant ID: `11111111-1111-1111-1111-111111111111`
* Action: `LOGIN`

Metrics
-------
| Metric | Before | After | Delta |
|--------|--------|-------|-------|
| Redis key count (`DBSIZE`) | 0 | 9 000 | +9 000 |
| Incentive rows in PostgreSQL | 0 | 30 000 | +30 000 |
| Publish (HTTP) time | – | **0.153 s** | – |
| Consumer processing time window observed | – | ~10 s | – |
| Estimated consumer throughput | – | **≈150 events/s** | – |

Observations
------------
* Producer published 1 500 JSON-encoded events in ~0.15 s – negligible overhead.
* The consumer processed and persisted all events within roughly 10 s, averaging ~150 events/s.
* Redis key growth (9000) aligns with 6 keys per event (cohort sets, limit counters, etc.).
* Database inserts (30 000 incentives) indicate multiple incentives per event as expected from campaign evaluation.
* No errors were logged by Spring Kafka; offset committed successfully.

Next Steps
----------
* Extend stress test to ≥100 k events to validate throughput and resource usage.
* Add Grafana/Prometheus metrics for detailed latency histograms.

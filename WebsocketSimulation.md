# WebSocket Simulation Results

**Date:** 2025-06-28

## Scenario
Integration test `WebSocketIntegrationTest` subscribes to `/topic/incentives`, then publishes an `IncentiveMessageDTO` and awaits reception.

## Result
❌ **FAILED** – Awaitility timed-out (20 s) waiting for the message. The STOMP client never received a payload.

```
org.awaitility.core.ConditionTimeoutException: Condition with "queue.peek() != null" was not fulfilled within 20 seconds.
```

## Observations
* WebSocket handshake succeeded; session established.
* Message sent via `SimpMessagingTemplate` but broker delivery to the client did not occur in test environment.
* Possible reasons:
  * SimpleBroker delivers on a different thread after test context already shutting down.
  * In-memory broker may require asynchronous scheduling delay beyond 20 s.

## Next Steps
1. Enable Spring’s STOMP frame logging (`logging.level.org.springframework.messaging=TRACE`) to verify broker routing during test.
2. Consider using `@WithMockUser` or `@SpringBootTest(webEnvironment = RANDOM_PORT, classes = …, properties = {"spring.main.allow-circular-references=true"})` if circular refs are pruned.
3. Alternatively, keep using the echo endpoint (`/app/test`) and send through the WebSocket session rather than the broker.

---

## Live End-to-End Verification (2025-06-28)

A plain WebSocket client (`wscat`) stayed connected while the helper API generated events that flowed through Kafka → consumers → CampaignEvaluationService → `IncentivesWebSocketHandler`.

### Sample messages received
```json
{"id":"68bf9ac4-8cf3-4927-83bb-f6ff2b2885bd","userId":"ef1f898f-bee0-42c2-b562-187572c4ca1b","type":"CASHBACK","amount":10.0,"currency":"USD","campaignName":"VIP Exclusive"}
{"id":"e360b65d-7ec8-4f62-9e7f-6bf96481a319","userId":"ef1f898f-bee0-42c2-b562-187572c4ca1b","type":"CASHBACK","amount":10.0,"currency":"USD","campaignName":"Flash Deal"}
{"id":"949b9ce7-9f0c-4243-a936-d34028b6239a","userId":"ef1f898f-bee0-42c2-b562-187572c4ca1b","type":"CASHBACK","amount":10.0,"currency":"USD","campaignName":"Festival Fiesta"}
... (total 15 messages)
```

### Outcome
✅ **SUCCESS** – Every helper-generated event produced an incentive JSON frame that the WebSocket client received in real time.

---

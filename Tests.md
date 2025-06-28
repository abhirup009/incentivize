# Test Cases Documentation

_Last updated: 2025-06-28 12:08 IST_

This document lists the unit tests executed for core services of the Incentivize platform.

## 1. LimitServiceTest
| Test | Purpose | Expected Behaviour |
|------|---------|--------------------|
| `create should persist and return limit` | Validate that `LimitService.create` stores a new limit and returns the persisted object | Returned `Limit` matches request fields; limit appears in `list()` result. |
| `update should modify existing limit` | Ensure `LimitService.update` correctly updates cap & status | Updated `Limit` reflects new cap and status. |

## 2. CampaignServiceTest
| Test | Purpose | Expected Behaviour |
|------|---------|--------------------|
| `create should map DTO to domain and save` | Verify DTO-to-domain mapping and repository save invocation | Saved domain object fields match DTO; repository `save()` called once. |

## 3. CampaignEvaluationServiceTest
| Test | Purpose | Expected Behaviour |
|------|---------|--------------------|
| `processEvent should save incentive when limits allow and rules pass` | Happy-path event processing | Incentive repository `save()` called once with correct params. |
| `processEvent should skip when limits block` | Ensure events are blocked when limit exceeded | Incentive repository **not** called when limit cap is exceeded. |
| `processEvent should allow when limits within cap` | Verify incentives issued when counter below cap | Incentive repository called once. |
| `processEvent should skip when rule engine fails` | Validate that failed rule evaluation blocks incentive | Incentive repository **not** called. |
| `processEvent should save incentive when cohort rule passes` | Validate positive cohort rule path | Incentive repository called once. |

## 4. RuleEngineServiceTests
| Test | Purpose | Expected Behaviour |
|------|---------|--------------------|
| `evaluateAll returns true when all rules pass` | Confirm engine returns true if every rule passes | Returns `true`. |
| `evaluateAll returns false when any rule fails` | Confirm engine stops processing on failure | Returns `false`. |

## 5. IncetivizeApplicationTests (Disabled)
`contextLoads` integration test is disabled during unit runs because it requires external PostgreSQL and Redis instances. It can be enabled in integration environments with proper containers.

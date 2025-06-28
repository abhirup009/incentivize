package com.example.service

import com.example.generated.model.ActionEvent
import com.example.repository.ICampaignRepository
import com.example.generated.model.Limit
import java.time.Duration
import java.util.UUID

import com.example.repository.IIncentiveRepository
import com.example.repository.IUserAggregationRepository
import com.example.jooq.generated.enums.IncentiveType
import com.example.redis.RedisClient
import com.example.domain.Campaign
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Core domain service that processes events and decides whether an incentive should be generated.
 * Redis limit checks and actual persistence are skipped for now.
 */
@Service
class CampaignEvaluationService(
    private val campaignRepository: ICampaignRepository,
    private val incentiveRepository: IIncentiveRepository,
    private val aggregationRepository: IUserAggregationRepository,
    private val limitService: LimitService,
    private val ruleEngine: com.example.rules.RuleEngineService,
    private val redis: RedisClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun processEvent(event: ActionEvent) {
        val tenantId = event.tenantId
        val campaigns = campaignRepository.findActiveCampaigns(tenantId, event.actionCode)
        if (campaigns.isEmpty()) {
            log.debug("No active campaigns for action {} tenant {}", event.actionCode, tenantId)
            return
        }

        campaigns.forEach { campaign ->
            // evaluate limits for this action first
            if (!allowedByLimits(event)) {
                log.info("[LIMIT] Incentive blocked for user {} action {} due to cap", event.userId, event.actionCode)
                return
            }

            // evaluate campaign rules
            val rulesPass = ruleEngine.evaluateAll(event, campaign.id, campaign.rules ?: emptyList())
            if (!rulesPass) {
                log.info("[RULE] Campaign rules not satisfied for user {} campaign {}", event.userId, campaign.name)
                return
            }

            when (campaign.type) {
                Campaign.CampaignType.SIMPLE -> handleSimple(event, campaign)
                Campaign.CampaignType.QUEST -> handleQuest(event, campaign) // quest logic simplified
            }
        }
    }

    private fun handleSimple(event: ActionEvent, campaign: Campaign) {
        // persist incentive row
        val incentiveId = UUID.randomUUID()
        incentiveRepository.save(
            incentiveId = incentiveId,
            type = IncentiveType.CASHBACK.name,
            currency = "USD",
            amount = 10.0
        )
        log.info("[SIMPLE] Incentive {} generated for user {} action {} campaign {}", incentiveId, event.userId, event.actionCode, campaign.name)
    }

    private fun handleQuest(event: ActionEvent, campaign: Campaign) {
        // Aggregation key pattern: quest:{campaignId}:{userId}
        val key = "quest:${campaign.id}:${event.userId}"
        redis.sadd(key, event.actionCode)
        // expire in 30 days (campaign typical window)
        redis.expire(key, Duration.ofDays(30))

        val actions = redis.smembers(key)
        if (actions.containsAll(campaign.requiredActions)) {
            // quest completed – persist incentive
            val incentiveId = UUID.randomUUID()
            incentiveRepository.save(incentiveId, IncentiveType.CASHBACK.name, "USD", 50.0)
            // update aggregation table as completed
            aggregationRepository.save(UUID.randomUUID(), event.userId, campaign.id, "COMPLETED")
            log.info("[QUEST] Completed for user {} campaign {}", event.userId, campaign.name)
        }

        // persist individual action aggregation row as before (sync fallback)
        val aggId = UUID.randomUUID()
        aggregationRepository.save(
            id = aggId,
            userId = event.userId,
            campaignId = campaign.id,
            action = event.actionCode
        )
        log.info("[QUEST] Aggregated action {}, aggId={}", event.actionCode, aggId)
    }

    // ----- Limit evaluation helper -----
    private fun allowedByLimits(event: ActionEvent): Boolean {
        val tenantLimits: List<Limit> = limitService.list(event.tenantId)
        val relevant = tenantLimits.filter { it.code == event.actionCode }
        if (relevant.isEmpty()) return true

        for (limit in relevant) {
            if (limit.status.name != "aCTIVE") continue
            val key = "limit:${limit.id}:${event.userId}"
            val ttl = when (limit.window.uppercase()) {
                "DAILY" -> Duration.ofDays(1)
                "MONTHLY" -> Duration.ofDays(30)
                else -> Duration.ofDays(365)
            }
            val count = redis.incr(key, ttl)
            if (count > limit.cap) return false
        }
        return true
    }
}

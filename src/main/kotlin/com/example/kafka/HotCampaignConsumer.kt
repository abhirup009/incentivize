package com.example.kafka

import com.example.generated.model.ActionEvent
import com.example.hot.HotCampaignService
import com.example.repository.ICampaignRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Lightweight Kafka consumer whose sole purpose is to keep the Count-Min Sketch (CMS)
 * counters up-to-date for hot-campaign detection. It executes extremely cheap work:
 *  1. Lookup active campaigns for the event's action (cached by repository impl).
 *  2. For each candidate campaign increment the CMS counter via [HotCampaignService].
 *
 * This consumer is fully independent of the main [ActionEventConsumer] that performs
 * full rule evaluation. Using a dedicated consumer ensures CMS updates are never blocked
 * by downstream DB/Redis writes in the main path and can be horizontally scaled if
 * needed without impacting incentive latency.
 */
@Component
class HotCampaignConsumer(
    private val campaignRepository: ICampaignRepository,
    private val hotCampaignService: HotCampaignService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["action-events"], groupId = "incentivize-cmsketch")
    fun consume(event: ActionEvent) {
        val campaigns = campaignRepository.findActiveCampaigns(event.tenantId, event.actionCode)
        campaigns.forEach { campaign ->
            hotCampaignService.registerEvent(event.tenantId, campaign.id)
        }
        log.debug("[CMS] Updated counts for tenant={} action={} campaigns={}", event.tenantId, event.actionCode, campaigns.size)
    }
}

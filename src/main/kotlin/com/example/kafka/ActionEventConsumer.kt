package com.example.kafka

import com.example.generated.model.ActionEvent
import com.example.service.CampaignEvaluationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Consumes ActionEvent messages from Kafka and delegates to [CampaignEvaluationService]
 * for limit and rule evaluation.
 */
@Component
class ActionEventConsumer(
    private val evaluationService: CampaignEvaluationService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["action-events"])
    fun consume(event: ActionEvent) {
        log.debug("Kafka event received tenant={} user={} action={}", event.tenantId, event.userId, event.actionCode)
        evaluationService.processEvent(event)
    }
}

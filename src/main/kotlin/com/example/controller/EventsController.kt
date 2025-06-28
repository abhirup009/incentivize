package com.example.controller

import com.example.generated.api.EventsApi
import com.example.generated.model.ActionEvent
import com.example.service.CampaignEvaluationService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class EventsController(
    private val evaluationService: CampaignEvaluationService
) : EventsApi {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishEvent(actionEvent: ActionEvent): ResponseEntity<Unit> {
        log.info("Received ActionEvent for tenant={} user={} action={}", actionEvent.tenantId, actionEvent.userId, actionEvent.actionCode)
        evaluationService.processEvent(actionEvent)
        return ResponseEntity.accepted().build()
    }
}

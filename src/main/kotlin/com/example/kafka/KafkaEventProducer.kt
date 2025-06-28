package com.example.kafka

import com.example.generated.model.ActionEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class KafkaEventProducer(
    private val template: KafkaTemplate<String, String>,
    private val mapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val topic = "action-events"

    fun send(event: ActionEvent) {
        val json = mapper.writeValueAsString(event)
        template.send(topic, event.userId.toString(), json)
        log.debug("Published event {} to Kafka", event)
    }

    /** Helper to generate and publish multiple dummy events */
    fun generateAndSend(count: Int, tenantId: UUID, action: String): Int {
        repeat(count) {
            val evt = ActionEvent(
                tenantId = tenantId,
                userId = UUID.randomUUID(),
                actionCode = action,
                eventTimestamp = OffsetDateTime.now()
            )
            send(evt)
        }
        return count
    }
}

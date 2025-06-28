package com.example.incetivize.helper

import com.example.kafka.KafkaEventProducer
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/helper")
class HelperController(private val producer: KafkaEventProducer) {

    /**
     * Generates and publishes `count` ActionEvent messages to Kafka for the given tenant and action code.
     * Example: POST /helper/generate?tenantId=...&action=LOGIN&count=1000
     */
    @PostMapping("/generate")
    fun generate(
        @RequestParam tenantId: UUID,
        @RequestParam action: String,
        @RequestParam(required = false, defaultValue = "1000") count: Int
    ): ResponseEntity<String> {
        val sent = producer.generateAndSend(count, tenantId, action)
        return ResponseEntity.ok("Sent $sent events to Kafka topic action-events")
    }
}

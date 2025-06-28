package com.example.controller

import com.example.dto.IncentiveMessageDTO
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

/**
 * A minimal STOMP controller to satisfy routing separation for incentive events.
 *
 * Although the application currently only pushes incentives from the backend (server-side),
 * defining a dedicated controller keeps WebSocket responsibilities isolated from REST controllers
 * and allows future interactive features (e.g., client acknowledgements) without touching
 * business services.
 */
@Controller
class IncentivesWebSocketController {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Echo endpoint for test clients. A client can send a STOMP message to `/app/test` and
     * receive the same payload back via `/topic/incentives`. This helps verify connectivity.
     * Remove or modify as needed.
     */
    @MessageMapping("/test")
    @SendTo("/topic/incentives")
    fun testEcho(msg: IncentiveMessageDTO): IncentiveMessageDTO {
        log.debug("Echoing test message over WebSocket: {}", msg)
        return msg
    }
}

package com.example.websocket

import com.example.dto.IncentiveMessageDTO
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Simple WebSocket handler that keeps track of open sessions and broadcasts
 * [IncentiveMessageDTO] as JSON text frames to every connected client.
 */
@Component
class IncentivesWebSocketHandler(
    private val mapper: ObjectMapper
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions: MutableSet<WebSocketSession> = CopyOnWriteArraySet()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        log.debug("WebSocket connected: {}", session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        log.debug("WebSocket closed: {} status={}", session.id, status)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.warn("WebSocket error for session {}", session.id, exception)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Simple echo for connectivity checks
        session.sendMessage(message)
    }

    /** Broadcasts the given DTO to all currently open sessions as a JSON text frame. */
    fun broadcast(dto: IncentiveMessageDTO) {
        val json = mapper.writeValueAsString(dto)
        val msg = TextMessage(json)
        sessions.filter { it.isOpen }.forEach {
            try {
                it.sendMessage(msg)
            } catch (ex: Exception) {
                log.warn("Failed to send message to session {}", it.id, ex)
            }
        }
    }
}

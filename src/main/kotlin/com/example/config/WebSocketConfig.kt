package com.example.config

import org.springframework.context.annotation.Configuration

import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * Global WebSocket/STOMP configuration.
 *
 * Clients establish the handshake at `/ws` (plain WebSocket or SockJS) and then
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(private val handler: com.example.websocket.IncentivesWebSocketHandler) : org.springframework.web.socket.config.annotation.WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, "/ws").setAllowedOriginPatterns("*")
        registry.addHandler(handler, "/ws").setAllowedOriginPatterns("*").withSockJS()
    }
}

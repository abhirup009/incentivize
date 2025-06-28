package com.example.websocket

import com.example.dto.IncentiveMessageDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimpleWebSocketIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var handler: IncentivesWebSocketHandler

    private val mapper = jacksonObjectMapper()

    @Test
    fun `broadcast reaches simple websocket client`() {
        val queue: ArrayBlockingQueue<IncentiveMessageDTO> = ArrayBlockingQueue(1)

        val client = StandardWebSocketClient()
        val handler = object : org.springframework.web.socket.handler.TextWebSocketHandler() {
            override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                val dto = mapper.readValue(message.payload, IncentiveMessageDTO::class.java)
                queue.offer(dto)
            }
        }
        val session: WebSocketSession = client.doHandshake(handler, org.springframework.web.socket.WebSocketHttpHeaders(), URI("ws://localhost:$port/ws"))
            .completable()
            .get(5, TimeUnit.SECONDS)

        // Ensure socket is open
        Thread.sleep(200)

        val dto = IncentiveMessageDTO(
            java.util.UUID.randomUUID(),
            java.util.UUID.randomUUID(),
            "CASHBACK",
            5.0,
            "USD",
            "test-campaign"
        )
        this.handler.broadcast(dto)

        await.atMost(5, TimeUnit.SECONDS).until { queue.peek() != null }
        val received = queue.poll()
        assertEquals(dto.id, received.id)
        assertEquals(dto.amount, received.amount)

        session.close()
    }
}

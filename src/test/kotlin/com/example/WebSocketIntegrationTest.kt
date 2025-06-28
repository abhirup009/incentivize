package com.example.websocket

import com.example.dto.IncentiveMessageDTO
import org.awaitility.Awaitility.await
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(
    classes = [WebSocketTestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    ]
)
@ExtendWith(SpringExtension::class)
class WebSocketIntegrationTest {

    @TestConfiguration
    class Stubs {
        @Bean
        fun campaignService() = io.mockk.mockk<com.example.service.CampaignService>(relaxed = true)
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mapper: com.fasterxml.jackson.databind.ObjectMapper

    @Autowired
    private lateinit var messagingTemplate: SimpMessagingTemplate

        @Test
    fun `echo message over websocket`() {
        val queue: ArrayBlockingQueue<IncentiveMessageDTO> = ArrayBlockingQueue(1)

        val stompClient = WebSocketStompClient(StandardWebSocketClient())
        stompClient.messageConverter = MappingJackson2MessageConverter()

        val url = "ws://localhost:$port/ws"
        val session: StompSession = stompClient
            .connectAsync(url, object : StompSessionHandlerAdapter() {})
            .get(3, TimeUnit.SECONDS)

        session.subscribe("/topic/incentives", object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type = IncentiveMessageDTO::class.java
            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                queue.offer(payload as IncentiveMessageDTO)
            }
        })

        Thread.sleep(1000) // ensure subscription is fully registered

        val testMsg = IncentiveMessageDTO(UUID.randomUUID(), UUID.randomUUID(), "TEST", 0.0, "USD", "dummy")
        messagingTemplate.convertAndSend("/topic/incentives", testMsg)

        await().atMost(20, TimeUnit.SECONDS).until { queue.peek() != null }
        val received = queue.poll()
        assertEquals(testMsg.id, received.id)
        assertEquals(testMsg.type, received.type)
    }
}

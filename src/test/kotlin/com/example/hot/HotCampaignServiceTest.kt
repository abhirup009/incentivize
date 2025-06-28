package com.example.hot

import com.example.redis.RedisClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

class HotCampaignServiceTest {

    private lateinit var redis: RedisClient
    private lateinit var service: HotCampaignService
    private lateinit var meterRegistry: SimpleMeterRegistry
    private val tenantId = UUID.randomUUID()
    private val campaignId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        redis = mockk(relaxed = true)
        meterRegistry = SimpleMeterRegistry()
        // threshold 3 for test brevity
        service = HotCampaignService(redis, meterRegistry, 3)
    }

    @Test
    fun `should mark campaign hot after threshold reached`() {
        every { redis.hincrBy(any(), any(), any()) } returnsMany listOf(1L, 2L, 3L)
        every { redis.sismember(any(), any()) } returnsMany listOf(false, true)
        every { redis.sadd(any(), any()) } returns Unit

        repeat(3) { service.registerEvent(tenantId, campaignId) }

        verify(exactly = 3) { redis.hincrBy(match { it.startsWith("cms") }, campaignId.toString(), 1L) }
        verify { redis.sadd(match { it.startsWith("hot:set") }, campaignId.toString()) }
        assertTrue(service.isHotCampaign(tenantId, campaignId))
    }

    @Test
    fun `should not mark campaign hot before threshold`() {
        every { redis.hincrBy(any(), any(), any()) } returns 1
        every { redis.sismember(any(), any()) } returns false

        service.registerEvent(tenantId, campaignId)

        assertFalse(service.isHotCampaign(tenantId, campaignId))
    }
}

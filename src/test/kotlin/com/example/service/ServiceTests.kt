package com.example.service

import com.example.domain.Campaign
import com.example.generated.model.Limit
import com.example.generated.model.LimitCreateRequest
import com.example.generated.model.LimitUpdateRequest
import com.example.jooq.generated.enums.IncentiveType
import com.example.redis.RedisClient
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test



import java.time.Duration
import java.time.OffsetDateTime
import java.util.*

// -------------------- LimitService tests ----------------------------
class LimitServiceTest {

    private lateinit var service: LimitService

    @BeforeEach
    fun setUp() { service = LimitService() }

    @Test
    fun `create should persist and return limit`() {
        val tenantId = UUID.randomUUID()
        val req = LimitCreateRequest(tenantId, "LOGIN", 5, "DAILY")
        val limit = service.create(req)

        assertEquals(req.tenantId, limit.tenantId)
        assertEquals(req.code, limit.code)
        assertEquals(req.cap, limit.cap)
        assertEquals(Limit.Status.aCTIVE, limit.status)
        assertTrue(service.list(tenantId).contains(limit))
    }

    @Test
    fun `update should modify existing limit`() {
        val tenantId = UUID.randomUUID()
        val createReq = LimitCreateRequest(tenantId, "PURCHASE", 3, "MONTHLY")
        val limit = service.create(createReq)

        val updReq = LimitUpdateRequest(cap = 10, status = LimitUpdateRequest.Status.pAUSED)
        val updated = service.update(limit.id, updReq)!!

        assertEquals(10, updated.cap)
        assertEquals(Limit.Status.pAUSED, updated.status)
    }
}

// -------------------- CampaignService tests ----------------------------

class CampaignServiceTest {

    private val repo = mockk<com.example.repository.ICampaignRepository>(relaxed = true)
    private lateinit var service: CampaignService

    @BeforeEach
    fun init() { service = CampaignService(repo) }

    @Test
    fun `create should map DTO to domain and save`() {
        val tenantId = UUID.randomUUID()
        val req = com.example.dto.CampaignCreateRequest(
            tenantId = tenantId,
            name = "Summer Promo",
            type = com.example.dto.CampaignType.SIMPLE,
            startAt = OffsetDateTime.now(),
            endAt = OffsetDateTime.now().plusDays(10),
            rules = null,
        )

        val dto = service.create(req)

        assertEquals(req.name, dto.name)
        assertEquals(req.type, dto.type)
        verify { repo.save(match { it.name == req.name && it.tenantId == tenantId }) }
    }
}

// -------------------- CampaignEvaluationService tests ----------------------------
class CampaignEvaluationServiceTest {

    private fun makeEvent(tenantId: UUID, action: String) = com.example.generated.model.ActionEvent(
        tenantId = tenantId,
        userId = UUID.randomUUID(),
        actionCode = action,
        eventTimestamp = OffsetDateTime.now(),
        attributes = mapOf<String, Any>()
    )

    private val campaignRepo = mockk<com.example.repository.ICampaignRepository>()
    private val incentiveRepo = mockk<com.example.repository.IIncentiveRepository>(relaxed = true)
    private val aggRepo = mockk<com.example.repository.IUserAggregationRepository>(relaxed = true)
    private val limitService = mockk<LimitService>()
    private val ruleEngine = mockk<com.example.rules.RuleEngineService>()
    private val redis = mockk<RedisClient>(relaxed = true)
    private val hotService = mockk<com.example.hot.HotCampaignService>(relaxed = true)

    private lateinit var service: CampaignEvaluationService

    @BeforeEach
    fun setup() {
        every { hotService.isHotCampaign(any(), any()) } returns false
        service = CampaignEvaluationService(campaignRepo, incentiveRepo, aggRepo, limitService, ruleEngine, hotService, redis)
    }

    @Test
    fun `processEvent should save incentive when limits allow and rules pass`() {
        val tenantId = UUID.randomUUID()
        val action = "LOGIN"
        val campaign = Campaign(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            name = "Login Bonus",
            type = Campaign.CampaignType.SIMPLE,
            rules = emptyList(),
            requiredActions = emptySet()
        )
        every { campaignRepo.findActiveCampaigns(tenantId, action) } returns listOf(campaign)
        every { limitService.list(tenantId) } returns emptyList()
        every { ruleEngine.evaluateAll(any(), campaign.id, any()) } returns true

        val event = com.example.generated.model.ActionEvent(
            tenantId = tenantId,
            userId = UUID.randomUUID(),
            actionCode = action,
            eventTimestamp = OffsetDateTime.now(),
            attributes = mapOf<String, Any>()
        )

        service.processEvent(event)

        verify { incentiveRepo.save(any(), IncentiveType.CASHBACK.name, "USD", 10.0) }
    }

    @Test
    fun `processEvent should allow when limits within cap`() {
        val tenantId = UUID.randomUUID()
        val action = "PURCHASE"
        val campaign = Campaign(UUID.randomUUID(), tenantId, "Purchase", Campaign.CampaignType.SIMPLE, rules = emptyList(), requiredActions = emptySet())
        every { campaignRepo.findActiveCampaigns(tenantId, action) } returns listOf(campaign)
        val limit = Limit(UUID.randomUUID(), tenantId, action, 3, "DAILY", Limit.Status.aCTIVE)
        every { limitService.list(tenantId) } returns listOf(limit)
        every { redis.incr(any(), any()) } returns 2 // within cap
        every { ruleEngine.evaluateAll(any(), campaign.id, any()) } returns true

        service.processEvent(makeEvent(tenantId, action))

        verify { incentiveRepo.save(any(), IncentiveType.CASHBACK.name, "USD", 10.0) }
    }

    @Test
    fun `processEvent should skip when limits block`() {
        val tenantId = UUID.randomUUID()
        val action = "PURCHASE"
        val campaign = Campaign(UUID.randomUUID(), tenantId, "Purchase", Campaign.CampaignType.SIMPLE, rules = emptyList(), requiredActions = emptySet())
        every { campaignRepo.findActiveCampaigns(tenantId, action) } returns listOf(campaign)
        // limit list returns a limit that will exceed after incr
        val limit = Limit(UUID.randomUUID(), tenantId, action, 1, "DAILY", Limit.Status.aCTIVE)
        every { limitService.list(tenantId) } returns listOf(limit)
        every { redis.incr(any(), any<Duration>()) } returns 2 // exceeds cap 1

        val event = com.example.generated.model.ActionEvent(tenantId, UUID.randomUUID(), action, OffsetDateTime.now(), null)

        service.processEvent(event)

        verify(exactly = 0) { incentiveRepo.save(any(), any(), any(), any()) }
    }

    @Test
    fun `processEvent should skip when rule engine fails`() {
        val tenantId = UUID.randomUUID()
        val action = "LOGIN"
        val campaign = Campaign(UUID.randomUUID(), tenantId, "Login Fail", Campaign.CampaignType.SIMPLE, rules = listOf(com.example.rules.Rule(com.example.rules.RuleType.COHORT)), requiredActions = emptySet())
        every { campaignRepo.findActiveCampaigns(tenantId, action) } returns listOf(campaign)
        every { limitService.list(tenantId) } returns emptyList()
        every { ruleEngine.evaluateAll(any(), campaign.id, any()) } returns false // rule fails

        service.processEvent(makeEvent(tenantId, action))

        verify(exactly = 0) { incentiveRepo.save(any(), any(), any(), any()) }
    }

    @Test
    fun `processEvent should save incentive when cohort rule passes`() {
        val tenantId = UUID.randomUUID()
        val action = "PURCHASE"
        val campaign = Campaign(UUID.randomUUID(), tenantId, "Purchase Cohort", Campaign.CampaignType.SIMPLE, rules = listOf(com.example.rules.Rule(com.example.rules.RuleType.COHORT)), requiredActions = emptySet())
        every { campaignRepo.findActiveCampaigns(tenantId, action) } returns listOf(campaign)
        every { limitService.list(tenantId) } returns emptyList()
        every { ruleEngine.evaluateAll(any(), campaign.id, any()) } returns true // rule passes

        service.processEvent(makeEvent(tenantId, action))

        verify { incentiveRepo.save(any(), IncentiveType.CASHBACK.name, "USD", 10.0) }
    }
}

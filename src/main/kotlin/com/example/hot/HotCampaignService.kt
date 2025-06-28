package com.example.hot

import com.example.redis.RedisClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import io.micrometer.core.instrument.MeterRegistry
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class HotCampaignService(
    private val redis: RedisClient,
    private val meterRegistry: MeterRegistry,
    @Value("\${hotCampaign.threshold:50}")
    private val threshold: Long,
) {

    init {
        // Gauge reporting total number of hot campaigns across all tenants
        meterRegistry.gauge("cms.hotCampaigns", this) { svc -> svc.totalHotCampaigns().toDouble() }
    }

    private fun windowBucket(epochSeconds: Long = Instant.now().epochSecond): Long = epochSeconds / 600 // 10-min window

    fun registerEvent(tenantId: UUID, campaignId: UUID) {
        val bucket = windowBucket()
        val cmsKey = "cms:${tenantId}:$bucket"
        val newCount = redis.hincrBy(cmsKey, campaignId.toString(), 1)
        // Ensure key expiry so old windows are cleaned up
        redis.expire(cmsKey, Duration.ofMinutes(10))
        if (newCount >= threshold) {
            val hotKey = "hot:set:${tenantId}"
            if (!redis.sismember(hotKey, campaignId.toString())) {
                redis.sadd(hotKey, campaignId.toString())
                // keep hot set twice the window length
                redis.expire(hotKey, Duration.ofMinutes(20))
            }
        }
    }

    fun isHotCampaign(tenantId: UUID, campaignId: UUID): Boolean {
        val hotKey = "hot:set:${tenantId}"
        return redis.sismember(hotKey, campaignId.toString())
    }

    fun totalHotCampaigns(): Long {
        val keys = redis.keys("hot:set:*")
        return keys.sumOf { redis.scard(it) }
    }
}

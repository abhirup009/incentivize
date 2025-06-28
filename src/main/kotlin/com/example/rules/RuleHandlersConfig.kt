package com.example.rules

import com.example.generated.model.ActionEvent
import com.example.redis.RedisClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.*

@Configuration
class RuleHandlersConfig(private val redis: RedisClient) {

    /** ACTION_COUNT rule – checks how many times the user performed the action within a window. */
    @Bean
    fun actionCountRule(): RuleHandlerRegistration = RuleHandlerRegistration(
        RuleType.ACTION_COUNT,
        RuleHandler { event: ActionEvent, campaignId: UUID, params: Map<String, Any> ->
            val targetCount = (params["count"] as? Number)?.toInt() ?: return@RuleHandler true
            val window = (params["window"] as? String)?.uppercase() ?: "DAILY"

            val ttl: Duration = when (window) {
                "DAILY" -> Duration.ofDays(1)
                "MONTHLY" -> Duration.ofDays(30)
                else -> Duration.ofDays(365)
            }

            // Redis key: rule:ac:{campaignId}:{userId}
            val key = "rule:ac:${campaignId}:${event.userId}"
            val current = redis.incr(key, ttl)
            current <= targetCount
        }
    )

    /** COHORT rule – passes only if event.attributes["cohort"] matches expected id. */
    @Bean
    fun cohortRule(): RuleHandlerRegistration = RuleHandlerRegistration(
        RuleType.COHORT,
        RuleHandler { event: ActionEvent, _: UUID, params: Map<String, Any> ->
            val expected = params["cohortId"]?.toString() ?: return@RuleHandler true
            val actual = event.attributes?.get("cohort")?.toString()
            expected == actual
        }
    )
}

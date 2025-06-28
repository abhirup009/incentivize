package com.example.rules

import com.example.generated.model.ActionEvent
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*

/**
 * Enum of known rule types. Extend by adding a new constant and registering a [RuleHandler].
 */
enum class RuleType {
    ACTION_COUNT,
    COHORT,
}

/**
 * Flexible rule definition that can be serialised to JSON.
 * It is intentionally generic – concrete handlers interpret the params map.
 */
 data class Rule(
    val type: RuleType,
    val params: Map<String, Any> = emptyMap()
 )

/** Handler contract for individual rule types. */
fun interface RuleHandler {
    fun evaluate(event: ActionEvent, campaignId: UUID, params: Map<String, Any>): Boolean
}

/**
 * Tiered rule evaluation engine.
 * New rules are added by exposing a Spring bean implementing [RuleHandler] and annotating with [RuleType].
 */
@Component
class RuleEngineService(handlers: List<RuleHandlerRegistration>) {

    private val handlerMap: Map<RuleType, RuleHandler> = handlers.associate { it.type to it.handler }

    fun evaluateAll(event: ActionEvent, campaignId: UUID, rules: List<Rule>): Boolean {
        if (rules.isEmpty()) return true
        for (rule in rules) {
            val handler = handlerMap[rule.type]
                ?: error("No handler registered for rule type ${rule.type}")
            if (!handler.evaluate(event, campaignId, rule.params)) return false
        }
        return true
    }
}

/**
 * Wrapper to bind a handler bean with its type. Easier than custom annotations.
 */
data class RuleHandlerRegistration(val type: RuleType, val handler: RuleHandler)

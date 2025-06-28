package com.example.service

import com.example.generated.model.ActionEvent
import com.example.rules.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class RuleEngineServiceTests {

    private val trueHandler = RuleHandler { _, _, _ -> true }
    private val falseHandler = RuleHandler { _, _, _ -> false }

    private fun dummyEvent() = ActionEvent(
        tenantId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        actionCode = "LOGIN",
        eventTimestamp = OffsetDateTime.now(),
        attributes = emptyMap()
    )

    @Test
    fun `evaluateAll returns true when all rules pass`() {
        val engine = RuleEngineService(listOf(RuleHandlerRegistration(RuleType.ACTION_COUNT, trueHandler)))
        val result = engine.evaluateAll(dummyEvent(), UUID.randomUUID(), listOf(Rule(RuleType.ACTION_COUNT)))
        assertTrue(result)
    }

    @Test
    fun `evaluateAll returns false when any rule fails`() {
        val engine = RuleEngineService(listOf(
            RuleHandlerRegistration(RuleType.COHORT, falseHandler)
        ))
        val res = engine.evaluateAll(dummyEvent(), UUID.randomUUID(), listOf(Rule(RuleType.COHORT)))
        assertFalse(res)
    }
}

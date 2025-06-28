package com.example.domain

import java.util.UUID

/**
 * Aggregate root representing a marketing campaign (simplified for IC).
 */
data class Campaign(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val type: CampaignType,
    val rules: List<com.example.rules.Rule>? = null,
    val requiredActions: Set<String>
) {
    enum class CampaignType { SIMPLE, QUEST }
}

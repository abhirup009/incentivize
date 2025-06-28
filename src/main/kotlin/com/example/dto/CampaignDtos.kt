package com.example.dto

import java.time.OffsetDateTime
import java.util.*

/**
 * DTOs and enums used by Campaign management APIs.
 */

// API request for creating a campaign

data class CampaignCreateRequest(
    val tenantId: UUID,
    val name: String,
    val type: CampaignType,
    val startAt: OffsetDateTime,
    val endAt: OffsetDateTime,
    val rules: List<com.example.rules.Rule>? = null,
)

// API request for updating a campaign

data class CampaignUpdateRequest(
    val name: String? = null,
    val endAt: OffsetDateTime? = null,
    val status: CampaignStatus? = null,
    val rules: List<com.example.rules.Rule>? = null,
)

// API response DTO for campaign

data class CampaignDTO(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val type: CampaignType,
    val status: CampaignStatus,
    val startAt: OffsetDateTime,
    val endAt: OffsetDateTime,
    val rules: List<com.example.rules.Rule>?,
    val createdAt: OffsetDateTime,
)

enum class CampaignType { SIMPLE, QUEST }

enum class CampaignStatus { DRAFT, ACTIVE, PAUSED, ENDED }

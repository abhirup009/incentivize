package com.example.dto

import java.util.UUID

/**
 * Payload sent over the WebSocket topic `/topic/incentives` whenever an incentive is generated.
 */
data class IncentiveMessageDTO(
    val id: UUID,
    val userId: UUID,
    val type: String,
    val amount: Double,
    val currency: String,
    val campaignName: String,
)

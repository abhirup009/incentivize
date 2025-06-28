package com.example.repository

import com.example.jooq.generated.tables.UserAggregation.Companion.USER_AGGREGATION
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

interface IUserAggregationRepository {
    fun save(id: UUID, userId: UUID, campaignId: UUID, action: String)
    fun actionsForUserCampaign(userId: UUID, campaignId: UUID): List<String>
}

@Repository
class UserAggregationRepositoryImpl(private val dsl: DSLContext) : IUserAggregationRepository {
    override fun save(id: UUID, userId: UUID, campaignId: UUID, action: String) {
        dsl.insertInto(USER_AGGREGATION)
            .set(USER_AGGREGATION.ID, id)
            .set(USER_AGGREGATION.USER_ID, userId)
            .set(USER_AGGREGATION.CAMPAIGN_ID, campaignId)
            .set(USER_AGGREGATION.ACTION_CODE, action)
            .execute()
    }

    override fun actionsForUserCampaign(userId: UUID, campaignId: UUID): List<String> =
        dsl.select(USER_AGGREGATION.ACTION_CODE)
            .from(USER_AGGREGATION)
            .where(
                USER_AGGREGATION.USER_ID.eq(userId)
                    .and(USER_AGGREGATION.CAMPAIGN_ID.eq(campaignId))
            )
            .fetch(USER_AGGREGATION.ACTION_CODE, String::class.java)
}

package com.example.repository

import com.example.jooq.generated.tables.Campaign.Companion.CAMPAIGN
import com.example.jooq.generated.enums.CampaignType as JCampaignType
import com.example.domain.Campaign as DomainCampaign
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

interface ICampaignRepository {
    fun save(campaign: DomainCampaign)
    fun findById(id: UUID): DomainCampaign?
    fun findActiveCampaigns(tenantId: UUID, actionCode: String): List<DomainCampaign>
}

@Repository
class CampaignRepositoryImpl(private val dsl: DSLContext) : ICampaignRepository {
    override fun save(campaign: DomainCampaign) {
        dsl.transaction { conf ->
            val ctx = conf.dsl()
            ctx.insertInto(CAMPAIGN)
                .set(CAMPAIGN.CAMPAIGN_ID, campaign.id)
                .set(CAMPAIGN.TENANT_ID, campaign.tenantId)
                .set(CAMPAIGN.NAME, campaign.name)
                .set(CAMPAIGN.TYPE, JCampaignType.valueOf(campaign.type.name))
                .set(CAMPAIGN.RULE_JSON, JSONB.valueOf("{}")) // placeholder
                .set(CAMPAIGN.START_AT, OffsetDateTime.now())
                .set(CAMPAIGN.END_AT, OffsetDateTime.now().plusDays(30))
                .execute()
        }
    }

    override fun findActiveCampaigns(tenantId: UUID, actionCode: String): List<DomainCampaign> {
        val now = OffsetDateTime.now()
        val recs = dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN.TENANT_ID.eq(tenantId))
            .and(CAMPAIGN.START_AT.le(now))
            .and(CAMPAIGN.END_AT.ge(now))
            // TODO once rule JSON stores actionCode mapping, filter by it
            .fetch()

        return recs.map {
            DomainCampaign(
                id = it.get(CAMPAIGN.CAMPAIGN_ID)!!,
                tenantId = it.get(CAMPAIGN.TENANT_ID)!!,
                name = it.get(CAMPAIGN.NAME)!!,
                type = DomainCampaign.CampaignType.valueOf(it.get(CAMPAIGN.TYPE)!!.name),
                requiredActions = emptySet()
            )
        }
    }

    override fun findById(id: UUID): DomainCampaign? {
        val rec: Record? = dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN.CAMPAIGN_ID.eq(id))
            .fetchOne()
        return rec?.let {
            DomainCampaign(
                id = it.get(CAMPAIGN.CAMPAIGN_ID)!!,
                tenantId = it.get(CAMPAIGN.TENANT_ID)!!,
                name = it.get(CAMPAIGN.NAME)!!,
                type = DomainCampaign.CampaignType.valueOf(it.get(CAMPAIGN.TYPE)!!.name),
                requiredActions = emptySet()
            )
        }
    }
}

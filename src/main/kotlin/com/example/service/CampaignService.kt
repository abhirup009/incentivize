package com.example.service

import com.example.dto.*
import com.example.repository.ICampaignRepository
import com.example.domain.Campaign as DomainCampaign
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class CampaignService(private val repo: ICampaignRepository) {

    fun create(req: CampaignCreateRequest): CampaignDTO {
        val campaign = DomainCampaign(
            id = UUID.randomUUID(),
            tenantId = req.tenantId,
            name = req.name,
            type = DomainCampaign.CampaignType.valueOf(req.type.name),
            rules = req.rules,
            requiredActions = emptySet()
        )
        repo.save(campaign)
        return campaign.toDto(CampaignStatus.DRAFT)
    }

    fun get(id: UUID): CampaignDTO? = repo.findById(id)?.toDto(CampaignStatus.ACTIVE)

    fun update(id: UUID, req: CampaignUpdateRequest): CampaignDTO? {
        val existing = repo.findById(id) ?: return null
        val updated = existing.copy(name = req.name ?: existing.name)
        repo.save(updated)
        return updated.toDto(req.status ?: CampaignStatus.ACTIVE)
    }

    fun list(tenantId: UUID): List<CampaignDTO> {
        // TODO: implement tenant-specific filtering when repository supports it
        return emptyList()
    }

    fun activate(id: UUID) { /* TODO */ }
    fun pause(id: UUID) { /* TODO */ }
}

// mapping helper
private fun DomainCampaign.toDto(status: CampaignStatus) = CampaignDTO(
    id = this.id,
    tenantId = this.tenantId,
    name = this.name,
    type = CampaignType.valueOf(this.type.name),
    status = status,
    startAt = OffsetDateTime.now(),
    endAt = OffsetDateTime.now().plusDays(30),
    rules = this.rules,
    createdAt = OffsetDateTime.now(),
)

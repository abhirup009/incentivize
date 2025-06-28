package com.example.repository

import com.example.jooq.generated.tables.Incentive.Companion.INCENTIVE
import com.example.jooq.generated.enums.IncentiveType as JIncentiveType
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

interface IIncentiveRepository {
    fun save(incentiveId: UUID, type: String, currency: String, amount: Double)
}

@Repository
class IncentiveRepositoryImpl(private val dsl: DSLContext) : IIncentiveRepository {
    override fun save(incentiveId: UUID, type: String, currency: String, amount: Double) {
        dsl.insertInto(INCENTIVE)
            .set(INCENTIVE.ID, incentiveId)
            .set(INCENTIVE.TYPE, JIncentiveType.valueOf(type))
            .set(INCENTIVE.REWARD_CURRENCY, currency)
            .set(INCENTIVE.REWARD_AMOUNT, BigDecimal.valueOf(amount))
            .execute()
    }
}

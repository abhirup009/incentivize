package com.example.service

import com.example.generated.model.Limit
import com.example.generated.model.LimitCreateRequest

import com.example.generated.model.LimitUpdateRequest
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Temporary in-memory implementation for limit management.
 * Will be replaced by persistent repository later.
 */
@Service
class LimitService {

    private val store: MutableMap<UUID, Limit> = ConcurrentHashMap()

    fun create(req: LimitCreateRequest): Limit {
        val id = UUID.randomUUID()
        val limit = Limit(
            id = id,
            tenantId = req.tenantId,
            code = req.code,
            cap = req.cap,
            window = req.window,
            status = Limit.Status.aCTIVE
        )
        store[id] = limit
        return limit
    }

    fun get(id: UUID): Limit? = store[id]

    fun update(id: UUID, req: LimitUpdateRequest): Limit? {
        val current = store[id] ?: return null
        val updated = current.copy(
            cap = req.cap ?: current.cap,
            status = req.status?.let { when (it) {
                LimitUpdateRequest.Status.aCTIVE -> Limit.Status.aCTIVE
                LimitUpdateRequest.Status.pAUSED -> Limit.Status.pAUSED
            } } ?: current.status
        )
        store[id] = updated
        return updated
    }

    fun list(tenantId: UUID): List<Limit> = store.values.filter { it.tenantId == tenantId }
}

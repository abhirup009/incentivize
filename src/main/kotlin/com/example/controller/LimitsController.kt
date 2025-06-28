package com.example.controller

import com.example.generated.api.LimitsApi
import com.example.generated.api.TenantsApi
import com.example.generated.model.Limit
import com.example.generated.model.LimitCreateRequest
import com.example.generated.model.LimitUpdateRequest
import com.example.service.LimitService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class LimitsController(private val svc: LimitService) : LimitsApi, TenantsApi {

    override fun createLimit(limitCreateRequest: LimitCreateRequest): ResponseEntity<Limit> {
        val created = svc.create(limitCreateRequest)
        return ResponseEntity.status(201).body(created)
    }

    override fun getLimit(id: UUID): ResponseEntity<Limit> {
        val limit = svc.get(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(limit)
    }

    override fun updateLimit(id: UUID, limitUpdateRequest: LimitUpdateRequest): ResponseEntity<Limit> {
        val updated = svc.update(id, limitUpdateRequest) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    override fun listLimits(tenantId: UUID): ResponseEntity<List<Limit>> =
        ResponseEntity.ok(svc.list(tenantId))
}

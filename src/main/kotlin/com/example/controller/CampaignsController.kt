package com.example.controller

import com.example.dto.*
import com.example.service.CampaignService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class CampaignsController(private val svc: CampaignService) {

    @PostMapping("/campaigns")
    fun create(@RequestBody req: CampaignCreateRequest): ResponseEntity<CampaignDTO> {
        val dto = svc.create(req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/campaigns/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<CampaignDTO> {
        val c = svc.get(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(c)
    }

    // Simplified update – only name/endAt/status
    @PutMapping("/campaigns/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: CampaignUpdateRequest): ResponseEntity<CampaignDTO> {
        val dto = svc.update(id, req) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/tenants/{tenantId}/campaigns")
    fun list(@PathVariable tenantId: UUID): ResponseEntity<List<CampaignDTO>> = ResponseEntity.ok(svc.list(tenantId))

    @PostMapping("/campaigns/{id}:activate")
    fun activate(@PathVariable id: UUID): ResponseEntity<Void> {
        svc.activate(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/campaigns/{id}:pause")
    fun pause(@PathVariable id: UUID): ResponseEntity<Void> {
        svc.pause(id)
        return ResponseEntity.ok().build()
    }
}

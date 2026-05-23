package br.com.agent.engine.controller

import br.com.agent.engine.entity.Architecture
import br.com.agent.engine.service.ArchitectureService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/architectures")
class ArchitectureController(
    private val architectureService: ArchitectureService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<Architecture>> =
        ResponseEntity.ok(architectureService.findAll())
}


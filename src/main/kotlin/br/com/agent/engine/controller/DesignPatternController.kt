package br.com.agent.engine.controller

import br.com.agent.engine.entity.DesignPattern
import br.com.agent.engine.service.DesignPatternService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/design-patterns")
class DesignPatternController(
    private val designPatternService: DesignPatternService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<DesignPattern>> =
        ResponseEntity.ok(designPatternService.findAll())
}


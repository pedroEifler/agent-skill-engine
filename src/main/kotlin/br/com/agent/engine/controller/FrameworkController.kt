package br.com.agent.engine.controller

import br.com.agent.engine.entity.Framework
import br.com.agent.engine.service.FrameworkService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/frameworks")
class FrameworkController(
    private val frameworkService: FrameworkService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<Framework>> =
        ResponseEntity.ok(frameworkService.findAll())

    @GetMapping("/language/{languageId}")
    fun getByLanguage(@PathVariable languageId: Long): ResponseEntity<List<Framework>> =
        ResponseEntity.ok(frameworkService.findByLanguageId(languageId))
}


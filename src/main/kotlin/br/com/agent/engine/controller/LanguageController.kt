package br.com.agent.engine.controller

import br.com.agent.engine.entity.Language
import br.com.agent.engine.service.LanguageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/languages")
class LanguageController(
    private val languageService: LanguageService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<Language>> {
        return ResponseEntity.ok(languageService.findAll())
    }
}



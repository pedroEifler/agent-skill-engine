package br.com.agent.engine.controller

import br.com.agent.engine.dto.GenerateSkillRequest
import br.com.agent.engine.dto.GenerateSkillResponse
import br.com.agent.engine.service.SkillGeneratorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/skills")
class SkillController(
    private val skillGeneratorService: SkillGeneratorService
) {

    @PostMapping("/generate")
    fun generateSkill(@RequestBody request: GenerateSkillRequest): ResponseEntity<GenerateSkillResponse> {
        val content = skillGeneratorService.generate(request)
        return ResponseEntity.ok(GenerateSkillResponse(content = content))
    }
}

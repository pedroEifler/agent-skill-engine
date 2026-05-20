package br.com.agent.engine.controller

import br.com.agent.engine.dto.GenerateSkillRequest
import br.com.agent.engine.dto.GenerateSkillResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/skills")
class SkillController {

    @PostMapping("/generate")
    fun generateSkill(@RequestBody request: GenerateSkillRequest): ResponseEntity<GenerateSkillResponse> {
        val response = GenerateSkillResponse(
            languageId = request.languageId,
            frameworkId = request.frameworkId,
            architectureId = request.architectureId,
            designPatternIds = request.designPatternIds,
            message = "Skill generation request received successfully"
        )
        return ResponseEntity.ok(response)
    }
}


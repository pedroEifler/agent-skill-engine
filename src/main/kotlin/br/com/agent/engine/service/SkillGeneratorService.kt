package br.com.agent.engine.service

import br.com.agent.engine.dto.GenerateSkillRequest
import br.com.agent.engine.dto.GenerateSkillResponse

interface SkillGeneratorService {
    fun generate(request: GenerateSkillRequest): GenerateSkillResponse
}


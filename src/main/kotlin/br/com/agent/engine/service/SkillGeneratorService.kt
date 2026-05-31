package br.com.agent.engine.service

import br.com.agent.engine.dto.GenerateSkillRequest

interface SkillGeneratorService {
    fun generate(request: GenerateSkillRequest): String
}


package br.com.agent.engine.dto

data class GenerateSkillResponse(
    val content: String,
    val references: List<SkillReference>
)

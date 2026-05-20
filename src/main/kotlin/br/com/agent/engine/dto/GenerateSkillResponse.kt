package br.com.agent.engine.dto

data class GenerateSkillResponse(
    val languageId: Long,
    val frameworkId: Long,
    val architectureId: Long,
    val designPatternIds: List<Long>,
    val message: String
)


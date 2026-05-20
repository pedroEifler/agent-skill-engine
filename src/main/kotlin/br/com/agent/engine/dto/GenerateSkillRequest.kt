package br.com.agent.engine.dto

data class GenerateSkillRequest(
    val languageId: Long,
    val frameworkId: Long,
    val architectureId: Long,
    val designPatternIds: List<Long>
)


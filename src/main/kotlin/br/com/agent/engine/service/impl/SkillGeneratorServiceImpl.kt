package br.com.agent.engine.service.impl

import br.com.agent.engine.dto.GenerateSkillRequest
import br.com.agent.engine.dto.GenerateSkillResponse
import br.com.agent.engine.dto.SkillReference
import br.com.agent.engine.repository.ArchitectureRepository
import br.com.agent.engine.repository.DesignPatternRepository
import br.com.agent.engine.repository.FrameworkRepository
import br.com.agent.engine.repository.LanguageRepository
import br.com.agent.engine.service.SkillGeneratorService
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Service

@Service
class SkillGeneratorServiceImpl(
    private val languageRepository: LanguageRepository,
    private val frameworkRepository: FrameworkRepository,
    private val architectureRepository: ArchitectureRepository,
    private val designPatternRepository: DesignPatternRepository
) : SkillGeneratorService {

    override fun generate(request: GenerateSkillRequest): GenerateSkillResponse {
        val language = languageRepository.findById(request.languageId)
            .orElseThrow { IllegalArgumentException("Language not found with id: ${request.languageId}") }

        val framework = frameworkRepository.findById(request.frameworkId)
            .orElseThrow { IllegalArgumentException("Framework not found with id: ${request.frameworkId}") }

        val architecture = architectureRepository.findById(request.architectureId)
            .orElseThrow { IllegalArgumentException("Architecture not found with id: ${request.architectureId}") }

        val patterns = designPatternRepository.findAllById(request.designPatternIds)

        val type = request.type

        val parts = mutableListOf<String>()
        parts += loadSkillFile("languages/${language.slug}-$type/SKILL.md")
        parts += loadSkillFile("frameworks/${framework.slug}-$type/SKILL.md")
        parts += loadSkillFile("architectures/${architecture.slug}-$type/SKILL.md")
        patterns.forEach { parts += loadSkillFile("design-patterns/${it.slug}-$type/SKILL.md") }

        val content = parts.joinToString("\n\n---\n\n")

        val references = mutableListOf<SkillReference>()
        references += loadReferences("languages/${language.slug}-$type/references")
        references += loadReferences("frameworks/${framework.slug}-$type/references")
        references += loadReferences("architectures/${architecture.slug}-$type/references")
        patterns.forEach { references += loadReferences("design-patterns/${it.slug}-$type/references") }

        return GenerateSkillResponse(content = content, references = references)
    }

    private fun loadSkillFile(path: String): String {
        val resource = ClassPathResource("skills/$path")
        if (!resource.exists()) {
            throw IllegalArgumentException("Skill file not found: skills/$path")
        }
        return resource.inputStream.bufferedReader().readText()
    }

    private fun loadReferences(path: String): List<SkillReference> {
        val resolver = PathMatchingResourcePatternResolver()
        val pattern = "classpath:skills/$path/*.md"
        return try {
            resolver.getResources(pattern).map { resource ->
                SkillReference(
                    fileName = resource.filename ?: "unknown",
                    content = resource.inputStream.bufferedReader().readText()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}


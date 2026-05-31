package br.com.agent.engine.service.impl

import br.com.agent.engine.dto.GenerateSkillRequest
import br.com.agent.engine.repository.ArchitectureRepository
import br.com.agent.engine.repository.DesignPatternRepository
import br.com.agent.engine.repository.FrameworkRepository
import br.com.agent.engine.repository.LanguageRepository
import br.com.agent.engine.service.SkillGeneratorService
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class SkillGeneratorServiceImpl(
    private val languageRepository: LanguageRepository,
    private val frameworkRepository: FrameworkRepository,
    private val architectureRepository: ArchitectureRepository,
    private val designPatternRepository: DesignPatternRepository
) : SkillGeneratorService {

    override fun generate(request: GenerateSkillRequest): String {
        val language = languageRepository.findById(request.languageId)
            .orElseThrow { IllegalArgumentException("Language not found with id: ${request.languageId}") }

        val framework = frameworkRepository.findById(request.frameworkId)
            .orElseThrow { IllegalArgumentException("Framework not found with id: ${request.frameworkId}") }

        val architecture = architectureRepository.findById(request.architectureId)
            .orElseThrow { IllegalArgumentException("Architecture not found with id: ${request.architectureId}") }

        val patterns = designPatternRepository.findAllById(request.designPatternIds)

        val parts = mutableListOf<String>()
        parts += loadSkillFile("languages/${language.slug}.md")
        parts += loadSkillFile("frameworks/${framework.slug}.md")
        parts += loadSkillFile("architectures/${architecture.slug}.md")
        patterns.forEach { parts += loadSkillFile("design-patterns/${it.slug}.md") }

        return parts.joinToString("\n\n---\n\n")
    }

    private fun loadSkillFile(path: String): String {
        val resource = ClassPathResource("skills/$path")
        if (!resource.exists()) {
            throw IllegalArgumentException("Skill file not found: skills/$path")
        }
        return resource.inputStream.bufferedReader().readText()
    }
}


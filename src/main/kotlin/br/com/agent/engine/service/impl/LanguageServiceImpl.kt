package br.com.agent.engine.service.impl

import br.com.agent.engine.entity.Language
import br.com.agent.engine.repository.LanguageRepository
import br.com.agent.engine.service.LanguageService
import org.springframework.stereotype.Service

@Service
class LanguageServiceImpl(
    private val languageRepository: LanguageRepository
) : LanguageService {

    override fun findAll(): List<Language> {
        return languageRepository.findAll()
    }
}


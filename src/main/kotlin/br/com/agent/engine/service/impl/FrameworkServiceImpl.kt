package br.com.agent.engine.service.impl

import br.com.agent.engine.entity.Framework
import br.com.agent.engine.repository.FrameworkRepository
import br.com.agent.engine.service.FrameworkService
import org.springframework.stereotype.Service

@Service
class FrameworkServiceImpl(
    private val frameworkRepository: FrameworkRepository
) : FrameworkService {

    override fun findAll(): List<Framework> = frameworkRepository.findAllByOrderByLockedAscNameAsc()

    override fun findByLanguageId(languageId: Long): List<Framework> =
        frameworkRepository.findByLanguageIdOrderByLockedAscNameAsc(languageId)
}


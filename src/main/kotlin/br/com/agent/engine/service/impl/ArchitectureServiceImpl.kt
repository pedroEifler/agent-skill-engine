package br.com.agent.engine.service.impl

import br.com.agent.engine.entity.Architecture
import br.com.agent.engine.repository.ArchitectureRepository
import br.com.agent.engine.service.ArchitectureService
import org.springframework.stereotype.Service

@Service
class ArchitectureServiceImpl(
    private val architectureRepository: ArchitectureRepository
) : ArchitectureService {

    override fun findAll(): List<Architecture> = architectureRepository.findAll()
}


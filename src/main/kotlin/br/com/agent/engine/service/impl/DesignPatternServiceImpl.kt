package br.com.agent.engine.service.impl

import br.com.agent.engine.entity.DesignPattern
import br.com.agent.engine.repository.DesignPatternRepository
import br.com.agent.engine.service.DesignPatternService
import org.springframework.stereotype.Service

@Service
class DesignPatternServiceImpl(
    private val designPatternRepository: DesignPatternRepository
) : DesignPatternService {

    override fun findAll(): List<DesignPattern> = designPatternRepository.findAll()
}


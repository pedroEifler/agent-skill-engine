package br.com.agent.engine.service

import br.com.agent.engine.entity.Architecture

interface ArchitectureService {
    fun findAll(): List<Architecture>
}


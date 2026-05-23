package br.com.agent.engine.service

import br.com.agent.engine.entity.Framework

interface FrameworkService {
    fun findAll(): List<Framework>
    fun findByLanguageId(languageId: Long): List<Framework>
}


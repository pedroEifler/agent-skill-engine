package br.com.agent.engine.service

import br.com.agent.engine.entity.Language

interface LanguageService {
    fun findAll(): List<Language>
}


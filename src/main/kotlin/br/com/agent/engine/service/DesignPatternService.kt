package br.com.agent.engine.service

import br.com.agent.engine.entity.DesignPattern

interface DesignPatternService {
    fun findAll(): List<DesignPattern>
}


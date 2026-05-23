package br.com.agent.engine.repository

import br.com.agent.engine.entity.DesignPattern
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DesignPatternRepository : JpaRepository<DesignPattern, Long>


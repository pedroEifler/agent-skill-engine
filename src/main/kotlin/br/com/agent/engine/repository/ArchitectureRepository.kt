package br.com.agent.engine.repository

import br.com.agent.engine.entity.Architecture
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchitectureRepository : JpaRepository<Architecture, Long>


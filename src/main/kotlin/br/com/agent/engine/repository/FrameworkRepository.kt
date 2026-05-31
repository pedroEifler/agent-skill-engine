package br.com.agent.engine.repository

import br.com.agent.engine.entity.Framework
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FrameworkRepository : JpaRepository<Framework, Long> {
    fun findAllByOrderByLockedAscNameAsc(): List<Framework>
    fun findByLanguageIdOrderByLockedAscNameAsc(languageId: Long): List<Framework>
}


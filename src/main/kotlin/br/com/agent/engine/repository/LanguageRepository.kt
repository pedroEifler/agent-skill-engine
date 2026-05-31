package br.com.agent.engine.repository

import br.com.agent.engine.entity.Language
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LanguageRepository : JpaRepository<Language, Long> {
    fun findAllByOrderByLockedAscNameAsc(): List<Language>
}

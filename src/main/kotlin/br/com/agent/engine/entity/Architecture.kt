package br.com.agent.engine.entity

import jakarta.persistence.*

@Entity
@Table(name = "architecture", schema = "skill_engine")
class Architecture(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, length = 255)
    val description: String,

    @Column(nullable = false)
    val locked: Boolean = false
)



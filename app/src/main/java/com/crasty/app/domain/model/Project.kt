package com.crasty.app.domain.model

import java.time.Instant
import java.time.LocalDateTime

/** Lista de tareas con fecha límite: todas sus tareas deben caer antes de [deadline]. */
data class Project(
    val id: Long = 0,
    val name: String,
    val deadline: LocalDateTime,
    val isCompleted: Boolean = false,
    /** Posición en la paleta de colores de proyecto. */
    val colorIndex: Int = 0,
    val createdAt: Instant = Instant.now(),
)

data class ProjectSummary(
    val project: Project,
    val totalTasks: Int,
    val doneTasks: Int,
)

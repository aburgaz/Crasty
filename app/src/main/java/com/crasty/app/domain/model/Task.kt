package com.crasty.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    /** Día de la tarea. En las recurrentes es el día de inicio (o el de la repetición que se muestra). */
    val date: LocalDate,
    val reminderTime: LocalTime? = null,
    val isCompleted: Boolean = false,
    /** Cuántas veces se ha procrastinado. */
    val postponedCount: Int = 0,
    val createdAt: Instant = Instant.now(),
    val projectId: Long? = null,
    /** null = tarea normal (de un solo día). */
    val recurrence: Recurrence? = null,
    /** Día en que se marcó como completada (solo tareas normales). */
    val completedOn: LocalDate? = null,
) {
    val isRecurring: Boolean get() = recurrence != null

    /** Se puede arrastrar entre Hoy y Mañana. */
    val isMovable: Boolean get() = recurrence == null && !isCompleted

    /** Cumple la fecha límite de un proyecto. Sin hora, basta con que el día no sea posterior. */
    fun fitsBefore(deadline: LocalDateTime): Boolean =
        reminderTime?.let { date.atTime(it).isBefore(deadline) } ?: !date.isAfter(deadline.toLocalDate())
}

package com.crasty.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val date: LocalDate,
    val reminderTime: LocalTime? = null,
    val isCompleted: Boolean = false,
    /** Cuántas veces se ha procrastinado. */
    val postponedCount: Int = 0,
    val createdAt: Instant = Instant.now(),
) {
    val reminderAt: LocalDateTime? get() = reminderTime?.let { date.atTime(it) }
}

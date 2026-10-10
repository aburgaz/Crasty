package com.crasty.app.data.backup

import com.crasty.app.domain.model.Task
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Formato del archivo JSON de copia de seguridad. Si cambia, subir [version]. */
@Serializable
data class BackupFile(
    val version: Int = 1,
    val exportedAt: String,
    val tasks: List<BackupTask>,
)

@Serializable
data class BackupTask(
    val title: String,
    val notes: String = "",
    val date: String,
    val reminderTime: String? = null,
    val isCompleted: Boolean = false,
    val postponedCount: Int = 0,
    val createdAt: String,
)

fun Task.toBackup() = BackupTask(
    title = title,
    notes = notes,
    date = date.toString(),
    reminderTime = reminderTime?.toString(),
    isCompleted = isCompleted,
    postponedCount = postponedCount,
    createdAt = createdAt.toString(),
)

fun BackupTask.toTask() = Task(
    title = title,
    notes = notes,
    date = LocalDate.parse(date),
    reminderTime = reminderTime?.let(LocalTime::parse),
    isCompleted = isCompleted,
    postponedCount = postponedCount,
    createdAt = Instant.parse(createdAt),
)

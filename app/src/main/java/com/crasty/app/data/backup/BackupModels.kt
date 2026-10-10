package com.crasty.app.data.backup

import com.crasty.app.data.repository.Snapshot
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Formato del archivo JSON de copia de seguridad. Si cambia, subir [version].
 * v1: solo tareas. v2: proyectos, tareas recurrentes y fecha de completado (los archivos v1 se siguen leyendo).
 * Las `key` solo sirven para enlazar elementos dentro del archivo.
 */
@Serializable
data class BackupFile(
    val version: Int = 2,
    val exportedAt: String,
    val projects: List<BackupProject> = emptyList(),
    val tasks: List<BackupTask>,
    val recurringCompletions: List<BackupCompletion> = emptyList(),
)

@Serializable
data class BackupProject(
    val key: Long,
    val name: String,
    val deadline: String,
    val isCompleted: Boolean = false,
    val createdAt: String,
)

@Serializable
data class BackupTask(
    val key: Long = 0,
    val title: String,
    val notes: String = "",
    val date: String,
    val reminderTime: String? = null,
    val isCompleted: Boolean = false,
    val postponedCount: Int = 0,
    val createdAt: String,
    val projectKey: Long? = null,
    val repeatInterval: Int? = null,
    val repeatUnit: String? = null,
    val completedOn: String? = null,
)

@Serializable
data class BackupCompletion(
    val taskKey: Long,
    val date: String,
)

fun Snapshot.toBackup(exportedAt: Instant) = BackupFile(
    exportedAt = exportedAt.toString(),
    projects = projects.map { it.toBackup() },
    tasks = tasks.map { it.toBackup() },
    recurringCompletions = recurringCompletions.map { (taskId, day) -> BackupCompletion(taskId, day.toString()) },
)

fun BackupFile.toSnapshot() = Snapshot(
    projects = projects.map { it.toProject() },
    tasks = tasks.map { it.toTask() },
    recurringCompletions = recurringCompletions.map { it.taskKey to LocalDate.parse(it.date) },
)

fun Project.toBackup() = BackupProject(
    key = id,
    name = name,
    deadline = deadline.toString(),
    isCompleted = isCompleted,
    createdAt = createdAt.toString(),
)

fun BackupProject.toProject() = Project(
    id = key,
    name = name,
    deadline = LocalDateTime.parse(deadline),
    isCompleted = isCompleted,
    createdAt = Instant.parse(createdAt),
)

fun Task.toBackup() = BackupTask(
    key = id,
    title = title,
    notes = notes,
    date = date.toString(),
    reminderTime = reminderTime?.toString(),
    isCompleted = isCompleted,
    postponedCount = postponedCount,
    createdAt = createdAt.toString(),
    projectKey = projectId,
    repeatInterval = recurrence?.interval,
    repeatUnit = recurrence?.unit?.name,
    completedOn = completedOn?.toString(),
)

fun BackupTask.toTask() = Task(
    id = key,
    title = title,
    notes = notes,
    date = LocalDate.parse(date),
    reminderTime = reminderTime?.let(LocalTime::parse),
    isCompleted = isCompleted,
    postponedCount = postponedCount,
    createdAt = Instant.parse(createdAt),
    projectId = projectKey,
    recurrence = repeatUnit?.let { Recurrence(repeatInterval ?: 1, RepeatUnit.valueOf(it)) },
    completedOn = completedOn?.let(LocalDate::parse),
)

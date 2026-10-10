package com.crasty.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks", indices = [Index("dateEpochDay"), Index("projectId")])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String,
    val dateEpochDay: Long,
    val reminderMinuteOfDay: Int?,
    val isCompleted: Boolean,
    val postponedCount: Int,
    val createdAtMillis: Long,
    val projectId: Long? = null,
    val repeatInterval: Int? = null,
    /** Nombre de [RepeatUnit]; null = tarea normal. */
    val repeatUnit: String? = null,
    val completedEpochDay: Long? = null,
)

fun TaskEntity.toTask() = Task(
    id = id,
    title = title,
    notes = notes,
    date = LocalDate.ofEpochDay(dateEpochDay),
    reminderTime = reminderMinuteOfDay?.let { LocalTime.ofSecondOfDay(it * 60L) },
    isCompleted = isCompleted,
    postponedCount = postponedCount,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    projectId = projectId,
    recurrence = repeatUnit?.let { Recurrence(repeatInterval ?: 1, RepeatUnit.valueOf(it)) },
    completedOn = completedEpochDay?.let(LocalDate::ofEpochDay),
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    dateEpochDay = date.toEpochDay(),
    reminderMinuteOfDay = reminderTime?.let { it.hour * 60 + it.minute },
    isCompleted = isCompleted,
    postponedCount = postponedCount,
    createdAtMillis = createdAt.toEpochMilli(),
    projectId = projectId,
    repeatInterval = recurrence?.interval,
    repeatUnit = recurrence?.unit?.name,
    completedEpochDay = completedOn?.toEpochDay(),
)
